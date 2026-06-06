package com.zhuanbaomao.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.config.BusinessException;
import com.zhuanbaomao.entity.Address;
import com.zhuanbaomao.user.mapper.AddressMapper;
import com.zhuanbaomao.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;

    /** 地址列表 */
    @GetMapping("/list")
    public Result<List<Address>> list(@CurrentUser Long userId) {
        return Result.success(addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)));
    }

    /** 新增地址 */
    @PostMapping("/add")
    public Result<?> add(@CurrentUser Long userId, @RequestBody Address addr) {
        addr.setUserId(userId);
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.insert(addr);
        return Result.success(addr.getId());
    }

    /** 更新地址 */
    @PutMapping("/{id}")
    public Result<?> update(@CurrentUser Long userId, @PathVariable Long id, @RequestBody Address addr) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new BusinessException("地址不存在");

        if (addr.getIsDefault() != null && addr.getIsDefault() == 1)
            addressMapper.clearDefault(userId);

        addr.setId(id);
        addr.setUserId(userId);
        addressMapper.updateById(addr);
        return Result.success("地址已更新");
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@CurrentUser Long userId, @PathVariable Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId))
            throw new BusinessException("地址不存在");
        addressMapper.deleteById(id);
        return Result.success("地址已删除");
    }

    /** 设置默认地址 */
    @PutMapping("/{id}/default")
    public Result<?> setDefault(@CurrentUser Long userId, @PathVariable Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId))
            throw new BusinessException("地址不存在");
        addressMapper.clearDefault(userId);
        addr.setIsDefault(1);
        addressMapper.updateById(addr);
        return Result.success("已设为默认地址");
    }
}
