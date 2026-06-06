package com.zhuanbaomao.config;

import com.zhuanbaomao.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF（前后端分离 + JWT）
            .csrf().disable()
            // 无状态会话
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 请求权限配置
            .authorizeRequests()
            // 公开接口
            .antMatchers(
                "/user/register",
                "/user/login",
                "/user/send-code",
                "/user/reset-code",
                "/user/reset-password",
                "/user/refresh-token",
                "/product/list",
                "/product/detail/**",
                "/review/list/**",
                "/category/**",
                "/banner/list",
                "/announcement/list",
                "/coupon/list",
                "/seckill/list"
            ).permitAll()
            // OPTIONS 预检请求
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 静态资源
            .antMatchers("/uploads/**").permitAll()
            // 管理后台仅管理员可访问
            .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            // 其余接口需要认证
            .anyRequest().authenticated()
            .and()
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 异常处理
            .exceptionHandling()
            .authenticationEntryPoint((req, resp, ex) -> {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(401);
                resp.getWriter().write("{\"code\":401,\"message\":\"未登录或令牌已过期\"}");
            })
            .accessDeniedHandler((req, resp, ex) -> {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(403);
                resp.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
            });

        return http.build();
    }
}
