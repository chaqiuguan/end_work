package com.zhuanbaomao.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.Coupon;
import com.zhuanbaomao.entity.UserCoupon;
import com.zhuanbaomao.order.mapper.CouponMapper;
import com.zhuanbaomao.order.mapper.UserCouponMapper;
import com.zhuanbaomao.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    /** 可领取的优惠券列表 */
    @GetMapping("/list")
    public Result<?> list(@CurrentUser Long userId) {
        List<Coupon> all = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1).gt(Coupon::getRemainStock, 0));
        // 获取用户已领取的
        List<Long> claimed = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId))
                .stream().map(UserCoupon::getCouponId).collect(Collectors.toList());

        List<Map<String,Object>> result = all.stream().map(c -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id",c.getId()); m.put("name",c.getName()); m.put("discount",c.getDiscount());
            m.put("minAmount",c.getMinAmount()); m.put("remainStock",c.getRemainStock());
            m.put("expireDays",c.getExpireDays()); m.put("claimed",claimed.contains(c.getId()));
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /** 领取优惠券 */
    @PostMapping("/claim/{couponId}")
    public Result<?> claim(@CurrentUser Long userId, @PathVariable Long couponId) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null || c.getStatus() == 0 || c.getRemainStock() <= 0)
            throw new BusinessException("优惠券不可用");

        long already = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId));
        if (already > 0) throw new BusinessException("已领取过该优惠券");

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId); uc.setCouponId(couponId); uc.setUsed(0);
        uc.setExpireAt(LocalDateTime.now().plusDays(c.getExpireDays()));
        userCouponMapper.insert(uc);

        c.setRemainStock(c.getRemainStock() - 1);
        couponMapper.updateById(c);
        return Result.success("领取成功");
    }

    /** 我的优惠券 */
    @GetMapping("/my")
    public Result<?> my(@CurrentUser Long userId) {
        List<UserCoupon> ucs = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId));
        List<Map<String,Object>> result = ucs.stream().map(uc -> {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            Map<String,Object> m = new HashMap<>();
            m.put("id",uc.getId()); m.put("used",uc.getUsed()); m.put("expireAt",uc.getExpireAt());
            if (c != null) { m.put("name",c.getName()); m.put("discount",c.getDiscount()); m.put("minAmount",c.getMinAmount()); }
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /** 管理员：优惠券CRUD */
    @GetMapping("/admin/list")
    public Result<?> adminList() { return Result.success(couponMapper.selectList(null)); }
    @PostMapping("/admin/add")
    public Result<?> adminAdd(@RequestBody Coupon c) { couponMapper.insert(c); return Result.success("添加成功"); }
    @PutMapping("/admin/{id}")
    public Result<?> adminUpdate(@PathVariable Long id, @RequestBody Coupon c) { c.setId(id); couponMapper.updateById(c); return Result.success("更新成功"); }
    @DeleteMapping("/admin/{id}")
    public Result<?> adminDelete(@PathVariable Long id) { couponMapper.deleteById(id); return Result.success("已删除"); }
}
