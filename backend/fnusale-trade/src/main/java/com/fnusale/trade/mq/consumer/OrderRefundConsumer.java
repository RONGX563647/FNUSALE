package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.trade.client.ProductClient;
import com.fnusale.trade.event.OrderRefundEvent;
import com.fnusale.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 退款消费者
 * 处理退款的异步操作：
 * 1. 执行退款（调用支付服务）
 * 2. 更新订单状态
 * 3. 恢复商品状态
 * 4. 通知买卖双方
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_REFUND_TOPIC,
        consumerGroup = "order-refund-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_REFUND_TAG_PROCESS
)
@RequiredArgsConstructor
public class OrderRefundConsumer implements RocketMQListener<OrderRefundEvent> {

    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final StringRedisTemplate redisTemplate;

    private static final String ORDER_REFUND_KEY_PREFIX = "order:refund:";

    @Override
    public void onMessage(OrderRefundEvent event) {
        Long orderId = event.getOrderId();
        String eventId = event.getEventId();

        log.info("收到退款处理消息，orderId: {}, amount: {}, eventId: {}", orderId, event.getRefundAmount(), eventId);

        // 幂等性检查
        String idempotentKey = ORDER_REFUND_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("退款消息已处理，跳过，orderId: {}", orderId);
            return;
        }

        try {
            // 1. 执行退款（模拟退款）
            processRefund(event);

            // 2. 更新订单状态为已退款
            orderMapper.updateRefundSuccess(orderId);
            log.info("订单状态更新为已退款，orderId: {}", orderId);

            // 3. 恢复商品状态为上架
            try {
                productClient.updateProductStatus(event.getProductId(), ProductStatus.ON_SHELF.getCode());
                log.info("商品状态恢复为上架，productId: {}", event.getProductId());
            } catch (Exception e) {
                log.error("恢复商品状态失败，productId: {}", event.getProductId(), e);
            }

            // 4. 通知买卖双方
            notifyRefund(event);

            log.info("退款处理完成，orderId: {}", orderId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("退款处理失败，orderId: {}", orderId, e);
            throw e;
        }
    }

    /**
     * 执行退款
     */
    private void processRefund(OrderRefundEvent event) {
        // 模拟退款处理
        // 实际项目中需要调用支付服务（微信、支付宝等）进行真实退款
        log.info("执行退款，orderId: {}, amount: {}, payType: {}",
                event.getOrderId(), event.getRefundAmount(), event.getPayType());

        // TODO: 根据payType调用对应的支付服务进行退款
        // switch (event.getPayType()) {
        //     case "WECHAT":
        //         wechatPayService.refund(event.getOrderId(), event.getRefundAmount());
        //         break;
        //     case "ALIPAY":
        //         alipayService.refund(event.getOrderId(), event.getRefundAmount());
        //         break;
        //     case "CAMPUS_CARD":
        //         campusCardService.refund(event.getOrderId(), event.getRefundAmount());
        //         break;
        // }
    }

    /**
     * 通知退款结果
     */
    private void notifyRefund(OrderRefundEvent event) {
        // TODO: 集成IM服务通知买家退款成功
        log.info("通知买家退款成功，buyerId: {}, orderId: {}", event.getBuyerId(), event.getOrderId());

        // TODO: 集成IM服务通知卖家订单已退款
        log.info("通知卖家订单已退款，sellerId: {}, orderId: {}", event.getSellerId(), event.getOrderId());
    }
}