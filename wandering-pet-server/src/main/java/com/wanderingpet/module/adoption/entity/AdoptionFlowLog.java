package com.wanderingpet.module.adoption.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 领养申请状态流转流水
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adoption_flow_log")
public class AdoptionFlowLog extends BaseEntity {

    private Long applicationId;

    /** 流转前状态（首次提交为空） */
    private String fromStatus;

    private String toStatus;

    /** 系统任务为 0 */
    private Long operatorId;

    /** USER / SHELTER / ADMIN / SYSTEM */
    private String operatorType;

    private String remark;
}
