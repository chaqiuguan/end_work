package com.zhuanbaomao.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询请求体
 */
@Data
public class ProductQueryDTO {

    /** 关键字搜索 */
    private String keyword;

    /** 分类ID */
    private Long categoryId;

    /** 最低价格 */
    private BigDecimal minPrice;

    /** 最高价格 */
    private BigDecimal maxPrice;

    /** 成色: 0-全新, 1-几乎全新, 2-轻微使用, 3-明显使用 */
    private Integer condition;

    /** 排序方式: price_asc, price_desc, created_desc, sales_desc */
    private String sortBy = "created_desc";

    /** 当前页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 20;
}
