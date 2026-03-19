package com.fnusale.common.constant;

/**
 * 本地消息状态常量
 */
public class LocalMessageStatus {

    private LocalMessageStatus() {
    }

    /**
     * 待发送
     */
    public static final String PENDING = "PENDING";

    /**
     * 已发送
     */
    public static final String SENT = "SENT";

    /**
     * 发送失败
     */
    public static final String FAILED = "FAILED";

    /**
     * 消息类型
     */
    public static class MessageType {
        public static final String SECKILL_ORDER = "SECKILL_ORDER";
        public static final String COUPON_GRANT = "COUPON_GRANT";
        public static final String SECKILL_REMINDER = "SECKILL_REMINDER";
    }

    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRY_COUNT = 5;

    /**
     * 重试间隔（分钟）
     */
    public static final int[] RETRY_INTERVALS = {1, 2, 5, 10, 30};
}