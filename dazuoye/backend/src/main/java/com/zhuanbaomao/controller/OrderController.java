package com.zhuanbaomao.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.OrderCreateDTO;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.service.OrderService;
import com.zhuanbaomao.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
}
