package com.fnusale.marketing.service.impl;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.marketing.service.MarketingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

/**
 * 营销事件发布服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingEventPublisherImpl implements MarketingEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publishSeckillOrderEvent(SeckillOrderEvent event) {
        String destination = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.SECKILL_ORDER_TAG_CREATE;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("秒杀订单消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("秒杀订单消息发送失败, eventId: {}, userId: {}", event.getEventId(), event.getUserId(), e);
            }
        });
    }

    @Override
    public void publishSeckillReminderEvent(SeckillReminderEvent event) {
        String destination = RocketMQConstants.SECKILL_REMINDER_TOPIC + ":" + RocketMQConstants.SECKILL_REMINDER_TAG_PUSH;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("秒杀提醒消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("秒杀提醒消息发送失败, eventId: {}, activityId: {}", event.getEventId(), event.getActivityId(), e);
            }
        });
    }

    @Override
    public void publishCouponGrantEvent(CouponGrantEvent event) {
        String destination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("优惠券发放消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("优惠券发放消息发送失败, eventId: {}, couponId: {}", event.getEventId(), event.getCouponId(), e);
            }
        });
    }
}