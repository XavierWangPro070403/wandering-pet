package com.wanderingpet.module.sys.vo;

import lombok.Data;

/**
 * 角色出参
 */
@Data
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /** 该角色已分配的权限 ID 集合 */
    private java.util.List<Long> permIds;
}
