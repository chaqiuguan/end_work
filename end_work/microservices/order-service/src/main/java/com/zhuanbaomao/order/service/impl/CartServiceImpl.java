package com.zhuanbaomao.order.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.CartItemDTO;
import com.zhuanbaomao.entity.Cart;
import com.zhuanbaomao.order.mapper.CartMapper;
import com.zhuanbaomao.order.service.CartService;
import com.zhuanbaomao.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 *
 * <p>设计思路：
 * <ul>
 *   <li>已登录用户：Redis作为一级缓存（可选），MySQL持久化兜底</li>
 *   <li>未登录用户：Redis存储（可选），key = "cart:anonymous:{deviceId}"</li>
 *   <li>登录时合并购物车</li>
 * </ul>
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    // ProductMapper is in product-service; use REST call in production

    private static final String CART_PREFIX = "cart:user:";
    private static final String ANONYMOUS_CART_PREFIX = "cart:anonymous:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    public CartServiceImpl(CartMapper cartMapper,
                           @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.cartMapper = cartMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public Result<?> addItem(Long userId, CartItemDTO dto) {
        // TODO: 微服务中通过 REST 调用 product-service 验证商品和库存

        Cart existing = cartMapper.selectByUserAndProduct(userId, dto.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            cart.setSelected(dto.getSelected() != null ? dto.getSelected() : 1);
            cartMapper.insert(cart);
        }

        // 清除 Redis 缓存
        clearCache(userId);
        log.info("购物车添加成功: userId={}, productId={}, qty={}", userId, dto.getProductId(), dto.getQuantity());
        return Result.success("已添加到购物车");
    }

    @Override
    @Transactional
    public Result<?> removeItem(Long userId, Long productId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        cartMapper.delete(wrapper);
        clearCache(userId);
        return Result.success("已从购物车移除");
    }

    @Override
    public Result<?> updateQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity < 1) {
            return removeItem(userId, productId);
        }
        Cart cart = cartMapper.selectByUserAndProduct(userId, productId);
        if (cart == null) {
            throw new BusinessException("购物车中不存在该商品");
        }
        // TODO: REST call product-service to check stock
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        clearCache(userId);
        return Result.success("数量已更新");
    }

    @Override
    public Result<?> updateSelected(Long userId, Long productId, Integer selected) {
        Cart cart = cartMapper.selectByUserAndProduct(userId, productId);
        if (cart == null) {
            throw new BusinessException("购物车中不存在该商品");
        }
        cart.setSelected(selected);
        cartMapper.updateById(cart);
        clearCache(userId);
        return Result.success("已更新");
    }

    @Override
    public Result<CartVO> list(Long userId) {
        // 1. 尝试从 Redis 缓存读取
        String cacheKey = CART_PREFIX + userId;
        if (redisTemplate != null) {
            @SuppressWarnings("unchecked")
            List<CartVO.CartItemVO> cachedItems = (List<CartVO.CartItemVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedItems != null) {
                CartVO cachedVO = CartVO.builder()
                        .items(cachedItems)
                        .totalPrice(calculateTotal(cachedItems))
                        .totalCount(cachedItems.stream().mapToInt(CartVO.CartItemVO::getQuantity).sum())
                        .build();
                return Result.success(cachedVO);
            }
        }

        // 2. 从 MySQL 查询
        // TODO: microservice — batch-fetch product info from product-service
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartVO.CartItemVO> items = cartList.stream().map(cart -> {
            String productTitle = "商品" + cart.getProductId();
            String productImage = "";
            BigDecimal price = BigDecimal.ZERO;
            int stock = 99;
            return CartVO.CartItemVO.builder()
                    .id(cart.getId())
                    .productId(cart.getProductId())
                    .productTitle(productTitle)
                    .productImage(productImage)
                    .price(price)
                    .quantity(cart.getQuantity())
                    .selected(cart.getSelected())
                    .stock(stock)
                    .build();
        }).collect(Collectors.toList());

        // 3. 写入 Redis 缓存（如果可用）
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, items, CART_TTL);
        }

        CartVO vo = CartVO.builder()
                .items(items)
                .totalPrice(calculateTotal(items))
                .totalCount(items.stream().mapToInt(CartVO.CartItemVO::getQuantity).sum())
                .build();
        return Result.success(vo);
    }

    @Override
    @Transactional
    public void clearSelected(Long userId) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<Long> selectedIds = cartList.stream()
                .filter(c -> c.getSelected() == 1)
                .map(Cart::getId)
                .collect(Collectors.toList());
        if (!selectedIds.isEmpty()) {
            cartMapper.deleteBatchIds(selectedIds);
        }
        clearCache(userId);
        log.info("已清空选中购物车项: userId={}, count={}", userId, selectedIds.size());
    }

    @Override
    @Transactional
    public void mergeCart(Long userId, String anonymousCartKey) {
        if (redisTemplate == null) return;
        String anonymousKey = ANONYMOUS_CART_PREFIX + anonymousCartKey;
        @SuppressWarnings("unchecked")
        Map<Long, Integer> anonymousCart = (Map<Long, Integer>) redisTemplate.opsForValue().get(anonymousKey);
        if (anonymousCart != null && !anonymousCart.isEmpty()) {
            for (Map.Entry<Long, Integer> entry : anonymousCart.entrySet()) {
                CartItemDTO dto = new CartItemDTO();
                dto.setProductId(entry.getKey());
                dto.setQuantity(entry.getValue());
                try {
                    addItem(userId, dto);
                } catch (Exception e) {
                    log.warn("合并购物车项失败: productId={}, error={}", entry.getKey(), e.getMessage());
                }
            }
            redisTemplate.delete(anonymousKey);
            log.info("购物车合并完成: userId={}, mergedItems={}", userId, anonymousCart.size());
        }
    }

    // ==================== 私有辅助方法 ====================

    private void clearCache(Long userId) {
        if (redisTemplate != null) {
            redisTemplate.delete(CART_PREFIX + userId);
        }
    }

    private BigDecimal calculateTotal(List<CartVO.CartItemVO> items) {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .filter(item -> item.getSelected() == 1)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
