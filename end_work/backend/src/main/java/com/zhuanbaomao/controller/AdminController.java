package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.*;
import com.zhuanbaomao.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final CategoryMapper categoryMapper;

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

    /** 用户列表（搜索，脱敏） */
    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "") String keyword,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        // 脱敏：移除密码字段
        List<Map<String, Object>> safeRecords = userPage.getRecords().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("avatar", u.getAvatar());
            m.put("role", u.getRole());
            m.put("status", u.getStatus());
            m.put("points", u.getPoints());
            m.put("lastLoginAt", u.getLastLoginAt());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        return Result.success(Map.of(
            "page", userPage.getCurrent(), "size", userPage.getSize(),
            "total", userPage.getTotal(), "records", safeRecords
        ));
    }

    /** 用户详情 */
    @GetMapping("/user/detail/{id}")
    public Result<?> userDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", user.getId());
        detail.put("username", user.getUsername());
        detail.put("nickname", user.getNickname());
        detail.put("email", user.getEmail());
        detail.put("phone", user.getPhone());
        detail.put("avatar", user.getAvatar());
        detail.put("role", user.getRole());
        detail.put("status", user.getStatus());
        detail.put("points", user.getPoints());
        detail.put("lastLoginAt", user.getLastLoginAt());
        detail.put("createdAt", user.getCreatedAt());
        return Result.success(detail);
    }

    // ==================== 分类管理 ====================

    @GetMapping("/category/list")
    public Result<?> categoryList() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder)));
    }

    @PostMapping("/category/add")
    public Result<?> categoryAdd(@RequestBody Category cat) {
        categoryMapper.insert(cat);
        return Result.success("分类添加成功");
    }

    @PutMapping("/category/{id}")
    public Result<?> categoryUpdate(@PathVariable Long id, @RequestBody Category cat) {
        cat.setId(id);
        categoryMapper.updateById(cat);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/category/{id}")
    public Result<?> categoryDelete(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success("分类已删除");
    }

    // ==================== 商品删除 ====================

    @DeleteMapping("/product/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException("商品不存在");
        productMapper.deleteById(id);
        return Result.success("商品已删除");
    }

    // ==================== 订单导出 ====================

    @GetMapping("/order/export")
    public Result<?> exportOrders() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedAt));
        List<Map<String, Object>> data = orders.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("orderNo", o.getOrderNo());
            m.put("userId", o.getUserId());
            m.put("payAmount", o.getPayAmount());
            m.put("status", o.getStatus());
            m.put("receiverName", o.getReceiverName());
            m.put("receiverPhone", o.getReceiverPhone());
            m.put("createdAt", o.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.success(data);
    }
}
