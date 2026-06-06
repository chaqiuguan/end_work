package com.zhuanbaomao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private BigDecimal discount;
    private BigDecimal minAmount;
    private Integer totalStock;
    private Integer remainStock;
    private Integer expireDays;
    private Integer status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
