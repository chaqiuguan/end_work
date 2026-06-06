package com.zhuanbaomao.product.service;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.ProductQueryDTO;
import com.zhuanbaomao.vo.ProductVO;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 分页查询商品列表（搜索 + 分类筛选 + 价格筛选 + 排序）
     */
    Result<?> list(ProductQueryDTO query);

    /**
     * 商品详情
     */
    Result<ProductVO> detail(Long productId);

    /**
     * 发布商品
     */
    Result<?> publish(Long sellerId, ProductVO productVO);

    /**
     * 更新商品
     */
    Result<?> update(Long sellerId, Long productId, ProductVO productVO);

    /**
     * 下架商品
     */
    Result<?> offShelf(Long sellerId, Long productId);

    /**
     * 相似商品推荐（同品类 + 模糊匹配）
     */
    Result<?> similar(Long productId);
}
