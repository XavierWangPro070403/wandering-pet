package com.wanderingpet.module.sys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台账号创建入参
 */
@Data
public class SysUserSaveDTO {

    @NotBlank(message = "请输入登录账号")
    @Size(min = 4, max = 32, message = "账号长度为 4-32 位")
    private String username;

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String password;

    @NotBlank(message = "请输入姓名")
    @Size(max = 32)
    private String realName;

    private String phone;

    /** ADMIN / SHELTER */
    @NotBlank(message = "请选择角色")
    private String roleCode;

    /** SHELTER 角色必填 */
    private Long shelterId;
}
