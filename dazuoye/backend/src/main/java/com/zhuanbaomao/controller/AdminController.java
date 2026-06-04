package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.Order;
import com.zhuanbaomao.entity.Product;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.mapper.OrderItemMapper;
import com.zhuanbaomao.mapper.OrderMapper;
import com.zhuanbaomao.mapper.ProductMapper;
import com.zhuanbaomao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理后台控制器 —— 简易版（需管理员权限）
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    // ==================== 商品审核 ====================

    /**
     * 查询待审核商品列表
     */
    @GetMapping("/product/pending")
    public Result<?> pendingProducts(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        Page<Product> productPage = productMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0)
                        .orderByDesc(Product::getCreatedAt));
        return Result.success(productPage);
    }

    /**
     * 审核通过商品
     */
    @PostMapping("/product/{id}/approve")
    public Result<?> approveProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new BusinessException("商品不存在");
        product.setStatus(1); // 上架
        productMapper.updateById(product);
        return Result.success("商品已审核通过");
    }

    /**
     * 审核拒绝商品
     */
    @PostMapping("/product/{id}/reject")
    public Result<?> rejectProduct(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new BusinessException("商品不存在");
        product.setStatus(2); // 下架（拒绝）
        productMapper.updateById(product);
        return Result.success("已拒绝, 原因: " + body.getOrDefault("reason", "未说明"));
    }

    // ==================== 订单管理 ====================

    /**
     * 发货
     */
    @PostMapping("/order/{id}/ship")
    public Result<?> shipOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 1) throw new BusinessException("仅待发货订单可发货");

        order.setStatus(2); // 待收货
        order.setShippedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("发货成功");
    }

    /**
     * 退款处理
     */
    @PostMapping("/order/{id}/refund")
    public Result<?> refundOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");

        // 恢复库存
        orderItemMapper.selectByOrderId(order.getId()).forEach(item ->
                productMapper.restoreStock(item.getProductId(), item.getQuantity()));

        order.setStatus(4); // 已取消（退款）
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("退款成功，库存已恢复");
    }

    // ==================== 用户管理 ====================

    /**
     * 封禁用户
     */
    @PostMapping("/user/{id}/ban")
    public Result<?> banUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(0);
        userMapper.updateById(user);
        return Result.success("用户已封禁");
    }

    /**
     * 解封用户
     */
    @PostMapping("/user/{id}/unban")
    public Result<?> unbanUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(1);
        userMapper.updateById(user);
        return Result.success("用户已解封");
    }
}
