package com.zhuanbaomao.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 购物车操作请求体
 */
@Data
public class CartItemDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;

    /** 是否选中: 0-否, 1-是 */
    private Integer selected = 1;
}
