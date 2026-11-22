package com.wanderingpet.module.sys.service;

import java.util.Set;

/**
 * 权限服务：登录后按角色加载权限编码集合（Redis 缓存优先）
 */
public interface PermissionService {

    /**
     * 加载角色权限编码集合
     *
     * @param roleCode 角色编码
     * @param userType 用户类型
     * @param userId   用户 ID
     * @return 权限编码集合（可能为空集合）
     */
    Set<String> loadPermissionCodes(String roleCode, String userType, Long userId);
}
