package com.zhuanbaomao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家用户ID */
    private Long sellerId;

    /** 分类ID */
    private Long categoryId;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 商品图片URL列表（JSON字符串） */
    private String images;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 转闲价（售价） */
    private BigDecimal price;

    /** 成色: 0-全新, 1-几乎全新, 2-轻微使用, 3-明显使用 */
    @TableField("`condition`")
    private Integer condition;

    /** 状态: 0-待审核, 1-已上架, 2-已下架, 3-已售出 */
    private Integer status;

    /** 库存数量 */
    private Integer stock;

    /** 浏览量 */
    private Integer viewCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 标签 (JSON字符串) */
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
