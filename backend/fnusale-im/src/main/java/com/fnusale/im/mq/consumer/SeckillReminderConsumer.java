package com.fnusale.im.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.SeckillReminderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀提醒消费者
 * 消费秒杀提醒消息，推送通知给用户
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.SECKILL_REMINDER_TOPIC,
        consumerGroup = "seckill-reminder-consumer-group",
        selectorExpression = RocketMQConstants.SECKILL_REMINDER_TAG_PUSH
)
@RequiredArgsConstructor
public class SeckillReminderConsumer implements RocketMQListener<SeckillReminderEvent> {

    private final StringRedisTemplate redisTemplate;

    /**
     * 提醒幂等性 Key 前缀
     */
    private static final String SECKILL_REMINDER_KEY_PREFIX = "seckill:reminder:";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void onMessage(SeckillReminderEvent event) {
        String eventId = event.getEventId();
        Long activityId = event.getActivityId();

        log.info("收到秒杀提醒消息, activityId: {}, eventId: {}, 用户数: {}",
                activityId, eventId, event.getUserIds().size());

        // 幂等性检查
        String idempotentKey = SECKILL_REMINDER_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("秒杀提醒已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            // 推送提醒给所有用户
            pushReminderToUsers(event);

            log.info("秒杀提醒推送成功, activityId: {}, 用户数: {}", activityId, event.getUserIds().size());
        } catch (Exception e) {
            // 处理失败，删除幂等性 Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("秒杀提醒推送失败, activityId: {}", activityId, e);
            throw e;
        }
    }

    /**
     * 推送提醒给用户
     */
    private void pushReminderToUsers(SeckillReminderEvent event) {
        String startTimeStr = event.getStartTime().format(TIME_FORMATTER);
        String title = "秒杀即将开始";
        String content = String.format("您关注的「%s」将于 %s 开始秒杀，不要错过哦！",
                event.getProductName(), startTimeStr);

        for (Long userId : event.getUserIds()) {
            try {
                // TODO: 调用 IM 服务发送站内消息
                // 或者调用 WebSocket 推送实时消息
                pushNotification(userId, title, content, event.getActivityId());
            } catch (Exception e) {
                log.warn("推送秒杀提醒失败, userId: {}", userId, e);
            }
        }
    }

    /**
     * 推送通知
     */
    private void pushNotification(Long userId, String title, String content, Long activityId) {
        // TODO: 实现实际的推送逻辑
        // 1. 构建 IMMessage 对象
        // 2. 设置消息类型为 SECKILL_REMINDER
        // 3. 通过 WebSocket 推送或存储到消息表

        log.debug("推送秒杀提醒: userId={}, title={}, content={}", userId, title, content);
    }
}