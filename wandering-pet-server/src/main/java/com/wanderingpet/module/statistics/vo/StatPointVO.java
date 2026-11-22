package com.wanderingpet.module.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计数据点（名称-数值对，图表通用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatPointVO {

    /** 名称（月份/物种/城市/状态） */
    private String name;

    /** 数值 */
    private Long value;

    /** 附加信息（如增长率） */
    private String extra;
}
