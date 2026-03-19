package com.fnusale.marketing.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 死信队列消费者 - 优惠券领取
 * 处理消费失败超过最大重试次数的消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "%DLQ%coupon-receive-consumer-group",
        consumerGroup = "dlq-coupon-receive-consumer-group"
)
@RequiredArgsConstructor
public class CouponReceiveDLQConsumer implements RocketMQListener<String> {

    private final StringRedisTemplate redisTemplate;

    private static final String DLQ_PROCESSED_KEY_PREFIX = "dlq:processed:";

    @Override
    public void onMessage(String message) {
        log.warn("收到优惠券领取死信消息: {}", message);

        try {
            // 记录死信消息
            String dlqKey = DLQ_PROCESSED_KEY_PREFIX + System.currentTimeMillis();
            redisTemplate.opsForValue().set(dlqKey, message, 7, TimeUnit.DAYS);

            // TODO: 发送告警通知，可能需要人工补偿
            // 提取 userId 和 couponId，人工处理或通知用户

            log.info("优惠券领取死信消息已记录: key={}", dlqKey);
        } catch (Exception e) {
            log.error("处理优惠券领取死信消息失败", e);
        }
    }
}