package com.wanderingpet.module.adoption.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 领养申请。
 * active_flag 为数据库生成列（非终态=1、终态=NULL），配合唯一索引防重复申请，实体不映射。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adoption_application")
public class AdoptionApplication extends BaseEntity {

    private String applyNo;

    private Long petId;

    private Long userId;

    private Long shelterId;

    private String applicantName;

    private String applicantPhone;

    private Integer applicantAge;

    private String occupation;

    private String city;

    /** OWN / RENT */
    private String housingType;

    private Integer hasPetExperience;

    private Integer familyAgree;

    private Integer hasChildren;

    private Integer hasAllergy;

    private String reason;

    /** 见 ApplicationStatus 状态机 */
    private String status;

    private LocalDateTime interviewTime;

    private LocalDateTime homeVisitTime;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;
}
