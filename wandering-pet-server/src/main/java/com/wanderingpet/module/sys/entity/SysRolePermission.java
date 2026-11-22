package com.wanderingpet.module.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_permission")
public class SysRolePermission extends BaseEntity {

    private Long roleId;

    private Long permId;
}
