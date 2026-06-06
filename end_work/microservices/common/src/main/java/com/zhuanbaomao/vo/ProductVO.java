package com.zhuanbaomao.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品信息返回体
 */
@Data
@Builder
public class ProductVO {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;

    /** 商品图片URL列表 */
    private List<String> images;

    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer condition;
    private String conditionText;
    private Integer status;
    private Integer stock;
    private Integer viewCount;
    private Integer favoriteCount;
    private List<String> tags;
    private Map<String, String> specs;

    private LocalDateTime createdAt;
}
