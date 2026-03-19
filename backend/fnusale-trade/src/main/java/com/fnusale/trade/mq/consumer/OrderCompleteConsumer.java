package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.trade.client.ProductClient;
import com.fnusale.trade.event.OrderCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单完成消费者
 * 处理确认收货后的异步操作：
 * 1. 更新商品状态为已售出
 * 2. 通知卖家收款
 * 3. 更新交易统计
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_COMPLETE_TOPIC,
        consumerGroup = "order-complete-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_COMPLETE_TAG_UPDATE_PRODUCT
)
@RequiredArgsConstructor
public class OrderCompleteConsumer implements RocketMQListener<OrderCompleteEvent> {

    private final ProductClient productClient;
    private final StringRedisTemplate redisTemplate;

    private static final String ORDER_COMPLETE_KEY_PREFIX = "order:complete:";

    @Override
    public void onMessage(OrderCompleteEvent event) {
        Long orderId = event.getOrderId();
        Long productId = event.getProductId();
        String eventId = event.getEventId();

        log.info("收到订单完成消息，orderId: {}, productId: {}, eventId: {}", orderId, productId, eventId);

        // 幂等性检查
        String idempotentKey = ORDER_COMPLETE_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("订单完成消息已处理，跳过，orderId: {}", orderId);
            return;
        }

        try {
            // 1. 更新商品状态为已售出
            try {
                productClient.updateProductStatus(productId, ProductStatus.SOLD_OUT.getCode());
                log.info("商品状态更新为已售出，productId: {}", productId);
            } catch (Exception e) {
                log.error("更新商品状态失败，productId: {}", productId, e);
                // 抛出异常触发重试
                throw e;
            }

            // 2. 通知卖家收款成功
            notifySellerPayment(event);

            // 3. 更新用户交易统计（可以发送到用户服务处理）
            updateUserTradeStats(event);

            log.info("订单完成后续处理完成，orderId: {}", orderId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("订单完成消息处理失败，orderId: {}", orderId, e);
            throw e;
        }
    }

    /**
     * 通知卖家收款成功
     */
    private void notifySellerPayment(OrderCompleteEvent event) {
        // TODO: 集成IM服务发送站内信
        // TODO: 集成推送服务发送通知
        log.info("通知卖家收款成功，sellerId: {}, amount: {}", event.getSellerId(), event.getAmount());
    }

    /**
     * 更新用户交易统计
     */
    private void updateUserTradeStats(OrderCompleteEvent event) {
        // TODO: 发送消息到用户服务更新交易统计
        log.info("更新用户交易统计，sellerId: {}, buyerId: {}", event.getSellerId(), event.getBuyerId());
    }
}