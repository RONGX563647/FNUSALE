package com.fnusale.im.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.ProductAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 商品审核通知消费者
 * 消费商品审核消息，通知用户审核结果
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.PRODUCT_AUDIT_TOPIC,
        consumerGroup = "product-audit-notify-consumer-group",
        selectorExpression = RocketMQConstants.PRODUCT_AUDIT_TAG_NOTIFY
)
@RequiredArgsConstructor
public class ProductAuditNotifyConsumer implements RocketMQListener<ProductAuditEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String PRODUCT_AUDIT_NOTIFY_KEY_PREFIX = "product:audit:notify:";

    @Override
    public void onMessage(ProductAuditEvent event) {
        Long productId = event.getProductId();
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到商品审核通知消息, productId: {}, userId: {}, eventId: {}", productId, userId, eventId);

        // 幂等性检查
        String idempotentKey = PRODUCT_AUDIT_NOTIFY_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("商品审核通知已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            // 发送通知
            sendAuditNotification(event);

            log.info("商品审核通知发送成功, productId: {}, userId: {}", productId, userId);
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("商品审核通知发送失败, productId: {}", productId, e);
            throw e;
        }
    }

    private void sendAuditNotification(ProductAuditEvent event) {
        String title = "PASS".equals(event.getAuditResult()) ? "商品审核通过" : "商品审核驳回";
        String content;

        if ("PASS".equals(event.getAuditResult())) {
            content = String.format("您发布的商品「%s」已通过审核，现已上架。", event.getProductName());
        } else {
            content = String.format("您发布的商品「%s」审核未通过。原因：%s", event.getProductName(), event.getRejectReason());
        }

        // TODO: 调用 IM 服务发送站内消息
        // pushMessage(event.getUserId(), title, content, event.getProductId());

        log.info("发送商品审核通知: userId={}, title={}, content={}", event.getUserId(), title, content);
    }
}