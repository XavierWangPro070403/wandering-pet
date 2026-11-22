package com.wanderingpet.common.enums;

/**
 * 回访任务状态
 */
public enum FollowUpStatus {

    /** 待回访 */
    PENDING,
    /** 已完成 */
    DONE,
    /** 已逾期（超过计划日期 3 天未填写，由定时任务标记） */
    OVERDUE;

    public static boolean isValid(String value) {
        for (FollowUpStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
