package com.zhuanbaomao.security;

import java.lang.annotation.*;

/**
 * 自定义注解 —— 获取当前登录用户
 *
 * <pre>
 *   @PostMapping("/address")
 *   public Result<?> addAddress(@CurrentUser Long userId, @RequestBody AddressDTO dto) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
