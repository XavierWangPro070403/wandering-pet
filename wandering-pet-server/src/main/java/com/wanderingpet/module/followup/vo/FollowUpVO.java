package com.wanderingpet.module.followup.vo;

import lombok.Data;

/**
 * 回访任务出参（领养人/机构通用，机构侧附加宠物与领养人信息）
 */
@Data
public class FollowUpVO {

    private Long id;

    private Long recordId;

    private Long applicationId;

    private Long petId;

    private String petName;

    private Long userId;

    private String userNickname;

    private String userPhone;

    private String planDate;

    private String actualDate;

    private Integer round;

    private java.math.BigDecimal weight;

    private String healthStatus;

    private String vaccineStatus;

    private String lifeDesc;

    private java.util.List<String> images;

    private Long handlerId;

    /** PENDING / DONE / OVERDUE */
    private String status;
}
