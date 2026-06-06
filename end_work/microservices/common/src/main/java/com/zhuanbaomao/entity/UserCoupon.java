package com.zhuanbaomao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long couponId;
    private Integer used;
    private LocalDateTime expireAt;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
