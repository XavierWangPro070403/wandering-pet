package com.wanderingpet.common.constant;

/**
 * Redis Key 约定（见提示词 6.4 节）
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    /** 登录态：auth:token:{userType}:{userId}，TTL 与 JWT 一致（2h） */
    public static final String AUTH_TOKEN = "auth:token:%s:%d";

    /** 图形验证码：auth:captcha:{uuid}，TTL 5min */
    public static final String AUTH_CAPTCHA = "auth:captcha:%s";
    public static final String AUTH_WECHAT_QR = "auth:wechatqr:%s";

    /** 权限缓存：auth:perm:{userType}:{userId}，TTL 2h（登录续期时刷新） */
    public static final String AUTH_PERM = "auth:perm:%s:%d";

    /** 宠物详情缓存：cache:pet:detail:{petId}，TTL 30min */
    public static final String CACHE_PET_DETAIL = "cache:pet:detail:%d";

    /** 空值缓存穿透保护后缀 */
    public static final String CACHE_EMPTY = "cache:empty:%s:%d";

    /** 数据字典缓存：cache:dict:all（Hash），TTL 24h */
    public static final String CACHE_DICT_ALL = "cache:dict:all";

    /** 浏览量日榜：rank:pet:view:day（ZSet），TTL 24h */
    public static final String RANK_PET_VIEW_DAY = "rank:pet:view:day";

    /** 防并发重复申请锁：lock:adoption:{petId}:{userId}，TTL 10s */
    public static final String LOCK_ADOPTION = "lock:adoption:%d:%d";

    /** 接口限流计数：limit:api:{subject}:{path}，TTL 1min */
    public static final String LIMIT_API = "limit:api:%s:%s";

    /** 编号序列：seq:{biz}:{yyyyMMdd}，TTL 48h */
    public static final String SEQ_BIZ = "seq:%s:%s";

    /** 定时任务分布式锁：lock:job:{name}，TTL 10min */
    public static final String LOCK_JOB = "lock:job:%s";

    /** 文章点赞去重：like:article:{articleId}（Set 存 userId），TTL 7d */
    public static final String LIKE_ARTICLE = "like:article:%d";

    /** 短信验证码：auth:sms:code:{scene}:{phone}，TTL 5min */
    public static final String SMS_CODE = "auth:sms:code:%s:%s";

    /** 短信发送节流：auth:sms:lock:{scene}:{phone}，TTL 60s */
    public static final String SMS_SEND_LOCK = "auth:sms:lock:%s:%s";

    /** 短信单日条数：auth:sms:day:{phone}:{yyyyMMdd}，TTL 24h */
    public static final String SMS_SEND_DAY = "auth:sms:day:%s:%s";

    /** 短信校验失败计数：auth:sms:fail:{scene}:{phone}，超过上限即作废验证码 */
    public static final String SMS_VERIFY_FAIL = "auth:sms:fail:%s:%s";
}
