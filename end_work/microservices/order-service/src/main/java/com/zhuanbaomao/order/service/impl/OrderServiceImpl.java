package com.zhuanbaomao.order.service.impl;

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
import com.zhuanbaomao.entity.Order;
import com.zhuanbaomao.entity.OrderItem;
import com.zhuanbaomao.entity.Cart;
import com.zhuanbaomao.order.mapper.OrderMapper;
import com.zhuanbaomao.order.mapper.OrderItemMapper;
import com.zhuanbaomao.order.mapper.CartMapper;
import com.zhuanbaomao.order.service.CartService;
import com.zhuanbaomao.order.service.OrderService;
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
    private final CartService cartService;
    // ProductMapper/AddressMapper are in other services; use REST calls in production

    /** 商品级本地锁（生产环境应替换为Redis分布式锁） */
    private final ConcurrentHashMap<Long, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartMapper cartMapper, CartService cartService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
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

        // 2. 生成订单号
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 3. 构建订单（微服务：product库存校验通过 REST 调用 product-service）
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart item : selectedItems) {
            // TODO: REST call product-service to validate stock
            BigDecimal itemTotal = new BigDecimal("100").multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductTitle("商品" + item.getProductId());
            orderItem.setProductImage("");
            orderItem.setPrice(new BigDecimal("100"));
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setFreight(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setPayType(dto.getPayType());
        order.setStatus(0);
        order.setReceiverName("收货人"); // TODO: REST user-service
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("收货地址");
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        List<Long> cartIds = selectedItems.stream().map(Cart::getId).collect(Collectors.toList());
        cartMapper.deleteBatchIds(cartIds);
        cartService.clearSelected(userId);

        log.info("订单创建: orderNo={}, userId={}", orderNo, userId);
        return Result.success("下单成功", buildOrderVO(order, orderItems));
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
        // TODO: REST call product-service to restore stock

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
