package com.wanderingpet.common.enums;

/**
 * 用户类型枚举
 */
public enum UserType {

    /** 平台管理员 */
    ADMIN,
    /** 救助站机构账号 */
    SHELTER,
    /** C 端普通用户 */
    USER;

    public static boolean isValid(String value) {
        for (UserType t : values()) {
            if (t.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
