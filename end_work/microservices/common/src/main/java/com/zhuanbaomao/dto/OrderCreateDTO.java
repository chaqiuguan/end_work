package com.zhuanbaomao.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 订单创建请求体
 */
@Data
public class OrderCreateDTO {

    /** 需要结算的购物车项ID列表 */
    @NotEmpty(message = "结算商品不能为空")
    private List<Long> cartItemIds;

    /** 收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 买家备注 */
    private String remark;

    /** 支付方式: 0-模拟支付 */
    private Integer payType = 0;
}
