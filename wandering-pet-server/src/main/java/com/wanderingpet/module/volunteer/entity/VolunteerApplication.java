package com.wanderingpet.module.volunteer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 志愿者报名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("volunteer_application")
public class VolunteerApplication extends BaseEntity {

    private Long shelterId;

    private Long userId;

    private String realName;

    private String phone;

    private String skills;

    private String availableTime;

    /** PENDING / APPROVED / REJECTED */
    private String status;

    private String auditRemark;

    private LocalDateTime auditTime;
}
