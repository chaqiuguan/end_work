package com.zhuanbaomao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（雪花ID） */
    private String orderNo;

    /** 买家用户ID */
    private Long userId;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 运费 */
    private BigDecimal freight;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 支付方式: 0-模拟支付, 1-微信, 2-支付宝 */
    private Integer payType;

    /** 订单状态: 0-待付款, 1-待发货, 2-待收货, 3-已完成, 4-已取消 */
    private Integer status;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 收货地址快照 */
    private String receiverAddress;

    /** 买家备注 */
    private String remark;

    /** 支付时间 */
    private LocalDateTime paidAt;

    /** 发货时间 */
    private LocalDateTime shippedAt;

    /** 收货时间 */
    private LocalDateTime receivedAt;

    /** 取消时间 */
    private LocalDateTime canceledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
