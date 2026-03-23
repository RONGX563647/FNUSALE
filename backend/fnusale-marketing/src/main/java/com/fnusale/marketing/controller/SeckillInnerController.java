package com.fnusale.marketing.controller;

import com.fnusale.common.annotation.InnerApi;
import com.fnusale.common.common.Result;
import com.fnusale.marketing.mapper.LocalMessageMapper;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀内部接口控制器
 * 用于服务间调用
 * 添加@InnerApi注解保护，防止外部恶意调用
 */
@Slf4j
@Tag(name = "秒杀内部接口", description = "供其他服务调用的内部接口")
@RestController
@RequestMapping("/marketing/inner/seckill")
@RequiredArgsConstructor
@InnerApi
public class SeckillInnerController {

    private final SeckillActivityMapper activityMapper;
    private final LocalMessageMapper localMessageMapper;

    /**
     * 扣减秒杀活动库存（数据库）
     * 由订单服务调用，保证Redis和数据库库存一致
     */
    @Operation(summary = "扣减秒杀库存", description = "扣减数据库中的秒杀库存")
    @PostMapping("/{activityId}/deduct-stock")
    public Result<Boolean> deductStock(@PathVariable Long activityId) {
        int rows = activityMapper.deductStock(activityId);
        if (rows > 0) {
            log.info("扣减秒杀库存成功: activityId={}", activityId);
            return Result.success(true);
        } else {
            log.warn("扣减秒杀库存失败，库存不足: activityId={}", activityId);
            return Result.success(false);
        }
    }
    
    /**
     * 重置本地消息状态为待重试
     * 消费者处理失败时调用，让定时任务可以重试
     */
    @Operation(summary = "重置消息状态", description = "消费者处理失败时重置消息状态为待重试")
    @PostMapping("/message/{messageId}/reset")
    public Result<Void> resetMessageStatus(@PathVariable String messageId) {
        int rows = localMessageMapper.resetToPending(messageId);
        if (rows > 0) {
            log.info("重置消息状态成功: messageId={}", messageId);
            return Result.success(null);
        } else {
            log.warn("重置消息状态失败，消息不存在: messageId={}", messageId);
            return Result.failed("消息不存在");
        }
    }
}
