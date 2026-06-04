package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.Review;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.mapper.ReviewMapper;
import com.zhuanbaomao.mapper.UserMapper;
import com.zhuanbaomao.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    /** 提交评价 */
    @PostMapping("/submit")
    public Result<?> submit(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        String content = (String) body.get("content");
        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : 5;
        String images = body.get("images") != null ? body.get("images").toString() : null;

        if (content == null || content.isBlank()) throw new BusinessException("评价内容不能为空");

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setContent(content);
        review.setRating(rating);
        review.setImages(images);
        reviewMapper.insert(review);
        return Result.success("评价成功");
    }

    /** 商品评价列表 */
    @GetMapping("/list/{productId}")
    public Result<?> list(@PathVariable Long productId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Page<Review> reviewPage = reviewMapper.selectPage(new Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .orderByDesc(Review::getCreatedAt));

        List<Map<String, Object>> records = reviewPage.getRecords().stream().map(r -> {
            User u = userMapper.selectById(r.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("content", r.getContent());
            m.put("rating", r.getRating());
            m.put("images", r.getImages());
            m.put("createdAt", r.getCreatedAt());
            m.put("userName", u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "匿名");
            m.put("avatar", u != null ? u.getAvatar() : null);
            return m;
        }).collect(Collectors.toList());

        return Result.success(Map.of("page", reviewPage.getCurrent(), "size", reviewPage.getSize(),
                "total", reviewPage.getTotal(), "records", records));
    }

    /** 删除评价（管理员） */
    @DeleteMapping("/admin/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) throw new BusinessException("评价不存在");
        reviewMapper.deleteById(id);
        return Result.success("评价已删除");
    }
}
