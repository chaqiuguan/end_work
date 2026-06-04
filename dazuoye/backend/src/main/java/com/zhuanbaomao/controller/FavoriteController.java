package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.Favorite;
import com.zhuanbaomao.entity.Product;
import com.zhuanbaomao.mapper.FavoriteMapper;
import com.zhuanbaomao.mapper.ProductMapper;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.vo.ProductVO;
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
    private final ProductMapper productMapper;

    /** 收藏/取消收藏商品 */
    @PostMapping("/toggle/{productId}")
    public Result<?> toggle(@CurrentUser Long userId, @PathVariable Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

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

        var records = favPage.getRecords().stream().map(fav -> {
            Product p = productMapper.selectById(fav.getProductId());
            if (p == null || p.getDeleted() == 1) return null;
            return Map.of(
                "id", fav.getId(),
                "productId", p.getId(),
                "title", p.getTitle(),
                "price", p.getPrice(),
                "image", p.getImages() != null ? p.getImages().replaceAll("[\"\\[\\]]", "").split(",")[0].trim() : ""
            );
        }).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());

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
