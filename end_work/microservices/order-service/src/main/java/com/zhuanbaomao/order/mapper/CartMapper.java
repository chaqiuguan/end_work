package com.zhuanbaomao.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbaomao.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 购物车 Mapper
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID和商品ID查询购物车项
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Cart selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 查询用户购物车（带商品信息）
     */
    @Select("SELECT c.* FROM cart c WHERE c.user_id = #{userId} ORDER BY c.created_at DESC")
    List<Cart> selectByUserId(@Param("userId") Long userId);

    /**
     * 合并购物车 —— 将未登录时的Redis缓存迁移到MySQL
     */
    default void mergeCart(Long userId, List<Cart> redisCartItems) {
        // 由 Service 层实现合并逻辑
    }
}
