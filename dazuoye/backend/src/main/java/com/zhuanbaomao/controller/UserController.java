package com.zhuanbaomao.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.LoginDTO;
import com.zhuanbaomao.dto.RegisterDTO;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.service.EmailService;
import com.zhuanbaomao.service.UserService;
import com.zhuanbaomao.service.VerificationCodeService;
import com.zhuanbaomao.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 发送邮箱验证码
     *
     * <pre>
     * POST /api/user/send-code
     * Body: { "email": "user@example.com" }
     * </pre>
     */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }

        // 检查发送间隔（60秒内不可重复发送）
        verificationCodeService.checkResendCooldown(email);

        // 生成6位验证码
        String code = verificationCodeService.generateAndStore(email);

        // 尝试发送邮件（如果SMTP不可用，验证码已输出到日志）
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            // 邮件发送失败，但验证码已存储，返回给用户用于开发测试
            return Result.success("邮件发送失败(" + e.getMessage() + ")，[开发模式] 验证码: " + code + "（有效5分钟）");
        }

        return Result.success("验证码已发送到 " + email + "，有效期为5分钟");
    }

    /**
     * 用户注册（仅邮箱注册 + 验证码）
     *
     * <pre>
     * POST /api/user/register
     * Body: { "username": "test", "password": "123456", "email": "user@example.com", "code": "123456" }
     * </pre>
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    /**
     * 用户登录（支持用户名/邮箱）
     *
     * <pre>
     * POST /api/user/login
     * Body: { "username": "test / user@example.com", "password": "123456" }
     * </pre>
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserVO> profile(@CurrentUser Long userId) {
        return userService.profile(userId);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public Result<UserVO> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return userService.refreshToken(refreshToken);
    }

    /**
     * 切换卖家/买家身份
     */
    @PutMapping("/switch-role")
    public Result<?> switchRole(@CurrentUser Long userId, @RequestBody Map<String, Integer> body) {
        Integer role = body.get("role");
        return userService.switchRole(userId, role);
    }
}
