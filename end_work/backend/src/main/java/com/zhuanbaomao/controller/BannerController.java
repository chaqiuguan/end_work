package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.entity.Banner;
import com.zhuanbaomao.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BannerController {

    private final BannerMapper bannerMapper;

    /** 公开：获取显示的轮播图（缓存30分钟） */
    @Cacheable(value = "banners", key = "'public'")
    @GetMapping("/banner/list")
    public Result<List<Banner>> publicList() {
        return Result.success(bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().eq(Banner::getStatus, 1).orderByAsc(Banner::getSortOrder)));
    }

    /** 管理员：全部轮播图 */
    @GetMapping("/admin/banner/list")
    public Result<List<Banner>> adminList() {
        return Result.success(bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder)));
    }

    /** 管理员：新增 */
    @CacheEvict(value = "banners", allEntries = true)
    @PostMapping("/admin/banner/add")
    public Result<?> add(@RequestBody Banner b) { bannerMapper.insert(b); return Result.success("添加成功"); }

    /** 管理员：更新 */
    @CacheEvict(value = "banners", allEntries = true)
    @PutMapping("/admin/banner/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Banner b) { b.setId(id); bannerMapper.updateById(b); return Result.success("更新成功"); }

    /** 管理员：删除 */
    @CacheEvict(value = "banners", allEntries = true)
    @DeleteMapping("/admin/banner/{id}")
    public Result<?> delete(@PathVariable Long id) { bannerMapper.deleteById(id); return Result.success("已删除"); }
}
