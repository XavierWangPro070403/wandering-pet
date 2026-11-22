package com.wanderingpet.module.sys.service;

import com.wanderingpet.module.sys.vo.DictItemVO;
import com.wanderingpet.module.sys.vo.OperationLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.sys.entity.SysDict;
import com.wanderingpet.module.sys.vo.PermissionTreeVO;
import com.wanderingpet.module.sys.vo.RoleVO;
import com.wanderingpet.module.sys.vo.SysUserVO;
import com.wanderingpet.module.sys.dto.RolePermAssignDTO;
import com.wanderingpet.module.sys.dto.SysUserSaveDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统管理服务：字典 / 角色权限 / 后台账号 / 操作日志
 */
public interface SysManageService {

    // ---------- 字典 ----------

    /** 全部启用字典（按类型分组，Redis 缓存 24h） */
    Map<String, List<DictItemVO>> dictAll();

    List<SysDict> listDict(String dictType);

    void saveDict(SysDict dict);

    void updateDict(Long id, SysDict dict);

    void deleteDict(Long id);

    // ---------- 角色权限 ----------

    List<RoleVO> listRoles();

    List<PermissionTreeVO> permissionTree();

    void assignRolePerms(Long roleId, RolePermAssignDTO dto);

    // ---------- 后台账号 ----------

    PageResult<SysUserVO> pageSysUsers(Integer pageNum, Integer pageSize, String roleCode, String keyword);

    void saveSysUser(SysUserSaveDTO dto);

    void toggleSysUser(Long id, Boolean enable);

    void resetSysUserPassword(Long id, String newPassword);

    // ---------- 操作日志（MongoDB） ----------

    PageResult<OperationLogVO> pageOperationLogs(Integer pageNum, Integer pageSize, Long userId, String module);
}
