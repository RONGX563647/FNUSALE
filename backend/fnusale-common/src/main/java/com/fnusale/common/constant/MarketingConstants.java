package com.fnusale.common.constant;

/**
 * 营销模块常量
 */
public class MarketingConstants {

    private MarketingConstants() {
    }

    // ========== 优惠券状态 ==========

    /**
     * 未使用
     */
    public static final String COUPON_STATUS_UNUSED = "UNUSED";

    /**
     * 已使用
     */
    public static final String COUPON_STATUS_USED = "USED";

    /**
     * 已过期
     */
    public static final String COUPON_STATUS_EXPIRED = "EXPIRED";

    // ========== 优惠券类型 ==========

    /**
     * 满减券
     */
    public static final String COUPON_TYPE_FULL_REDUCE = "FULL_REDUCE";

    /**
     * 直降券
     */
    public static final String COUPON_TYPE_DIRECT_REDUCE = "DIRECT_REDUCE";

    /**
     * 品类券
     */
    public static final String COUPON_TYPE_CATEGORY = "CATEGORY";

    // ========== 秒杀活动状态 ==========

    /**
     * 未开始
     */
    public static final String SECKILL_STATUS_NOT_START = "NOT_START";

    /**
     * 进行中
     */
    public static final String SECKILL_STATUS_ON_GOING = "ON_GOING";

    /**
     * 已结束
     */
    public static final String SECKILL_STATUS_END = "END";

    // ========== Redis Key 常量 ==========

    /**
     * 优惠券详情缓存key前缀
     */
    public static final String COUPON_INFO_KEY_PREFIX = "coupon:info:";

    /**
     * 可领取优惠券列表缓存key
     */
    public static final String COUPON_AVAILABLE_KEY = "coupon:available";

    /**
     * 秒杀活动详情缓存key前缀
     */
    public static final String SECKILL_INFO_KEY_PREFIX = "seckill:info:";

    /**
     * 秒杀库存缓存key前缀
     */
    public static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";

    /**
     * 今日秒杀列表缓存key
     */
    public static final String SECKILL_TODAY_KEY = "seckill:today";

    /**
     * 秒杀用户已购买标记key前缀
     */
    public static final String SECKILL_USER_BOUGHT_PREFIX = "seckill:user:bought:";

    // ========== 业务限制 ==========

    /**
     * 优惠券有效期最大天数
     */
    public static final int COUPON_MAX_VALIDITY_DAYS = 90;

    /**
     * 优惠券最大发放数量
     */
    public static final int COUPON_MAX_TOTAL_COUNT = 10000;

    /**
     * 秒杀活动最大时长（小时）
     */
    public static final int SECKILL_MAX_DURATION_HOURS = 2;

    /**
     * 秒杀库存预热提前时间（分钟）
     */
    public static final int SECKILL_STOCK_PRELOAD_MINUTES = 30;

    /**
     * 秒杀提醒提前时间（分钟）
     */
    public static final int SECKILL_REMINDER_MINUTES = 5;

    /**
     * 秒杀接口QPS阈值
     */
    public static final int SECKILL_QPS_LIMIT = 500;
}