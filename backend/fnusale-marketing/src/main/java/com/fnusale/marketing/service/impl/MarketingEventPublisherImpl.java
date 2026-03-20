package com.fnusale.marketing.service.impl;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.CouponReceiveEvent;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.common.event.SeckillWarmUpEvent;
import com.fnusale.marketing.service.MarketingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 营销事件发布服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MarketingEventPublisherImpl implements MarketingEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 顺序消息队列选择器
     * 根据哈希键选择队列，相同哈希键的消息发送到同一队列
     */
    private final MessageQueueSelector queueSelector = (mqs, msg, arg) -> {
        String hashKey = (String) arg;
        int index = Math.abs(hashKey.hashCode()) % mqs.size();
        return mqs.get(index);
    };

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
    public void publishSeckillOrderEventOrdered(SeckillOrderEvent event, String hashKey) {
        String destination = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.SECKILL_ORDER_TAG_CREATE;

        try {
            // 使用同步发送顺序消息
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                    destination,
                    event,
                    hashKey
            );
            log.info("秒杀订单顺序消息发送成功, eventId: {}, hashKey: {}, msgId: {}, queueId: {}",
                    event.getEventId(), hashKey, sendResult.getMsgId(), sendResult.getMessageQueue().getQueueId());
        } catch (Exception e) {
            log.error("秒杀订单顺序消息发送失败, eventId: {}, hashKey: {}", event.getEventId(), hashKey, e);
            throw new RuntimeException("消息发送失败", e);
        }
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

    @Override
    public void publishCouponGrantEventOrdered(CouponGrantEvent event, String hashKey) {
        String destination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;

        try {
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, event, hashKey);
            log.info("优惠券发放顺序消息发送成功, eventId: {}, hashKey: {}, msgId: {}, queueId: {}",
                    event.getEventId(), hashKey, sendResult.getMsgId(), sendResult.getMessageQueue().getQueueId());
        } catch (Exception e) {
            log.error("优惠券发放顺序消息发送失败, eventId: {}, hashKey: {}", event.getEventId(), hashKey, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    @Override
    public void publishCouponGrantBatch(List<CouponGrantEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        String destination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;

        // 构建批量消息
        List<org.springframework.messaging.Message<CouponGrantEvent>> messages = events.stream()
                .map(event -> MessageBuilder.withPayload(event).build())
                .toList();

        try {
            SendResult sendResult = rocketMQTemplate.syncSend(destination, messages);
            log.info("批量优惠券发放消息发送成功, 数量: {}, msgId: {}", events.size(), sendResult.getMsgId());
        } catch (Exception e) {
            log.error("批量优惠券发放消息发送失败, 数量: {}", events.size(), e);
            // 降级：逐条发送
            events.forEach(this::publishCouponGrantEvent);
        }
    }

    @Override
    public void publishCouponGrantBatchOrdered(List<CouponGrantEvent> events, List<String> hashKeys) {
        if (events == null || events.isEmpty()) {
            return;
        }

        if (hashKeys == null || hashKeys.size() != events.size()) {
            throw new IllegalArgumentException("hashKeys 数量必须与 events 数量一致");
        }

        String destination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < events.size(); i++) {
            CouponGrantEvent event = events.get(i);
            String hashKey = hashKeys.get(i);

            try {
                SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, event, hashKey);
                successCount++;
                log.debug("批量顺序消息发送成功, eventId: {}, hashKey: {}, msgId: {}",
                        event.getEventId(), hashKey, sendResult.getMsgId());
            } catch (Exception e) {
                failCount++;
                log.error("批量顺序消息发送失败, eventId: {}, hashKey: {}", event.getEventId(), hashKey, e);
            }
        }

        log.info("批量优惠券发放顺序消息发送完成, 成功: {}, 失败: {}", successCount, failCount);
    }

    @Override
    public void publishCouponReceiveEvent(CouponReceiveEvent event) {
        String destination = RocketMQConstants.COUPON_RECEIVE_TOPIC + ":" + RocketMQConstants.COUPON_RECEIVE_TAG_RECEIVE;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("优惠券领取消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("优惠券领取消息发送失败, eventId: {}, couponId: {}", event.getEventId(), event.getCouponId(), e);
            }
        });
    }

    @Override
    public void publishCouponReceiveEventOrdered(CouponReceiveEvent event, String hashKey) {
        String destination = RocketMQConstants.COUPON_RECEIVE_TOPIC + ":" + RocketMQConstants.COUPON_RECEIVE_TAG_RECEIVE;

        try {
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, event, hashKey);
            log.info("优惠券领取顺序消息发送成功, eventId: {}, hashKey: {}, msgId: {}, queueId: {}",
                    event.getEventId(), hashKey, sendResult.getMsgId(), sendResult.getMessageQueue().getQueueId());
        } catch (Exception e) {
            log.error("优惠券领取顺序消息发送失败, eventId: {}, hashKey: {}", event.getEventId(), hashKey, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    @Override
    public void publishSeckillWarmUpEvent(SeckillWarmUpEvent event, int delayLevel) {
        String destination = RocketMQConstants.SECKILL_WARMUP_TOPIC + ":" + RocketMQConstants.SECKILL_WARMUP_TAG_STOCK;

        // 构建延迟消息
        org.springframework.messaging.Message<SeckillWarmUpEvent> message = MessageBuilder
                .withPayload(event)
                .build();

        // RocketMQ 延迟消息级别: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        // delayLevel: 1-18
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message, 3000, delayLevel);
            log.info("秒杀预热延迟消息发送成功, activityId: {}, delayLevel: {}, msgId: {}",
                    event.getActivityId(), delayLevel, sendResult.getMsgId());
        } catch (Exception e) {
            log.error("秒杀预热延迟消息发送失败, activityId: {}", event.getActivityId(), e);
        }
    }

    @Override
    public void publishCouponExpireReminderEvent(CouponExpireReminderEvent event) {
        String destination = RocketMQConstants.COUPON_EXPIRE_REMINDER_TOPIC + ":" + RocketMQConstants.COUPON_EXPIRE_REMINDER_TAG_NOTIFY;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("优惠券过期提醒消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("优惠券过期提醒消息发送失败, eventId: {}", event.getEventId(), e);
            }
        });
    }

    @Override
    public void sendRawMessage(String topic, String tag, String messageContent) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.syncSend(destination, messageContent);
        log.info("原始消息发送成功: destination={}", destination);
    }

    @Override
    public boolean sendSync(String topic, String tag, Object message) {
        try {
            String destination = topic + ":" + tag;
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
            log.info("同步消息发送成功: destination={}, msgId={}", destination, sendResult.getMsgId());
            return true;
        } catch (Exception e) {
            log.error("同步消息发送失败: topic={}, tag={}", topic, tag, e);
            return false;
        }
    }
}