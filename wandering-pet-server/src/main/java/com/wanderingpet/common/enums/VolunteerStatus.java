package com.wanderingpet.common.enums;

/**
 * 志愿者报名审核状态
 */
public enum VolunteerStatus {

    /** 待审核 */
    PENDING,
    /** 已通过 */
    APPROVED,
    /** 未通过 */
    REJECTED;

    public static boolean isValid(String value) {
        for (VolunteerStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
