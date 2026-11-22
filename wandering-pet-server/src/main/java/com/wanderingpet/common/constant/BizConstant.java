package com.wanderingpet.common.constant;

/**
 * 业务常量：对象类型、业务类型、文件约束等
 */
public final class BizConstant {

    private BizConstant() {
    }

    // ---------- 收藏/评论对象类型 ----------
    public static final String TARGET_PET = "PET";
    public static final String TARGET_ARTICLE = "ARTICLE";

    // ---------- 消息业务类型 ----------
    public static final String BIZ_APPLICATION = "APPLICATION";
    public static final String BIZ_FOLLOW_UP = "FOLLOW_UP";
    public static final String BIZ_DONATION = "DONATION";
    public static final String BIZ_SHELTER = "SHELTER";
    public static final String BIZ_REPORT = "REPORT";
    public static final String BIZ_SYSTEM = "SYSTEM";

    // ---------- 文件业务类型 ----------
    public static final String FILE_BIZ_PET = "pet";
    public static final String FILE_BIZ_AVATAR = "avatar";
    public static final String FILE_BIZ_ARTICLE = "article";
    public static final String FILE_BIZ_DONATION = "donation";
    public static final String FILE_BIZ_REPORT = "report";
    public static final String FILE_BIZ_AGREEMENT = "agreement";
    public static final String FILE_BIZ_PRODUCT = "product";
    public static final String FILE_BIZ_DYNAMIC = "dynamic";

    // ---------- 文件大小限制（字节） ----------
    public static final long IMAGE_MAX_SIZE = 5L * 1024 * 1024;
    public static final long VIDEO_MAX_SIZE = 50L * 1024 * 1024;
    public static final long DOC_MAX_SIZE = 10L * 1024 * 1024;

    // ---------- 编号前缀 ----------
    public static final String SEQ_PET = "pet";
    public static final String SEQ_APPLY = "apply";
    public static final String SEQ_ADOPTION = "ado";
    public static final String SEQ_PAY = "pay";
    public static final String SEQ_REPORT = "rsr";
    public static final String SEQ_SHOP_ORDER = "ord";

    // ---------- 机构审核状态 ----------
    public static final int SHELTER_AUDIT_PENDING = 0;
    public static final int SHELTER_AUDIT_PASS = 1;
    public static final int SHELTER_AUDIT_REJECT = 2;

    // ---------- 评论/文章状态 ----------
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_OFFLINE = 2;

    // ---------- 回访计划（领养完成后第 7/30/90/180 天） ----------
    public static final int[] FOLLOW_UP_DAYS = {7, 30, 90, 180};

    // ---------- 领养申请超期天数 ----------
    public static final int APPLICATION_EXPIRE_DAYS = 7;
}
