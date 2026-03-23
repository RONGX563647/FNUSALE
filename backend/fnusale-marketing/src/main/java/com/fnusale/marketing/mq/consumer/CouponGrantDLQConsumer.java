package com.fnusale.marketing.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.marketing.service.AlertService;
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

    private final AlertService alertService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DLQ_PROCESSED_KEY_PREFIX = "dlq:coupon:grant:";
    private static final String DLQ_TOPIC = "coupon-grant-consumer-group";

    @Override
    public void onMessage(String message) {
        log.error("收到优惠券发放死信消息: {}", message);

        try {
            CouponGrantEvent event = parseEvent(message);

            recordFailedMessage(event);

            alertService.sendDLQAlert(
                    DLQ_TOPIC,
                    "coupon-grant-consumer-group",
                    buildAlertContent(event, message)
            );

            log.info("优惠券发放死信消息已处理: userId={}, couponId={}",
                    event != null ? event.getUserId() : "unknown",
                    event != null ? event.getCouponId() : "unknown");
        } catch (Exception e) {
            log.error("处理优惠券发放死信消息失败", e);
        }
    }

    private CouponGrantEvent parseEvent(String message) {
        try {
            return objectMapper.readValue(message, CouponGrantEvent.class);
        } catch (Exception e) {
            log.warn("解析死信消息失败: {}", message);
            return null;
        }
    }

    private void recordFailedMessage(CouponGrantEvent event) {
        try {
            String dlqKey = DLQ_PROCESSED_KEY_PREFIX + System.currentTimeMillis();
            String value = event != null
                    ? String.format("userId=%d,couponId=%d,eventId=%s,batchId=%s",
                            event.getUserId(), event.getCouponId(), event.getEventId(), event.getBatchId())
                    : message;
            redisTemplate.opsForValue().set(dlqKey, value, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录死信消息到Redis失败", e);
        }
    }

    private String buildAlertContent(CouponGrantEvent event, String rawMessage) {
        if (event != null) {
            return String.format("用户ID: %d\n优惠券ID: %d\n批次ID: %s\n事件ID: %s",
                    event.getUserId(), event.getCouponId(), event.getBatchId(), event.getEventId());
        }
        return rawMessage.length() > 500 ? rawMessage.substring(0, 500) : rawMessage;
    }
}