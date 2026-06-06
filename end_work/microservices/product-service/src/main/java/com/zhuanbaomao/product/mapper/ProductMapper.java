package com.zhuanbaomao.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbaomao.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 增加浏览量（原子操作）
     */
    @Update("UPDATE product SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 扣减库存（原子操作，防超卖）
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 恢复库存（取消订单时）
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}
