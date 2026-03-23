package com.fnusale.marketing.config;

import lombok.Getter;

/**
 * 消息幂等性Key配置
 * 根据RocketMQ实际重试间隔计算合理的过期时间
 */
@Getter
public class IdempotentKeyConfig {

    private IdempotentKeyConfig() {
    }

    /**
     * RocketMQ重试间隔（秒）
     * 对应延迟级别: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    private static final int[] RETRY_INTERVALS = {
            1, 5, 10, 30, 60, 120, 180, 240, 300, 360,
            420, 480, 540, 600, 1200, 1800, 3600, 7200
    };

    /**
     * 消费者最大重试次数（建议值）
     * 根据业务重要性调整：
     * - 关键业务（如订单）：3-5次
     * - 普通业务（如优惠券）：3次
     * - 低优先级业务（如提醒）：2次
     */
    public static final int DEFAULT_MAX_RETRY_TIMES = 3;

    /**
     * 缓冲时间（秒）
     * 在最大重试时间基础上增加缓冲
     */
    private static final long BUFFER_SECONDS = 7200L;

    /**
     * 计算幂等性Key过期时间（秒）
     * 公式：总重试时间 + 缓冲时间
     *
     * @param maxRetryTimes 最大重试次数
     * @return 过期时间（秒）
     */
    public static long calculateExpireSeconds(int maxRetryTimes) {
        long total = 0;
        for (int i = 0; i < maxRetryTimes && i < RETRY_INTERVALS.length; i++) {
            total += RETRY_INTERVALS[i];
        }
        return total + BUFFER_SECONDS;
    }

    /**
     * 使用默认最大重试次数计算过期时间
     *
     * @return 过期时间（秒）
     */
    public static long calculateDefaultExpireSeconds() {
        return calculateExpireSeconds(DEFAULT_MAX_RETRY_TIMES);
    }

    /**
     * 关键业务使用的过期时间（如秒杀订单）
     * 较长的时间确保可靠性
     *
     * @return 过期时间（秒）
     */
    public static long calculateCriticalExpireSeconds() {
        return calculateExpireSeconds(5) + BUFFER_SECONDS;
    }

    /**
     * 低优先级业务使用的过期时间（如过期提醒）
     * 较短的时间节省内存
     *
     * @return 过期时间（秒）
     */
    public static long calculateLowPriorityExpireSeconds() {
        return calculateExpireSeconds(2) + BUFFER_SECONDS;
    }
}