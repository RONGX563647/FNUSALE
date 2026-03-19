package com.fnusale.marketing.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fnusale.common.common.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 限流降级处理类
 */
@Slf4j
public class SeckillBlockHandler {

    /**
     * 秒杀接口限流处理
     */
    public static Result<Long> handleJoinSeckillBlock(Long userId, Long activityId, BlockException e) {
        log.warn("秒杀接口限流: userId={}, activityId={}", userId, activityId);
        return Result.failed(429, "秒杀火爆，请稍后再试");
    }

    /**
     * 获取秒杀列表限流处理
     */
    public static Result<?> handleGetSeckillListBlock(BlockException e) {
        log.warn("获取秒杀列表限流");
        return Result.failed(429, "系统繁忙，请稍后再试");
    }

    /**
     * 获取秒杀详情限流处理
     */
    public static Result<?> handleGetDetailBlock(Long activityId, BlockException e) {
        log.warn("获取秒杀详情限流: activityId={}", activityId);
        return Result.failed(429, "系统繁忙，请稍后再试");
    }
}