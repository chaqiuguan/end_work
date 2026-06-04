package com.zhuanbaomao.service.impl;

import com.zhuanbaomao.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现 —— 发送验证码
 *
 * <p>如果邮件发送失败（如 SMTP 未配置授权码），验证码仍会输出到日志中用于开发测试。
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        if (mailSender == null) {
            log.warn("JavaMailSender 未配置，验证码仅输出到日志: code={}", code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("转鱼宝猫 - 邮箱验证码");
            message.setText(String.format(
                    "您好！\n\n" +
                    "欢迎注册转鱼宝猫电商平台！\n\n" +
                    "您的验证码是：%s\n" +
                    "验证码有效期为 5 分钟，请尽快使用。\n\n" +
                    "如非本人操作，请忽略此邮件。\n\n" +
                    "—— 转鱼宝猫团队",
                    code
            ));

            mailSender.send(message);
            log.info("验证码已发送到: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", to, e.getMessage());
            log.info("=== 验证码（开发模式）: {} -> {} ===", to, code);
            throw new RuntimeException("邮件发送失败，请检查SMTP配置。163邮箱需使用「授权码」而非登录密码。验证码已输出到服务器日志。", e);
        }
    }
}
