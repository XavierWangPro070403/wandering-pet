package com.wanderingpet.common.enums;

/**
 * 领养申请状态机
 *
 * <pre>
 * DRAFT → SUBMITTED → REVIEWING
 * REVIEWING → INTERVIEW → HOME_VISIT → APPROVED
 * REVIEWING/INTERVIEW/HOME_VISIT → REJECTED
 * APPROVED → SIGNED → ADOPTED
 * SUBMITTED/REVIEWING → CANCELED（申请人撤回）
 * ADOPTED → RETURNED（退养）
 * SUBMITTED/REVIEWING 超期 7 天 → EXPIRED（定时任务）
 * </pre>
 */
public enum ApplicationStatus {

    DRAFT, SUBMITTED, REVIEWING, INTERVIEW, HOME_VISIT,
    APPROVED, SIGNED, ADOPTED, REJECTED, CANCELED, EXPIRED, RETURNED;

    /** 是否终态（终态后不可再流转，且不阻止用户再次申请） */
    public boolean isTerminal() {
        return this == ADOPTED || this == REJECTED || this == CANCELED
                || this == EXPIRED || this == RETURNED;
    }

    /** 申请人是否可撤回（仅未进入审核实质阶段前） */
    public boolean canCancel() {
        return this == SUBMITTED || this == REVIEWING;
    }

    /** 救助站是否可驳回 */
    public boolean canReject() {
        return this == REVIEWING || this == INTERVIEW || this == HOME_VISIT;
    }

    public static boolean isValid(String value) {
        for (ApplicationStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
