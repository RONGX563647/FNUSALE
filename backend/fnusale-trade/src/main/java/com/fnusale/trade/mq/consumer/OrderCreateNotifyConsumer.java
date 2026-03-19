package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.trade.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单创建通知消费者
 * 处理订单创建后的通知操作：
 * 1. 通知卖家有新订单
 * 2. 发送短信/推送通知
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_CREATE_TOPIC,
        consumerGroup = "order-create-notify-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_CREATE_TAG_NOTIFY_SELLER
)
@RequiredArgsConstructor
public class OrderCreateNotifyConsumer implements RocketMQListener<OrderEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String ORDER_NOTIFY_KEY_PREFIX = "order:notify:";

    @Override
    public void onMessage(OrderEvent event) {
        Long orderId = event.getOrderId();
        Long sellerId = event.getSellerId();
        String eventId = event.getEventId();

        log.info("收到订单创建通知消息，orderId: {}, sellerId: {}, eventId: {}",
                orderId, sellerId, eventId);

        // 幂等性检查
        String idempotentKey = ORDER_NOTIFY_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("订单创建通知消息已处理，跳过，orderId: {}", orderId);
            return;
        }

        try {
            // 1. 发送站内信通知卖家
            notifySellerNewOrder(event);

            // 2. 发送短信/推送通知（如果卖家开启了通知）
            sendPushNotification(event);

            log.info("订单创建通知处理完成，orderId: {}, sellerId: {}", orderId, sellerId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("订单创建通知处理失败，orderId: {}", orderId, e);
            throw e;
        }
    }

    /**
     * 通知卖家有新订单
     */
    private void notifySellerNewOrder(OrderEvent event) {
        // TODO: 集成IM服务发送站内信
        // IM消息内容：
        // 标题：您有新的订单
        // 内容：您的商品有买家下单啦！订单号：{orderNo}，请及时处理。
        log.info("发送站内信通知卖家，sellerId: {}, orderId: {}", event.getSellerId(), event.getOrderId());
    }

    /**
     * 发送推送通知
     */
    private void sendPushNotification(OrderEvent event) {
        // TODO: 集成推送服务（极光、个推等）
        // TODO: 发送短信通知（阿里云短信、腾讯云短信等）
        log.info("发送推送通知，sellerId: {}, orderId: {}", event.getSellerId(), event.getOrderId());
    }
}