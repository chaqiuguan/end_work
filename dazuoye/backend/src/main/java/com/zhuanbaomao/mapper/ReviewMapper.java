package com.zhuanbaomao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbaomao.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    @Select("SELECT * FROM review WHERE product_id = #{productId} AND deleted = 0 ORDER BY created_at DESC")
    List<Review> selectByProductId(Long productId);
}
