package com.zhuanbaomao.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.entity.Favorite;
import com.zhuanbaomao.user.mapper.FavoriteMapper;
import com.zhuanbaomao.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品收藏控制器
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;
    // ProductMapper is in product-service; use REST call in production

    /** 收藏/取消收藏商品 */
    @PostMapping("/toggle/{productId}")
    public Result<?> toggle(@CurrentUser Long userId, @PathVariable Long productId) {
        // TODO: 微服务中通过 REST 调用 product-service 验证商品存在

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        Favorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.success("已取消收藏");
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            return Result.success("已收藏");
        }
    }

    /** 查看收藏列表 */
    @GetMapping("/list")
    public Result<?> list(@CurrentUser Long userId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size) {
        Page<Favorite> favPage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt));

        // TODO: 微服务中批量调用 product-service 获取商品信息
        var records = favPage.getRecords().stream().map(fav -> {
            return Map.of(
                "id", fav.getId(),
                "productId", fav.getProductId(),
                "title", "商品" + fav.getProductId(),
                "price", 0,
                "image", ""
            );
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(Map.of(
            "page", favPage.getCurrent(),
            "size", favPage.getSize(),
            "total", favPage.getTotal(),
            "records", records
        ));
    }

    /** 检查是否已收藏 */
    @GetMapping("/check/{productId}")
    public Result<?> check(@CurrentUser Long userId, @PathVariable Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        return Result.success(favoriteMapper.selectCount(wrapper) > 0);
    }
}
