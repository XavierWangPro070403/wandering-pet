package com.wanderingpet.module.volunteer.vo;

import lombok.Data;

/**
 * 志愿者报名出参
 */
@Data
public class VolunteerVO {

    private Long id;

    private Long shelterId;

    private String shelterName;

    private Long userId;

    private String userNickname;

    private String realName;

    private String phone;

    private String skills;

    private String availableTime;

    private String status;

    private String auditRemark;

    private String auditTime;

    private String createTime;
}
