package com.wanderingpet.module.user.vo;

import lombok.Data;

/**
 * C 端用户资料出参（手机号脱敏，不含身份证）
 */
@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    /** 138****0001 */
    private String phone;

    private Integer gender;

    private String city;

    private String realName;

    /** 0-未实名 1-已实名 */
    private Integer realNameStatus;

    /** 是否已绑定微信（可用微信一键登录） */
    private Boolean wechatBound;

    /** 积分余额 */
    private Integer points;
}
