package com.fnusale.common.constant;

/**
 * 用户相关常量
 */
public class UserConstants {

    private UserConstants() {
    }

    /**
     * 默认信誉分
     */
    public static final int DEFAULT_CREDIT_SCORE = 100;

    /**
     * 最低信誉分（低于此分数限制发布/秒杀）
     */
    public static final int MIN_CREDIT_SCORE = 60;

    /**
     * 默认地址数量上限
     */
    public static final int MAX_ADDRESS_COUNT = 10;

    /**
     * 验证码长度
     */
    public static final int CAPTCHA_LENGTH = 6;

    /**
     * 验证码有效期（秒）
     */
    public static final int CAPTCHA_EXPIRATION = 300;

    /**
     * 验证码发送间隔（秒）
     */
    public static final int CAPTCHA_SEND_INTERVAL = 60;

    /**
     * 签到基础奖励积分
     */
    public static final int SIGN_BASE_POINTS = 1;

    /**
     * 连续签到7天奖励积分
     */
    public static final int SIGN_7_DAYS_POINTS = 7;

    /**
     * 连续签到14天奖励积分
     */
    public static final int SIGN_14_DAYS_POINTS = 15;

    /**
     * 连续签到30天奖励积分
     */
    public static final int SIGN_30_DAYS_POINTS = 30;

    /**
     * 补签消耗积分
     */
    public static final int REPAIR_SIGN_COST = 10;

    /**
     * 补签最大天数限制
     */
    public static final int REPAIR_SIGN_MAX_DAYS = 7;

    /**
     * 每月最大补签次数
     */
    public static final int REPAIR_SIGN_MAX_MONTHLY = 3;

    /**
     * 身份类型常量
     */
    public static final String IDENTITY_TYPE_STUDENT = "STUDENT";
    public static final String IDENTITY_TYPE_TEACHER = "TEACHER";

    /**
     * 定位权限常量
     */
    public static final String LOCATION_PERMISSION_ALLOW = "ALLOW";
    public static final String LOCATION_PERMISSION_DENY = "DENY";

    /**
     * 认证状态常量
     */
    public static final String AUTH_STATUS_UNAUTH = "UNAUTH";
    public static final String AUTH_STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String AUTH_STATUS_SUCCESS = "AUTH_SUCCESS";

    /**
     * 登录失败限制
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_LOCK_TIME_MINUTES = 15;

    /**
     * 接口限流配置
     */
    public static final int CAPTCHA_SEND_LIMIT_PER_DAY = 10;
    public static final int LOGIN_LIMIT_PER_MINUTE = 5;

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 用户名最小长度
     */
    public static final int USERNAME_MIN_LENGTH = 2;

    /**
     * 用户名最大长度
     */
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 评价提交时限（订单完成后，天）
     */
    public static final int EVALUATION_DEADLINE_DAYS = 7;

    /**
     * 追加评价时限（评价后，天）
     */
    public static final int APPEND_EVALUATION_DEADLINE_DAYS = 30;

    // ========== Redis Key 常量 ==========

    /**
     * 验证码缓存key前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /**
     * 登录token缓存key前缀
     */
    public static final String TOKEN_KEY_PREFIX = "token:";

    /**
     * 用户信息缓存key前缀
     */
    public static final String USER_CACHE_KEY_PREFIX = "user:info:";

    /**
     * 签到记录缓存key前缀
     */
    public static final String SIGN_RECORD_KEY_PREFIX = "sign:record:";

    /**
     * 连续签到天数缓存key前缀
     */
    public static final String SIGN_CONTINUOUS_KEY_PREFIX = "sign:continuous:";
}