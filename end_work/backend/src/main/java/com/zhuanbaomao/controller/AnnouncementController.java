package com.zhuanbaomao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.entity.Announcement;
import com.zhuanbaomao.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    /** 公开：公告列表 */
    @GetMapping("/announcement/list")
    public Result<List<Announcement>> list() {
        return Result.success(announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getStatus, 1).orderByDesc(Announcement::getCreatedAt)));
    }

    /** 管理员：全部公告 */
    @GetMapping("/admin/announcement/list")
    public Result<List<Announcement>> adminList() {
        return Result.success(announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt)));
    }

    @PostMapping("/admin/announcement/add")
    public Result<?> add(@RequestBody Announcement a) { announcementMapper.insert(a); return Result.success("发布成功"); }
    @PutMapping("/admin/announcement/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Announcement a) { a.setId(id); announcementMapper.updateById(a); return Result.success("更新成功"); }
    @DeleteMapping("/admin/announcement/{id}")
    public Result<?> delete(@PathVariable Long id) { announcementMapper.deleteById(id); return Result.success("已删除"); }
}
