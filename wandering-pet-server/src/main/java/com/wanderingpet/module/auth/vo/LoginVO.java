package com.wanderingpet.module.auth.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录 / 个人信息出参（C 端与后台共用，字段按角色可空）
 */
@Data
public class LoginVO {

    private String token;

    private Long id;

    /** ADMIN / SHELTER / USER */
    private String userType;

    /** 展示名：C 端昵称 / 后台真实姓名 */
    private String nickname;

    private String avatar;

    /** 脱敏手机号，如 138****0001 */
    private String phone;

    private String city;

    /** C 端实名状态 */
    private Integer realNameStatus;

    /** C 端绑定状态：是否已设置用户名密码（可用账号密码登录） */
    private Boolean usernameBound;

    /** C 端绑定状态：是否已绑定微信（可用微信一键登录） */
    private Boolean wechatBound;

    /** 积分余额（1元消费=1积分） */
    private Integer points;

    /** SHELTER 角色所属救助站 */
    private Long shelterId;

    private String shelterName;

    /** 权限编码集合（C 端为空集合） */
    private List<String> permissions;
}
