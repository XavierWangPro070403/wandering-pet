package com.wanderingpet.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.sys.dto.RolePermAssignDTO;
import com.wanderingpet.module.sys.dto.SysUserSaveDTO;
import com.wanderingpet.module.sys.entity.SysDict;
import com.wanderingpet.module.sys.service.SysManageService;
import com.wanderingpet.module.sys.vo.DictItemVO;
import com.wanderingpet.module.sys.vo.OperationLogVO;
import com.wanderingpet.module.sys.vo.PermissionTreeVO;
import com.wanderingpet.module.sys.vo.RoleVO;
import com.wanderingpet.module.sys.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置接口（角色权限 / 字典 / 后台账号 / 操作日志）
 */
@Tag(name = "管理端-系统设置", description = "角色权限、数据字典、后台账号、操作日志")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SysManageService sysManageService;

    // ---------------- 字典（公开读取在 /api/v1/dicts） ----------------

    @Operation(summary = "字典全量（按类型分组）")
    @RequirePermission("dict:manage")
    @GetMapping("/dicts")
    public R<Map<String, List<DictItemVO>>> dictAll() {
        return R.ok(sysManageService.dictAll());
    }

    @Operation(summary = "字典列表（管理）")
    @RequirePermission("dict:manage")
    @GetMapping("/dict-items")
    public R<List<SysDict>> listDict(@RequestParam(required = false) String dictType) {
        return R.ok(sysManageService.listDict(dictType));
    }

    @Operation(summary = "新增字典项")
    @RequirePermission("dict:manage")
    @PostMapping("/dict-items")
    @OpLog(module = "dict", action = "新增字典项")
    public R<Void> saveDict(@RequestBody SysDict dict) {
        sysManageService.saveDict(dict);
        return R.ok();
    }

    @Operation(summary = "更新字典项")
    @RequirePermission("dict:manage")
    @PutMapping("/dict-items/{id}")
    @OpLog(module = "dict", action = "更新字典项")
    public R<Void> updateDict(@PathVariable Long id, @RequestBody SysDict dict) {
        sysManageService.updateDict(id, dict);
        return R.ok();
    }

    @Operation(summary = "删除字典项")
    @RequirePermission("dict:manage")
    @DeleteMapping("/dict-items/{id}")
    @OpLog(module = "dict", action = "删除字典项")
    public R<Void> deleteDict(@PathVariable Long id) {
        sysManageService.deleteDict(id);
        return R.ok();
    }

    // ---------------- 角色权限 ----------------

    @Operation(summary = "角色列表（含权限 ID）")
    @RequirePermission("role:manage")
    @GetMapping("/roles")
    public R<List<RoleVO>> roles() {
        return R.ok(sysManageService.listRoles());
    }

    @Operation(summary = "权限树")
    @RequirePermission("role:manage")
    @GetMapping("/permissions")
    public R<List<PermissionTreeVO>> permissions() {
        return R.ok(sysManageService.permissionTree());
    }

    @Operation(summary = "分配角色权限")
    @RequirePermission("role:manage")
    @PutMapping("/roles/{id}/permissions")
    @OpLog(module = "role", action = "分配角色权限")
    public R<Void> assignRolePerms(@PathVariable Long id, @Valid @RequestBody RolePermAssignDTO dto) {
        sysManageService.assignRolePerms(id, dto);
        return R.ok();
    }

    // ---------------- 后台账号 ----------------

    @Operation(summary = "后台账号分页")
    @RequirePermission("sys-user:manage")
    @GetMapping("/sys-users")
    public R<PageResult<SysUserVO>> sysUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String roleCode,
                                             @RequestParam(required = false) String keyword) {
        Page<SysUserVO> page = new Page<>(pageNum, pageSize);
        PageResult<SysUserVO> result = sysManageService.pageSysUsers(pageNum, pageSize, roleCode, keyword);
        return R.ok(result);
    }

    @Operation(summary = "新增后台账号")
    @RequirePermission("sys-user:manage")
    @PostMapping("/sys-users")
    @OpLog(module = "sys-user", action = "新增后台账号")
    public R<Void> saveSysUser(@Valid @RequestBody SysUserSaveDTO dto) {
        sysManageService.saveSysUser(dto);
        return R.ok();
    }

    @Operation(summary = "启用/禁用后台账号")
    @RequirePermission("sys-user:manage")
    @PostMapping("/sys-users/{id}/status")
    @OpLog(module = "sys-user", action = "启停后台账号")
    public R<Void> toggleSysUser(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        sysManageService.toggleSysUser(id, body.get("enable"));
        return R.ok();
    }

    @Operation(summary = "重置后台账号密码")
    @RequirePermission("sys-user:manage")
    @PostMapping("/sys-users/{id}/reset-password")
    @OpLog(module = "sys-user", action = "重置后台账号密码")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        sysManageService.resetSysUserPassword(id, body.get("newPassword"));
        return R.ok();
    }

    // ---------------- 操作日志 ----------------

    @Operation(summary = "操作日志分页（MongoDB）")
    @RequirePermission("oplog:view")
    @GetMapping("/operation-logs")
    public R<PageResult<OperationLogVO>> operationLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       @RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) String module) {
        return R.ok(sysManageService.pageOperationLogs(pageNum, pageSize, userId, module));
    }
}
