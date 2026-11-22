package com.wanderingpet.module.followup.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 领养后回访记录（ADOPTED 后自动生成 4 次：第 7/30/90/180 天）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("follow_up")
public class FollowUp extends BaseEntity {

    /** 领养记录 ID */
    private Long recordId;

    private Long applicationId;

    private LocalDate planDate;

    private LocalDate actualDate;

    /** 第几次（1-4） */
    private Integer round;

    private BigDecimal weight;

    private String healthStatus;

    private String vaccineStatus;

    private String lifeDesc;

    /** 生活照 JSON 数组（最多 3 张） */
    private String images;

    /** 归档处理人（救助站账号 ID） */
    private Long handlerId;

    /** PENDING / DONE / OVERDUE */
    private String status;
}
