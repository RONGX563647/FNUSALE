package com.fnusale.common.constant;

/**
 * RocketMQ Topic 常量
 */
public class RocketMQConstants {

    private RocketMQConstants() {
    }

    // ==================== 用户相关 Topic ====================

    /**
     * 用户注册 Topic
     */
    public static final String USER_REGISTER_TOPIC = "user-register";

    /**
     * 用户注册 - 初始化积分 Tag
     */
    public static final String USER_REGISTER_TAG_INIT_POINTS = "init-points";

    /**
     * 用户注册 - 发送欢迎通知 Tag
     */
    public static final String USER_REGISTER_TAG_WELCOME = "welcome";

    /**
     * 用户注册 - 发放新人优惠券 Tag
     */
    public static final String USER_REGISTER_TAG_NEW_USER_COUPON = "new-user-coupon";

    // ==================== 秒杀相关 Topic ====================

    /**
     * 秒杀订单 Topic
     */
    public static final String SECKILL_ORDER_TOPIC = "seckill-order";

    /**
     * 秒杀订单 - 创建订单 Tag
     */
    public static final String SECKILL_ORDER_TAG_CREATE = "create";

    /**
     * 秒杀提醒 Topic
     */
    public static final String SECKILL_REMINDER_TOPIC = "seckill-reminder";

    /**
     * 秒杀提醒 - 推送通知 Tag
     */
    public static final String SECKILL_REMINDER_TAG_PUSH = "push";

    // ==================== 优惠券相关 Topic ====================

    /**
     * 优惠券发放 Topic
     */
    public static final String COUPON_GRANT_TOPIC = "coupon-grant";

    /**
     * 优惠券发放 - 批量发放 Tag
     */
    public static final String COUPON_GRANT_TAG_BATCH = "batch";

    // ==================== 商品审核相关 Topic ====================

    /**
     * 商品审核 Topic
     */
    public static final String PRODUCT_AUDIT_TOPIC = "product-audit";

    /**
     * 商品审核 - 审核结果通知 Tag
     */
    public static final String PRODUCT_AUDIT_TAG_NOTIFY = "notify";

    // ==================== 用户认证相关 Topic ====================

    /**
     * 用户认证审核 Topic
     */
    public static final String USER_AUTH_TOPIC = "user-auth";

    /**
     * 用户认证 - 审核结果通知 Tag
     */
    public static final String USER_AUTH_TAG_NOTIFY = "notify";

    // ==================== 用户封禁相关 Topic ====================

    /**
     * 用户封禁 Topic
     */
    public static final String USER_BAN_TOPIC = "user-ban";

    /**
     * 用户封禁 - 封禁/解封通知 Tag
     */
    public static final String USER_BAN_TAG_NOTIFY = "notify";
}