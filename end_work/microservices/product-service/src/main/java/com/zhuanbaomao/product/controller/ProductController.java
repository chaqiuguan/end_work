package com.zhuanbaomao.product.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.ProductQueryDTO;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.product.service.ProductService;
import com.zhuanbaomao.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品列表（公开接口）：分页 + 搜索 + 筛选 + 排序
     *
     * <pre>
     * GET /api/product/list?keyword=iPhone&categoryId=1&minPrice=100&maxPrice=1000&sortBy=price_asc&page=1&size=20
     * </pre>
     */
    @GetMapping("/list")
    public Result<?> list(@ModelAttribute ProductQueryDTO query) {
        return productService.list(query);
    }

    /**
     * 商品详情（公开接口）
     */
    @GetMapping("/detail/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return productService.detail(id);
    }

    /**
     * 发布商品 —— 需要卖家身份
     */
    @PostMapping("/publish")
    public Result<?> publish(@CurrentUser Long userId, @RequestBody ProductVO vo) {
        return productService.publish(userId, vo);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<?> update(@CurrentUser Long userId, @PathVariable Long id, @RequestBody ProductVO vo) {
        return productService.update(userId, id, vo);
    }

    /**
     * 相似商品推荐（公开接口）：同品类6件
     */
    @GetMapping("/similar/{id}")
    public Result<?> similar(@PathVariable Long id) {
        return productService.similar(id);
    }

    /**
     * 下架商品
     */
    @PutMapping("/{id}/off-shelf")
    public Result<?> offShelf(@CurrentUser Long userId, @PathVariable Long id) {
        return productService.offShelf(userId, id);
    }
}
