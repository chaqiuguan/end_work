package com.zhuanbaomao.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.LoginDTO;
import com.zhuanbaomao.dto.RegisterDTO;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.mapper.UserMapper;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.service.EmailService;
import com.zhuanbaomao.service.UserService;
import com.zhuanbaomao.service.VerificationCodeService;
import com.zhuanbaomao.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

    /** 找回密码 — 发送重置验证码 */
    @PostMapping("/reset-code")
    public Result<?> sendResetCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return Result.fail(400, "邮箱不能为空");

        User user = userMapper.selectByAccount(email);
        if (user == null) throw new BusinessException("该邮箱未注册");

        verificationCodeService.checkResendCooldown(email);
        String code = verificationCodeService.generateAndStore(email);
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            return Result.success("邮件发送失败，[开发模式] 验证码: " + code);
        }
        return Result.success("重置密码验证码已发送");
    }

    /** 重置密码 */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");

        if (email == null || code == null || newPassword == null)
            return Result.fail(400, "参数不完整");
        if (newPassword.length() < 6)
            return Result.fail(400, "密码至少6位");

        User user = userMapper.selectByAccount(email);
        if (user == null) throw new BusinessException("用户不存在");

        if (!verificationCodeService.verify(email, code))
            throw new BusinessException("验证码错误或已过期");

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("密码重置成功，请重新登录");
    }

    /** 修改个人信息 */
    @PutMapping("/profile")
    public Result<?> updateProfile(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));

        // 修改密码需要旧密码验证
        if (body.containsKey("oldPassword") && body.containsKey("newPassword")) {
            if (!passwordEncoder.matches(body.get("oldPassword"), user.getPassword()))
                throw new BusinessException("旧密码错误");
            if (body.get("newPassword").length() < 6)
                throw new BusinessException("新密码至少6位");
            user.setPassword(passwordEncoder.encode(body.get("newPassword")));
        }

        userMapper.updateById(user);
        return Result.success("个人信息已更新");
    }
}
