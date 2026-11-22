package com.wanderingpet.module.adoption.vo;

import lombok.Data;

/**
 * 状态流水出参
 */
@Data
public class FlowLogVO {

    private Long id;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String operatorType;

    private String remark;

    private String createTime;
}
