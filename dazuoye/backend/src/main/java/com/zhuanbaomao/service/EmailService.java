package com.zhuanbaomao.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码到指定邮箱
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
}
