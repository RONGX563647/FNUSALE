package com.fnusale.im.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.UserBanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户封禁通知消费者
 * 消费用户封禁/解封消息，通知用户
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.USER_BAN_TOPIC,
        consumerGroup = "user-ban-notify-consumer-group",
        selectorExpression = RocketMQConstants.USER_BAN_TAG_NOTIFY
)
@RequiredArgsConstructor
public class UserBanNotifyConsumer implements RocketMQListener<UserBanEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String USER_BAN_NOTIFY_KEY_PREFIX = "user:ban:notify:";

    @Override
    public void onMessage(UserBanEvent event) {
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到用户封禁通知消息, userId: {}, eventId: {}", userId, eventId);

        // 幂等性检查
        String idempotentKey = USER_BAN_NOTIFY_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("用户封禁通知已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            // 发送通知
            sendBanNotification(event);

            log.info("用户封禁通知发送成功, userId: {}", userId);
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("用户封禁通知发送失败, userId: {}", userId, e);
            throw e;
        }
    }

    private void sendBanNotification(UserBanEvent event) {
        String title = "BAN".equals(event.getOperateType()) ? "账号被封禁" : "账号已解封";
        String content;

        if ("BAN".equals(event.getOperateType())) {
            content = String.format("您的账号已被封禁。原因：%s", event.getReason());
        } else {
            content = "您的账号已解封，可以正常使用了。";
        }

        // TODO: 调用 IM 服务发送站内消息
        // pushMessage(event.getUserId(), title, content, null);

        log.info("发送用户封禁通知: userId={}, title={}, content={}", event.getUserId(), title, content);
    }
}