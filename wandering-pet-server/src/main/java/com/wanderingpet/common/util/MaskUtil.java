package com.wanderingpet.common.util;

/**
 * 脱敏工具：手机号、姓名（日志与展示均禁止输出完整敏感信息）
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 138****0001 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 江小领 → 江**；两字姓名 → 张* */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
