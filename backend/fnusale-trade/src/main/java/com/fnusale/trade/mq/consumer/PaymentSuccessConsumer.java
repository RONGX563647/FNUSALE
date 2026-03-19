package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.trade.client.MarketingClient;
import com.fnusale.trade.event.OrderPayEvent;
import com.fnusale.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 支付成功消费者
 * 处理支付成功后的异步操作：
 * 1. 核销优惠券
 * 2. 通知买家
 * 3. 通知卖家
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_PAY_TOPIC,
        consumerGroup = "order-pay-success-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_PAY_TAG_SUCCESS
)
@RequiredArgsConstructor
public class PaymentSuccessConsumer implements RocketMQListener<OrderPayEvent> {

    private final OrderMapper orderMapper;
    private final MarketingClient marketingClient;
    private final StringRedisTemplate redisTemplate;

    private static final String PAY_SUCCESS_KEY_PREFIX = "pay:success:";

    @Override
    public void onMessage(OrderPayEvent event) {
        Long orderId = event.getOrderId();
        String eventId = event.getEventId();

        log.info("收到支付成功消息，orderId: {}, buyerId: {}, eventId: {}", orderId, event.getBuyerId(), eventId);

        // 幂等性检查
        String idempotentKey = PAY_SUCCESS_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("支付成功消息已处理，跳过，orderId: {}", orderId);
            return;
        }

        try {
            // 1. 核销优惠券（如果使用了）
            if (event.getCouponId() != null) {
                try {
                    marketingClient.useCoupon(event.getCouponId(), orderId);
                    log.info("优惠券核销成功，couponId: {}, orderId: {}", event.getCouponId(), orderId);
                } catch (Exception e) {
                    log.error("优惠券核销失败，couponId: {}, orderId: {}", event.getCouponId(), orderId, e);
                    // 优惠券核销失败不影响主流程，记录日志即可
                }
            }

            // 2. 通知买家（这里可以集成IM或推送服务）
            notifyBuyer(event);

            // 3. 通知卖家
            notifySeller(event);

            log.info("支付成功后续处理完成，orderId: {}", orderId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("支付成功消息处理失败，orderId: {}", orderId, e);
            throw e;
        }
    }

    /**
     * 通知买家支付成功
     */
    private void notifyBuyer(OrderPayEvent event) {
        // TODO: 集成IM服务发送站内信
        // TODO: 集成推送服务发送通知
        log.info("通知买家支付成功，buyerId: {}, orderId: {}", event.getBuyerId(), event.getOrderId());
    }

    /**
     * 通知卖家有新订单
     */
    private void notifySeller(OrderPayEvent event) {
        // TODO: 集成IM服务发送站内信
        // TODO: 集成推送服务发送通知
        log.info("通知卖家有新订单，sellerId: {}, orderId: {}", event.getSellerId(), event.getOrderId());
    }
}