package com.zhuanbaomao.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbaomao.common.PageResult;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.ProductQueryDTO;
import com.zhuanbaomao.entity.Category;
import com.zhuanbaomao.entity.Product;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.mapper.CategoryMapper;
import com.zhuanbaomao.mapper.ProductMapper;
import com.zhuanbaomao.mapper.UserMapper;
import com.zhuanbaomao.service.ProductService;
import com.zhuanbaomao.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    @Override
    public Result<?> list(ProductQueryDTO query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询已上架的商品
        wrapper.eq(Product::getStatus, 1);

        // 关键字搜索
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Product::getTitle, query.getKeyword())
                    .or()
                    .like(Product::getDescription, query.getKeyword()));
        }

        // 分类筛选
        if (query.getCategoryId() != null && query.getCategoryId() > 0) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }

        // 价格区间
        if (query.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null && query.getMaxPrice().compareTo(BigDecimal.ZERO) > 0) {
            wrapper.le(Product::getPrice, query.getMaxPrice());
        }

        // 成色筛选
        if (query.getCondition() != null) {
            wrapper.eq(Product::getCondition, query.getCondition());
        }

        // 排序
        String sortBy = StrUtil.blankToDefault(query.getSortBy(), "created_desc");
        switch (sortBy) {
            case "price_asc":
                wrapper.orderByAsc(Product::getPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Product::getPrice);
                break;
            case "view_desc":
                wrapper.orderByDesc(Product::getViewCount);
                break;
            case "created_desc":
            default:
                wrapper.orderByDesc(Product::getCreatedAt);
                break;
        }

        // 分页查询
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        IPage<Product> productPage = productMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<ProductVO> records = productPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<ProductVO> result = new PageResult<>(
                productPage.getCurrent(),
                productPage.getSize(),
                productPage.getTotal(),
                productPage.getPages(),
                records
        );

        return Result.success(result);
    }

    @Override
    public Result<ProductVO> detail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(2001, "商品不存在");
        }

        if (product.getStatus() != 1) {
            throw new BusinessException(2002, "商品已下架或待审核");
        }

        // 原子增加浏览量
        productMapper.incrementViewCount(productId);

        ProductVO vo = toVO(product);
        return Result.success(vo);
    }

    @Override
    @Transactional
    public Result<?> publish(Long sellerId, ProductVO vo) {
        // 校验卖家身份
        User user = userMapper.selectById(sellerId);
        if (user == null || user.getRole() < 1) {
            throw new BusinessException("仅卖家可发布商品，请先切换为卖家身份");
        }

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCategoryId(vo.getCategoryId() != null ? vo.getCategoryId() : 0L);
        product.setTitle(vo.getTitle());
        product.setDescription(vo.getDescription());
        product.setImages(vo.getImages() != null ? JSONUtil.toJsonStr(vo.getImages()) : "[]");
        product.setOriginalPrice(vo.getOriginalPrice() != null ? vo.getOriginalPrice() : BigDecimal.ZERO);
        product.setPrice(vo.getPrice());
        product.setCondition(vo.getCondition() != null ? vo.getCondition() : 0);
        product.setStatus(0); // 默认待审核
        product.setStock(vo.getStock() != null ? vo.getStock() : 1);

        productMapper.insert(product);
        log.info("商品发布成功: productId={}, title={}", product.getId(), product.getTitle());

        return Result.success("商品发布成功，等待审核", product.getId());
    }

    @Override
    @Transactional
    public Result<?> update(Long sellerId, Long productId, ProductVO vo) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }

        if (StrUtil.isNotBlank(vo.getTitle())) product.setTitle(vo.getTitle());
        if (StrUtil.isNotBlank(vo.getDescription())) product.setDescription(vo.getDescription());
        if (vo.getImages() != null) product.setImages(JSONUtil.toJsonStr(vo.getImages()));
        if (vo.getPrice() != null) product.setPrice(vo.getPrice());
        if (vo.getOriginalPrice() != null) product.setOriginalPrice(vo.getOriginalPrice());
        if (vo.getCondition() != null) product.setCondition(vo.getCondition());
        if (vo.getStock() != null) product.setStock(vo.getStock());
        if (vo.getCategoryId() != null) product.setCategoryId(vo.getCategoryId());

        productMapper.updateById(product);
        return Result.success("商品更新成功");
    }

    @Override
    public Result<?> offShelf(Long sellerId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }

        product.setStatus(2); // 下架
        productMapper.updateById(product);
        return Result.success("商品已下架");
    }

    // ==================== 私有辅助方法 ====================

    private ProductVO toVO(Product product) {
        // 查询分类名称
        String categoryName = "";
        if (product.getCategoryId() != null && product.getCategoryId() > 0) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 查询卖家昵称
        String sellerName = "";
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            sellerName = seller.getNickname() != null ? seller.getNickname() : seller.getUsername();
        }

        // 解析图片列表
        List<String> imageList = List.of();
        if (StrUtil.isNotBlank(product.getImages())) {
            try {
                imageList = JSONUtil.toList(product.getImages(), String.class);
            } catch (Exception ignored) {}
        }

        // 解析标签
        List<String> tagList = List.of();
        if (StrUtil.isNotBlank(product.getTags())) {
            try {
                tagList = JSONUtil.toList(product.getTags(), String.class);
            } catch (Exception ignored) {}
        }

        // 成色文本
        int cond = product.getCondition() != null ? product.getCondition() : 0;
        String conditionText;
        switch (cond) {
            case 0: conditionText = "全新"; break;
            case 1: conditionText = "几乎全新"; break;
            case 2: conditionText = "轻微使用"; break;
            case 3: conditionText = "明显使用"; break;
            default: conditionText = "未知"; break;
        }

        return ProductVO.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .sellerName(sellerName)
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .title(product.getTitle())
                .description(product.getDescription())
                .images(imageList)
                .originalPrice(product.getOriginalPrice())
                .price(product.getPrice())
                .condition(product.getCondition())
                .conditionText(conditionText)
                .status(product.getStatus())
                .stock(product.getStock())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .tags(tagList)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
