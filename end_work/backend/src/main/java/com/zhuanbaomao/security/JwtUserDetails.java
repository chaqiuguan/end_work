package com.zhuanbaomao.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 中携带的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDetails {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色 */
    private Integer role;
}
