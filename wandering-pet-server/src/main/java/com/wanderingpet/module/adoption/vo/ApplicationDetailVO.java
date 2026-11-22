package com.wanderingpet.module.adoption.vo;

import lombok.Data;

import java.util.List;

/**
 * 领养申请详情出参（含宠物摘要与状态流水）
 */
@Data
public class ApplicationDetailVO {

    private Long id;

    private String applyNo;

    private Long petId;

    private String petName;

    private String petCover;

    private Long userId;

    private String userNickname;

    private Long shelterId;

    private String shelterName;

    private String applicantName;

    /** 脱敏电话 */
    private String applicantPhone;

    private Integer applicantAge;

    private String occupation;

    private String city;

    private String housingType;

    private Integer hasPetExperience;

    private Integer familyAgree;

    private Integer hasChildren;

    private Integer hasAllergy;

    private String reason;

    private String status;

    private String interviewTime;

    private String homeVisitTime;

    private String auditRemark;

    private String auditTime;

    private String createTime;

    /** 状态流转记录 */
    private List<FlowLogVO> flowLogs;
}
