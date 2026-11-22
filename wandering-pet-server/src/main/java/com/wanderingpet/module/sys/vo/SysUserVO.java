package com.wanderingpet.module.sys.vo;

import lombok.Data;

/**
 * 后台账号出参
 */
@Data
public class SysUserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String avatar;

    private String roleCode;

    private Long shelterId;

    private String shelterName;

    private Integer status;

    private String lastLoginTime;

    private String createTime;
}
