package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.marketing.config.IdempotentKeyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券过期提醒消费者
 * 消费过期提醒消息，推送通知给用户
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.COUPON_EXPIRE_REMINDER_TOPIC,
        consumerGroup = "coupon-expire-reminder-consumer-group",
        selectorExpression = RocketMQConstants.COUPON_EXPIRE_REMINDER_TAG_NOTIFY,
        maxReconsumeTimes = 2,
        consumeThreadMin = 5,
        consumeThreadMax = 10,
        consumeTimeout = 5,
        messageModel = MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class CouponExpireReminderConsumer implements RocketMQListener<CouponExpireReminderEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_EXPIRE_REMINDER_KEY_PREFIX = "coupon:expire:reminder:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM月dd日");

    @Override
    public void onMessage(CouponExpireReminderEvent event) {
        String eventId = event.getEventId();

        log.info("收到优惠券过期提醒消息, eventId: {}", eventId);

        String idempotentKey = COUPON_EXPIRE_REMINDER_KEY_PREFIX + eventId;
        long expireSeconds = IdempotentKeyConfig.calculateLowPriorityExpireSeconds();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", expireSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.info("优惠券过期提醒已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            if (event.getUserCouponList() != null && !event.getUserCouponList().isEmpty()) {
                processBatchReminder(event);
            } else {
                processSingleReminder(event);
            }

            log.info("优惠券过期提醒发送成功, eventId: {}", eventId);
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("优惠券过期提醒发送失败, eventId: {}", eventId, e);
            throw e;
        }
    }

    private void processBatchReminder(CouponExpireReminderEvent event) {
        for (CouponExpireReminderEvent.UserCouponInfo info : event.getUserCouponList()) {
            try {
                sendExpireNotification(info.getUserId(), info.getCouponName(),
                        info.getReduceAmount(), info.getExpireTime());
            } catch (Exception e) {
                log.warn("发送优惠券过期提醒失败, userId: {}, userCouponId: {}",
                        info.getUserId(), info.getUserCouponId(), e);
            }
        }
    }

    private void processSingleReminder(CouponExpireReminderEvent event) {
        sendExpireNotification(event.getUserId(), event.getCouponName(),
                event.getReduceAmount(), event.getExpireTime());
    }

    private void sendExpireNotification(Long userId, String couponName,
                                         java.math.BigDecimal reduceAmount,
                                         java.time.LocalDateTime expireTime) {
        String expireDateStr = expireTime.format(DATE_FORMATTER);
        String title = "优惠券即将过期";
        String content = String.format("您的优惠券「%s」（优惠%.2f元）将于%s过期，请尽快使用！",
                couponName, reduceAmount, expireDateStr);

        log.info("发送优惠券过期提醒: userId={}, title={}, content={}", userId, title, content);
    }
}