package com.wanderingpet.module.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /** 权限编码，如 pet:create */
    private String permCode;

    private String permName;

    /** MENU / BUTTON / API */
    private String permType;

    /** 父级 ID（0-顶级） */
    private Long parentId;

    /** 菜单路由路径 */
    private String path;

    private Integer sort;
}
