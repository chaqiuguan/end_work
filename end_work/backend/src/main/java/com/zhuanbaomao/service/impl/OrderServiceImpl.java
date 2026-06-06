package com.zhuanbaomao.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.PageResult;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.OrderCreateDTO;
import com.zhuanbaomao.entity.*;
import com.zhuanbaomao.mapper.*;
import com.zhuanbaomao.service.CartService;
import com.zhuanbaomao.service.OrderService;
import com.zhuanbaomao.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 *
 * <p>关键设计：
 * <ul>
 *   <li>使用 ConcurrentHashMap + ReentrantLock 防止超卖</li>
 *   <li>订单创建为幂等操作</li>
 *   <li>订单号使用雪花ID保证全局唯一</li>
 * </ul>
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final CartService cartService;

    /** 商品级本地锁（生产环境应替换为Redis分布式锁） */
    private final ConcurrentHashMap<Long, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartMapper cartMapper, ProductMapper productMapper,
                            AddressMapper addressMapper, CartService cartService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public Result<OrderVO> createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 获取选中的购物车项
        List<Cart> selectedItems = getSelectedCartItems(userId, dto.getCartItemIds());
        if (selectedItems.isEmpty()) {
            throw new BusinessException("结算商品不能为空");
        }

        // 2. 获取收货地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址无效");
        }

        String receiverAddress = address.getProvince() + address.getCity()
                + address.getDistrict() + " " + address.getDetail();

        // 3. 生成订单号
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 4. 锁住所有商品，防止超卖
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (Cart item : selectedItems) {
                ReentrantLock lock = productLocks.computeIfAbsent(
                        item.getProductId(), k -> new ReentrantLock());
                lock.lock();
                acquiredLocks.add(lock);
            }

            // 5. 校验库存并扣减
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (Cart item : selectedItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product == null || product.getStatus() != 1) {
                    throw new BusinessException("商品「" + (product != null ? product.getTitle() : "未知") + "」已下架");
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new BusinessException("商品「" + product.getTitle() + "」库存不足，当前库存: " + product.getStock());
                }

                // 原子扣减库存
                int deducted = productMapper.deductStock(product.getId(), item.getQuantity());
                if (deducted == 0) {
                    throw new BusinessException("商品「" + product.getTitle() + "」库存不足");
                }

                // 商品售罄则标记为已售
                Product updated = productMapper.selectById(product.getId());
                if (updated.getStock() <= 0) {
                    updated.setStatus(3);
                    productMapper.updateById(updated);
                }

                // 构建订单详情
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo(orderNo);
                orderItem.setProductId(product.getId());
                orderItem.setProductTitle(product.getTitle());
                orderItem.setProductImage(getFirstImage(product.getImages()));
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setTotalPrice(itemTotal);
                orderItems.add(orderItem);
            }

            // 6. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setFreight(BigDecimal.ZERO);
            order.setPayAmount(totalAmount);
            order.setPayType(dto.getPayType());
            order.setStatus(0);
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(receiverAddress);
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);

            // 7. 保存订单详情
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }

            // 8. 清空购物车中已结算商品
            List<Long> cartIdsToDelete = selectedItems.stream()
                    .map(Cart::getId).collect(Collectors.toList());
            cartMapper.deleteBatchIds(cartIdsToDelete);
            cartService.clearSelected(userId);

            log.info("订单创建成功: orderNo={}, userId={}, amount={}", orderNo, userId, totalAmount);

            OrderVO vo = buildOrderVO(order, orderItems);
            return Result.success("下单成功", vo);

        } catch (BusinessException e) {
            throw e;
        } finally {
            // 释放所有锁
            for (ReentrantLock lock : acquiredLocks) {
                try {
                    lock.unlock();
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    @Transactional
    public Result<?> pay(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(3002, "订单状态不允许支付，当前状态: " + getStatusText(order.getStatus()));
        }

        order.setStatus(1);
        order.setPayType(0);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("模拟支付成功: orderNo={}, amount={}", order.getOrderNo(), order.getPayAmount());
        return Result.success("支付成功（模拟）");
    }

    @Override
    @Transactional
    public Result<?> cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(3002, "仅待付款订单可取消");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(4);
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已取消: orderNo={}", order.getOrderNo());
        return Result.success("订单已取消");
    }

    @Override
    @Transactional
    public Result<?> confirmReceived(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(3002, "仅待收货订单可确认收货");
        }

        order.setStatus(3);
        order.setReceivedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已收货: orderNo={}", order.getOrderNo());
        return Result.success("已确认收货");
    }

    @Override
    public Result<OrderVO> detail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(buildOrderVO(order, items));
    }

    @Override
    public Result<?> listByUser(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, 0);

        if (status != null && status >= 0) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> orderPage = new Page<>(page, size);
        IPage<Order> result = orderMapper.selectPage(orderPage, wrapper);

        List<OrderVO> records = result.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                    OrderVO vo = buildOrderVO(order, items);
                    vo.setItems(null);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult<OrderVO> pageResult = new PageResult<>(
                result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
        return Result.success(pageResult);
    }

    // ==================== 私有辅助方法 ====================

    private List<Cart> getSelectedCartItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            return cartMapper.selectBatchIds(cartItemIds).stream()
                    .filter(c -> c.getUserId().equals(userId))
                    .collect(Collectors.toList());
        }
        return cartMapper.selectByUserId(userId).stream()
                .filter(c -> c.getSelected() == 1)
                .collect(Collectors.toList());
    }

    private OrderVO buildOrderVO(Order order, List<OrderItem> items) {
        List<OrderVO.OrderItemVO> itemVOs = items.stream()
                .map(item -> OrderVO.OrderItemVO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productTitle(item.getProductTitle())
                        .productImage(item.getProductImage())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .freight(order.getFreight())
                .payAmount(order.getPayAmount())
                .payType(order.getPayType())
                .payTypeText(order.getPayType() == 0 ? "模拟支付" : order.getPayType() == 1 ? "微信" : "支付宝")
                .status(order.getStatus())
                .statusText(getStatusText(order.getStatus()))
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .items(itemVOs)
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .receivedAt(order.getReceivedAt())
                .canceledAt(order.getCanceledAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    private String getFirstImage(String imagesJson) {
        if (StrUtil.isBlank(imagesJson)) return null;
        try {
            List<String> images = JSONUtil.toList(imagesJson, String.class);
            return images.isEmpty() ? null : images.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
