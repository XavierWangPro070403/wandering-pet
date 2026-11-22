package com.wanderingpet.common.enums;

/**
 * 宠物状态枚举
 *
 * <p>AVAILABLE（可领养）→ PENDING（审核中锁定）→ ADOPTED（已领养）；
 * MEDICAL（治疗中）、OFFLINE（下架）不参与申请。</p>
 */
public enum PetStatus {

    /** 可领养 */
    AVAILABLE,
    /** 审核中锁定 */
    PENDING,
    /** 已领养 */
    ADOPTED,
    /** 治疗中 */
    MEDICAL,
    /** 已下架 */
    OFFLINE;

    /** 是否允许提交领养申请 */
    public boolean adoptable() {
        return this == AVAILABLE;
    }

    public static boolean isValid(String value) {
        for (PetStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
