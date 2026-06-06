package com.zhuanbaomao.service;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.OrderCreateDTO;
import com.zhuanbaomao.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单（分布式锁防超卖）
     */
    Result<OrderVO> createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 模拟支付
     */
    Result<?> pay(Long userId, Long orderId);

    /**
     * 取消订单
     */
    Result<?> cancel(Long userId, Long orderId);

    /**
     * 确认收货
     */
    Result<?> confirmReceived(Long userId, Long orderId);

    /**
     * 订单详情
     */
    Result<OrderVO> detail(Long userId, Long orderId);

    /**
     * 用户订单列表（分页）
     */
    Result<?> listByUser(Long userId, Integer status, Integer page, Integer size);
}
