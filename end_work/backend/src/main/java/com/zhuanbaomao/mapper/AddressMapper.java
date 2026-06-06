package com.zhuanbaomao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbaomao.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 地址 Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 取消用户的所有默认地址
     */
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(@Param("userId") Long userId);
}
