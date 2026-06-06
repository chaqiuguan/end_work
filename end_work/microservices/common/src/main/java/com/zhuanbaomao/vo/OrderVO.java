package com.zhuanbaomao.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单返回体
 */
@Data
@Builder
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal payAmount;
    private Integer payType;
    private String payTypeText;
    private Integer status;
    private String statusText;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;

    /** 订单项目列表 */
    private List<OrderItemVO> items;

    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;

    /**
     * 单个订单项
     */
    @Data
    @Builder
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;
    }
}
