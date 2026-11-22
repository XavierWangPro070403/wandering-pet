package com.wanderingpet.module.adoption.vo;

import lombok.Data;

/**
 * 领养申请列表项
 */
@Data
public class ApplicationListVO {

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

    private String applicantPhone;

    private String city;

    private String status;

    private String interviewTime;

    private String homeVisitTime;

    private String auditRemark;

    private String createTime;
}
