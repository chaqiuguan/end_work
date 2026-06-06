package com.zhuanbaomao.order.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.OrderCreateDTO;
import com.zhuanbaomao.entity.Order;
import com.zhuanbaomao.order.mapper.OrderMapper;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.order.service.OrderService;
import com.zhuanbaomao.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    /**
     * 创建订单（从购物车生成）
     *
     * <pre>
     * POST /api/order/create
     * Body: { "cartItemIds": [1, 2], "addressId": 1, "remark": "请尽快发货" }
     * </pre>
     */
    @PostMapping("/create")
    public Result<OrderVO> create(@CurrentUser Long userId, @Valid @RequestBody OrderCreateDTO dto) {
        return orderService.createOrder(userId, dto);
    }

    /**
     * 模拟支付
     */
    @PostMapping("/{id}/pay")
    public Result<?> pay(@CurrentUser Long userId, @PathVariable Long id) {
        return orderService.pay(userId, id);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@CurrentUser Long userId, @PathVariable Long id) {
        return orderService.cancel(userId, id);
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/confirm")
    public Result<?> confirmReceived(@CurrentUser Long userId, @PathVariable Long id) {
        return orderService.confirmReceived(userId, id);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@CurrentUser Long userId, @PathVariable Long id) {
        return orderService.detail(userId, id);
    }

    /**
     * 用户订单列表（分页）
     *
     * <pre>
     * GET /api/order/list?status=0&page=1&size=10
     * status: -1=全部, 0=待付款, 1=待发货, 2=待收货, 3=已完成, 4=已取消
     * </pre>
     */
    @GetMapping("/list")
    public Result<?> list(@CurrentUser Long userId,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        return orderService.listByUser(userId, status, page, size);
    }

    /** 申请退款 */
    @PostMapping("/{id}/refund")
    public Result<?> refund(@CurrentUser Long userId, @PathVariable Long id, @RequestBody Map<String, String> body) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException(3001, "订单不存在");
        if (order.getStatus() != 2 && order.getStatus() != 3)
            throw new BusinessException(3002, "仅待收货/已完成订单可申请退款");

        order.setStatus(4);
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("退款申请已提交");
    }

    /** 查看物流（模拟） */
    @GetMapping("/{id}/logistics")
    public Result<?> logistics(@CurrentUser Long userId, @PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException(3001, "订单不存在");
        return Result.success(Map.of(
            "orderNo", order.getOrderNo(),
            "status", "运输中",
            "carrier", "顺丰速运",
            "trackingNo", "SF" + order.getOrderNo().substring(0, 12),
            "nodes", java.util.List.of(
                Map.of("time", order.getShippedAt() != null ? order.getShippedAt().toString() : "待发货", "desc", "已发货"),
                Map.of("time", "运输中", "desc", "正在派送")
            )
        ));
    }
}
