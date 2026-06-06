package com.zhuanbaomao.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.dto.LoginDTO;
import com.zhuanbaomao.dto.RegisterDTO;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.user.mapper.UserMapper;
import com.zhuanbaomao.security.JwtTokenProvider;
import com.zhuanbaomao.user.service.UserService;
import com.zhuanbaomao.user.service.VerificationCodeService;
import com.zhuanbaomao.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeService verificationCodeService;

    @Override
    @Transactional
    public Result<?> register(RegisterDTO dto) {
        // 1. 校验两次密码一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 校验验证码
        if (!verificationCodeService.verify(dto.getEmail(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 3. 校验用户名是否已存在
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 4. 校验邮箱唯一性
        long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (emailCount > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getUsername()); // 默认昵称=用户名
        user.setRole(0);
        user.setStatus(1);
        user.setPoints(0);

        userMapper.insert(user);
        log.info("用户注册成功: username={}, email={}, userId={}", dto.getUsername(), dto.getEmail(), user.getId());

        return Result.success("注册成功");
    }

    @Override
    public Result<UserVO> login(LoginDTO dto) {
        // 1. 查询用户（支持用户名/邮箱登录）
        User user = userMapper.selectByAccount(dto.getUsername());
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(1002, "密码错误");
        }

        // 3. 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(1003, "账号已被封禁，请联系管理员");
        }

        // 4. 生成 Token
        String accessToken = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getUsername());

        // 5. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 构建返回体
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .points(user.getPoints())
                .lastLoginAt(user.getLastLoginAt())
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return Result.success("登录成功", userVO);
    }

    @Override
    public Result<UserVO> profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .points(user.getPoints())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        return Result.success(userVO);
    }

    @Override
    public Result<UserVO> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(1004, "Refresh Token无效或已过期，请重新登录");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(1004, "用户状态异常");
        }

        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getUsername());

        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return Result.success("令牌刷新成功", userVO);
    }

    @Override
    public Result<?> switchRole(Long userId, Integer role) {
        if (role != 0 && role != 1) {
            throw new BusinessException("无效的身份类型");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        user.setRole(role);
        userMapper.updateById(user);

        log.info("用户身份切换: userId={}, newRole={}", userId, role);
        return Result.success(role == 1 ? "已切换为卖家身份" : "已切换为买家身份");
    }
}
