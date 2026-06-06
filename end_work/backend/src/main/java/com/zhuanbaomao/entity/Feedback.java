package com.zhuanbaomao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String content;
    private String reply;
    private Integer status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
