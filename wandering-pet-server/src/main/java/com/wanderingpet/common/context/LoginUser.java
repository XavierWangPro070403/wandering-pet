package com.wanderingpet.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 登录用户上下文对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户 ID（sys_user.id 或 user.id） */
    private Long userId;

    /** 用户类型：ADMIN / SHELTER / USER */
    private String userType;

    /** 登录账号 */
    private String username;

    /** 救助站 ID（SHELTER 类型必填） */
    private Long shelterId;

    /** 权限编码集合（ADMIN / SHELTER 加载，USER 为空集合） */
    private Set<String> permissions;

    public boolean isAdmin() {
        return "ADMIN".equals(userType);
    }

    public boolean isShelter() {
        return "SHELTER".equals(userType);
    }

    public boolean isUser() {
        return "USER".equals(userType);
    }

    public boolean hasPermission(String permCode) {
        return permissions != null && permissions.contains(permCode);
    }
}
