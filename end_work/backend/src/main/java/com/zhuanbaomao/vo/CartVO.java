package com.zhuanbaomao.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车返回体
 */
@Data
@Builder
public class CartVO {

    /** 购物车项列表 */
    private List<CartItemVO> items;

    /** 总金额 */
    private BigDecimal totalPrice;

    /** 总数量 */
    private Integer totalCount;

    /**
     * 单个购物车项
     */
    @Data
    @Builder
    public static class CartItemVO {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private Integer selected;
        private Integer stock; // 商品当前库存
    }
}
