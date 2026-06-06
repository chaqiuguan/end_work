package com.zhuanbaomao.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.entity.Feedback;
import com.zhuanbaomao.entity.User;
import com.zhuanbaomao.user.mapper.FeedbackMapper;
import com.zhuanbaomao.user.mapper.UserMapper;
import com.zhuanbaomao.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    /** 用户提交反馈 */
    @PostMapping("/submit")
    public Result<?> submit(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        Feedback f = new Feedback();
        f.setUserId(userId);
        f.setContent(body.get("content"));
        f.setStatus(0);
        feedbackMapper.insert(f);
        return Result.success("反馈已提交");
    }

    /** 管理员：反馈列表 */
    @GetMapping("/admin/list")
    public Result<?> adminList() {
        List<Feedback> list = feedbackMapper.selectList(
                new LambdaQueryWrapper<Feedback>().orderByDesc(Feedback::getCreatedAt));
        return Result.success(list.stream().map(f -> {
            User u = userMapper.selectById(f.getUserId());
            Map<String,Object> m = new HashMap<>();
            m.put("id",f.getId()); m.put("content",f.getContent()); m.put("reply",f.getReply());
            m.put("status",f.getStatus()); m.put("createdAt",f.getCreatedAt());
            m.put("userName", u != null ? u.getUsername() : "未知");
            return m;
        }).collect(Collectors.toList()));
    }

    /** 管理员：回复反馈 */
    @PutMapping("/admin/{id}/reply")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Feedback f = feedbackMapper.selectById(id);
        if (f == null) return Result.fail("反馈不存在");
        f.setReply(body.get("reply"));
        f.setStatus(1);
        feedbackMapper.updateById(f);
        return Result.success("已回复");
    }
}
