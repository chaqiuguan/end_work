package com.zhuanbaomao.common;

/**
 * 通用状态码常量
 */
public interface ResultCode {

    int SUCCESS = 200;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int CONFLICT = 409;
    int INTERNAL_ERROR = 500;

    // 业务错误码 (4位数便于前端处理)
    int USER_NOT_FOUND = 1001;
    int PASSWORD_ERROR = 1002;
    int USER_BANNED = 1003;
    int TOKEN_EXPIRED = 1004;

    int PRODUCT_NOT_FOUND = 2001;
    int PRODUCT_OFF_SHELF = 2002;
    int STOCK_INSUFFICIENT = 2003;

    int ORDER_NOT_FOUND = 3001;
    int ORDER_STATUS_ERROR = 3002;
    int ORDER_DUPLICATE = 3003;

    int CART_ITEM_EXISTS = 4001;
}
