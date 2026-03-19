package com.fnusale.im.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.UserAuthAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证审核通知消费者
 * 消费用户认证审核消息，通知用户认证结果
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.USER_AUTH_TOPIC,
        consumerGroup = "user-auth-notify-consumer-group",
        selectorExpression = RocketMQConstants.USER_AUTH_TAG_NOTIFY
)
@RequiredArgsConstructor
public class UserAuthAuditNotifyConsumer implements RocketMQListener<UserAuthAuditEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String USER_AUTH_NOTIFY_KEY_PREFIX = "user:auth:notify:";

    @Override
    public void onMessage(UserAuthAuditEvent event) {
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到用户认证审核通知消息, userId: {}, eventId: {}", userId, eventId);

        // 幂等性检查
        String idempotentKey = USER_AUTH_NOTIFY_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("用户认证审核通知已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            // 发送通知
            sendAuthNotification(event);

            log.info("用户认证审核通知发送成功, userId: {}", userId);
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("用户认证审核通知发送失败, userId: {}", userId, e);
            throw e;
        }
    }

    private void sendAuthNotification(UserAuthAuditEvent event) {
        String title = "PASS".equals(event.getAuditResult()) ? "校园身份认证通过" : "校园身份认证驳回";
        String content;

        if ("PASS".equals(event.getAuditResult())) {
            content = String.format("恭喜您，%s！您的校园身份认证已通过审核。", event.getUsername());
        } else {
            content = String.format("您的校园身份认证未通过审核。原因：%s", event.getRejectReason());
        }

        // TODO: 调用 IM 服务发送站内消息
        // TODO: 可选发送短信或邮件通知
        // pushMessage(event.getUserId(), title, content, null);

        log.info("发送用户认证审核通知: userId={}, title={}, content={}", event.getUserId(), title, content);
    }
}