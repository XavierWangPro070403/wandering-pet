package com.wanderingpet.module.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机构工作台统计出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShelterWorkbenchVO {

    /** 在册宠物总数 */
    private Long petTotal;

    /** 可领养宠物数 */
    private Long availableCount;

    /** 待处理申请数（SUBMITTED/REVIEWING） */
    private Long pendingApplication;

    /** 待回访任务数 */
    private Long followUpPending;

    /** 本月领养完成数 */
    private Long monthAdopted;

    /** 机构未读消息数 */
    private Long unreadMessage;
}
