package com.zhuanbaomao.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbaomao.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名或手机号或邮箱查询用户
     */
    @Select("SELECT * FROM user WHERE (username = #{account} OR phone = #{account} OR email = #{account}) AND deleted = 0")
    User selectByAccount(@Param("account") String account);

    /**
     * 根据用户名查询（去重校验）
     */
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);
}
