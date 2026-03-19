package com.fnusale.marketing.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 死信队列消费者 - 优惠券发放
 * 处理消费失败超过最大重试次数的消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "%DLQ%coupon-grant-consumer-group",
        consumerGroup = "dlq-coupon-grant-consumer-group"
)
@RequiredArgsConstructor
public class CouponGrantDLQConsumer implements RocketMQListener<String> {

    private final StringRedisTemplate redisTemplate;

    private static final String DLQ_PROCESSED_KEY_PREFIX = "dlq:processed:";

    @Override
    public void onMessage(String message) {
        log.warn("收到优惠券发放死信消息: {}", message);

        try {
            // 解析消息，提取关键信息
            // 实际场景中可能需要人工介入或通知管理员

            // 记录死信消息到 Redis（用于后续处理）
            String dlqKey = DLQ_PROCESSED_KEY_PREFIX + System.currentTimeMillis();
            redisTemplate.opsForValue().set(dlqKey, message, 7, TimeUnit.DAYS);

            // TODO: 发送告警通知
            // alertService.sendDLQAlert("coupon-grant", message);

            log.info("优惠券发放死信消息已记录: key={}", dlqKey);
        } catch (Exception e) {
            log.error("处理优惠券发放死信消息失败", e);
            // 不抛出异常，避免死信队列也失败
        }
    }
}