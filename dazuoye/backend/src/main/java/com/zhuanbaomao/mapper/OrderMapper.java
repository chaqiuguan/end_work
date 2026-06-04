package com.zhuanbaomao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户订单（分页）
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    IPage<Order> selectByUserId(Page<Order> page, @Param("userId") Long userId);

    /**
     * 查询待支付且已过期的订单
     */
    @Select("SELECT * FROM orders WHERE status = 0 AND deleted = 0 AND created_at < #{expireTime}")
    java.util.List<Order> selectExpiredOrders(@Param("expireTime") java.time.LocalDateTime expireTime);
}
