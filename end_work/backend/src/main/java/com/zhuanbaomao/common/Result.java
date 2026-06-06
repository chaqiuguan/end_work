package com.zhuanbaomao.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    // ==================== 成功响应 ====================

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, System.currentTimeMillis());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    // ==================== 失败响应 ====================

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null, System.currentTimeMillis());
    }

    // ==================== 常用快捷方法 ====================

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或令牌已过期", null, System.currentTimeMillis());
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(403, "无权限访问", null, System.currentTimeMillis());
    }

    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null, System.currentTimeMillis());
    }
}
