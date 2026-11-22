package com.wanderingpet.common.result;

import lombok.Getter;

/**
 * 统一响应码：HTTP 语义 + 业务错误码分段
 *
 * <p>分段规则：1001-1099 账号域、2001-2099 宠物域、3001-3099 领养域、
 * 4001-4099 文件域、5001-5099 系统域。</p>
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "success"),
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录或登录态已失效 */
    UNAUTHORIZED(401, "请先登录"),
    /** 无权限 */
    FORBIDDEN(403, "没有操作权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    /** 系统内部错误 */
    ERROR(500, "系统繁忙，请稍后再试"),

    // ---------- 账号域 1001-1099 ----------
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    CAPTCHA_ERROR(1003, "验证码错误或已过期"),
    USERNAME_EXISTS(1004, "用户名已被注册"),
    OLD_PASSWORD_ERROR(1005, "原密码不正确"),
    USER_NOT_FOUND(1006, "用户不存在"),
    SMS_CODE_ERROR(1007, "短信验证码错误或已过期"),
    SMS_SEND_TOO_FREQUENT(1008, "验证码已发送，请稍后再试"),
    SMS_DAILY_LIMIT(1009, "该手机号今日验证码发送次数已达上限"),
    PHONE_ALREADY_BOUND(1010, "该手机号已被其他账号绑定"),
    WECHAT_ALREADY_BOUND(1011, "该微信已被其他账号绑定"),

    // ---------- 宠物域 2001-2099 ----------
    PET_NOT_FOUND(2001, "宠物不存在"),
    PET_NOT_AVAILABLE(2002, "该宠物当前不可申请领养"),
    PET_ALREADY_ADOPTED(2003, "该宠物已被领养"),
    CATEGORY_NOT_FOUND(2004, "物种分类不存在"),

    // ---------- 领养域 3001-3099 ----------
    APPLICATION_EXISTS(3001, "您已有该宠物的进行中申请，请先等待处理或撤回"),
    APPLICATION_STATUS_ERROR(3002, "当前状态不允许该操作"),
    APPLICATION_NOT_FOUND(3003, "领养申请不存在"),
    ADOPTION_REQUIREMENT_NOT_MET(3004, "未满足领养条件，请完善申请信息"),
    FOLLOW_UP_NOT_FOUND(3005, "回访任务不存在"),

    // ---------- 文件域 4001-4099 ----------
    FILE_TYPE_NOT_SUPPORT(4001, "文件类型不支持"),
    FILE_TOO_LARGE(4002, "文件超出大小限制"),
    FILE_UPLOAD_ERROR(4003, "文件上传失败，请稍后重试"),
    FILE_EMPTY(4004, "上传文件不能为空"),

    // ---------- 系统域 5001-5099 ----------
    SERVICE_BUSY(5001, "服务繁忙，请稍后再试"),
    THIRD_PARTY_ERROR(5002, "第三方服务调用失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
