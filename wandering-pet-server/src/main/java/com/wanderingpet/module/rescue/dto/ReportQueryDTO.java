package com.wanderingpet.module.rescue.dto;

import lombok.Data;

/**
 * 线索列表查询入参（管理端/我的上报）
 */
@Data
public class ReportQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String status;

    private Integer urgency;

    private String city;

    private String keyword;
}
