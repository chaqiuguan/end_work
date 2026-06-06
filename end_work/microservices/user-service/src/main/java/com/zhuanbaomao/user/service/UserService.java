package com.zhuanbaomao.user.service;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.LoginDTO;
import com.zhuanbaomao.dto.RegisterDTO;
import com.zhuanbaomao.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    Result<?> register(RegisterDTO dto);

    /**
     * 用户登录（支持用户名/手机号/邮箱）
     */
    Result<UserVO> login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    Result<UserVO> profile(Long userId);

    /**
     * 刷新 Token
     */
    Result<UserVO> refreshToken(String refreshToken);

    /**
     * 切换卖家/买家身份
     */
    Result<?> switchRole(Long userId, Integer role);
}
