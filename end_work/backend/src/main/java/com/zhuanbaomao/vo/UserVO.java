package com.zhuanbaomao.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回体（脱敏）
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private Integer role;
    private Integer status;
    private Integer points;
    private LocalDateTime lastLoginAt;

    /** JWT Access Token */
    private String token;

    /** JWT Refresh Token */
    private String refreshToken;
}
