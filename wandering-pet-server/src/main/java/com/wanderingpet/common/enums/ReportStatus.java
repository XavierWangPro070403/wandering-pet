package com.wanderingpet.common.enums;

/**
 * 流浪动物线索状态机：PENDING → ASSIGNED → PROCESSING → RESOLVED → CLOSED
 */
public enum ReportStatus {

    /** 待处理 */
    PENDING,
    /** 已分派 */
    ASSIGNED,
    /** 处理中 */
    PROCESSING,
    /** 已解决 */
    RESOLVED,
    /** 已关闭 */
    CLOSED;

    public static boolean isValid(String value) {
        for (ReportStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
