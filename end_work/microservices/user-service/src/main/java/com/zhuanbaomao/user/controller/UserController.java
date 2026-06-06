package com.zhuanbaomao.user.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.LoginDTO;
import com.zhuanbaomao.dto.RegisterDTO;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.user.mapper.UserMapper;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.user.service.UserService;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
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
