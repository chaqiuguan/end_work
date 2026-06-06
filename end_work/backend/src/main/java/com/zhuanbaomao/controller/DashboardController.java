package com.zhuanbaomao.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;

    /** 核心数据统计 */
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();

        long totalUsers = userMapper.selectCount(null);
        long totalOrders = orderMapper.selectCount(null);
        long totalProducts = productMapper.selectCount(null);

        // 总销售额（已完成订单）
        List<com.zhuanbaomao.entity.Order> allOrders = orderMapper.selectList(null);
        BigDecimal totalSales = allOrders.stream()
                .filter(o -> o.getStatus() == 3)
                .map(com.zhuanbaomao.entity.Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日统计
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
                .count();
        BigDecimal todaySales = allOrders.stream()
                .filter(o -> o.getStatus() == 3 && o.getPaidAt() != null && o.getPaidAt().isAfter(todayStart))
                .map(com.zhuanbaomao.entity.Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("totalUsers", totalUsers);
        data.put("totalOrders", totalOrders);
        data.put("totalProducts", totalProducts);
        data.put("totalSales", totalSales);
        data.put("todayOrders", todayOrders);
        data.put("todaySales", todaySales);

        return Result.success(data);
    }

    /** 订单状态统计 */
    @GetMapping("/order-status")
    public Result<?> orderStatus() {
        List<com.zhuanbaomao.entity.Order> orders = orderMapper.selectList(null);
        Map<Integer, Long> statusCount = new HashMap<>();
        for (int i = 0; i <= 4; i++) {
            final int s = i;
            statusCount.put(i, orders.stream().filter(o -> o.getStatus() == s).count());
        }
        return Result.success(statusCount);
    }

    /** 热销商品排行 */
    @GetMapping("/hot-products")
    public Result<?> hotProducts() {
        var all = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zhuanbaomao.entity.Product>()
                        .orderByDesc(com.zhuanbaomao.entity.Product::getViewCount)
                        .last("LIMIT 10"));
        var list = all.stream().map(p -> Map.of(
                "id", p.getId(), "title", p.getTitle(),
                "price", p.getPrice(), "viewCount", p.getViewCount()
        )).collect(java.util.stream.Collectors.toList());
        return Result.success(list);
    }
}
