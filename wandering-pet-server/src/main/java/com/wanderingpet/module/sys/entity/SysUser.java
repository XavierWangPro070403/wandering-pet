package com.wanderingpet.module.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 后台账号（管理员 + 救助站机构账号）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** BCrypt 密文 */
    private String password;

    private String realName;

    private String phone;

    private String avatar;

    /** ADMIN / SHELTER */
    private String roleCode;

    /** 所属救助站（SHELTER 必填） */
    private Long shelterId;

    /** 1-启用 0-禁用 */
    private Integer status;

    private LocalDateTime lastLoginTime;
}
