package com.wanderingpet.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.sys.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限点 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 按角色编码查询权限编码集合（sys_role → sys_role_permission → sys_permission） */
    List<String> selectPermCodesByRoleCode(String roleCode);
}
