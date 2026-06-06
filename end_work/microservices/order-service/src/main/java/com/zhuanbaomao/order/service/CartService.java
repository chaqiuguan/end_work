package com.zhuanbaomao.order.service;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.CartItemDTO;
import com.zhuanbaomao.vo.CartVO;

/**
 * 购物车服务接口（Redis缓存 + MySQL持久化）
 */
public interface CartService {

    /**
     * 添加商品到购物车
     */
    Result<?> addItem(Long userId, CartItemDTO dto);

    /**
     * 从购物车移除商品
     */
    Result<?> removeItem(Long userId, Long productId);

    /**
     * 更新购物车商品数量
     */
    Result<?> updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品选中状态
     */
    Result<?> updateSelected(Long userId, Long productId, Integer selected);

    /**
     * 查看购物车列表
     */
    Result<CartVO> list(Long userId);

    /**
     * 清空已选中的购物车项（下单后调用）
     */
    void clearSelected(Long userId);

    /**
     * 合并购物车：登录后将未登录时的Redis缓存迁移到MySQL
     */
    void mergeCart(Long userId, String anonymousCartKey);
}
