package com.wanderingpet.module.sys.service.impl;

import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.module.sys.mapper.SysPermissionMapper;
import com.wanderingpet.module.sys.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现：Redis List 缓存 auth:perm:{type}:{id}，TTL 2 小时
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final com.wanderingpet.common.util.RedisUtil redisUtil;

    @Override
    public Set<String> loadPermissionCodes(String roleCode, String userType, Long userId) {
        if (roleCode == null) {
            return new HashSet<>();
        }
        String cacheKey = RedisKeyConstant.AUTH_PERM.formatted(userType, userId);
        List<String> cached = redisUtil.template().opsForList().range(cacheKey, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return new HashSet<>(cached);
        }
        Set<String> perms = new HashSet<>(sysPermissionMapper.selectPermCodesByRoleCode(roleCode));
        if (!perms.isEmpty()) {
            redisUtil.listPushAll(cacheKey, List.copyOf(perms), Duration.ofHours(2));
        }
        return perms;
    }
}
