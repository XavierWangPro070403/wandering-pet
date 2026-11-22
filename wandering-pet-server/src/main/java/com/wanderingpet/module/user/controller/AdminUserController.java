package com.wanderingpet.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.common.util.MaskUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端 C 端用户管理接口
 */
@Tag(name = "管理端-用户管理", description = "C 端用户列表、启用/禁用（仅管理员）")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @Operation(summary = "C 端用户分页")
    @RequirePermission("user:manage")
    @GetMapping
    public R<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer status) {
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<User>()
                        .eq(status != null, User::getStatus, status)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(User::getUsername, keyword)
                                .or().like(User::getNickname, keyword))
                        .orderByDesc(User::getCreateTime));
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return R.ok(new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages()));
    }

    @Operation(summary = "启用/禁用用户")
    @RequirePermission("user:manage")
    @PostMapping("/{id}/status")
    @OpLog(module = "user", action = "启停 C 端用户")
    public R<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return R.fail(1006, "用户不存在");
        }
        boolean enable = Boolean.TRUE.equals(body.get("enable"));
        user.setStatus(enable ? 1 : 0);
        userMapper.updateById(user);
        if (!enable) {
            // 禁用即强制下线
            redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted("USER", user.getId()));
        }
        return R.ok();
    }

    /** VO 不含密码/身份证/完整手机号 */
    private Map<String, Object> toVO(User user) {
        Map<String, Object> vo = new java.util.HashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername());
        vo.put("nickname", user.getNickname());
        vo.put("avatar", user.getAvatar());
        vo.put("phone", MaskUtil.maskPhone(user.getPhone()));
        vo.put("gender", user.getGender());
        vo.put("city", user.getCity());
        vo.put("realNameStatus", user.getRealNameStatus());
        vo.put("status", user.getStatus());
        vo.put("lastLoginTime", user.getLastLoginTime() == null ? null : user.getLastLoginTime().toString().replace('T', ' '));
        vo.put("createTime", user.getCreateTime() == null ? null : user.getCreateTime().toString().replace('T', ' '));
        return vo;
    }
}
