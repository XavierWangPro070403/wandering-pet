package com.wanderingpet.module.sys.vo;

import lombok.Data;

/**
 * 操作日志出参（MongoDB operation_log）
 */
@Data
public class OperationLogVO {

    private String id;

    private Long userId;

    private String userType;

    private String module;

    private String action;

    private String method;

    private String params;

    private String ip;

    private Long duration;

    private Boolean success;

    private String errorMessage;

    private String createTime;
}
