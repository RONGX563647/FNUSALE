package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.PayStatus;
import com.fnusale.trade.event.OrderEvent;
import com.fnusale.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单超时消费者
 * 处理订单超时未支付自动取消
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_TIMEOUT_TOPIC,
        consumerGroup = "order-timeout-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_TIMEOUT_TAG_CANCEL
)
@RequiredArgsConstructor
public class OrderTimeoutConsumer implements RocketMQListener<OrderEvent> {

    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 幂等性Key前缀
     */
    private static final String ORDER_TIMEOUT_KEY_PREFIX = "order:timeout:";

    @Override
    public void onMessage(OrderEvent event) {
        Long orderId = event.getOrderId();
        String eventId = event.getEventId();

        log.info("收到订单超时消息，orderId: {}, eventId: {}", orderId, eventId);

        // 幂等性检查
        String idempotentKey = ORDER_TIMEOUT_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("订单超时消息已处理，跳过，orderId: {}", orderId);
            return;
        }

        try {
            // 查询订单当前状态
            var order = orderMapper.selectById(orderId);
            if (order == null) {
                log.warn("订单不存在，orderId: {}", orderId);
                return;
            }

            // 检查订单状态：只有未支付状态才取消
            if (!OrderStatus.UNPAID.getCode().equals(order.getOrderStatus())) {
                log.info("订单已不是待支付状态，跳过取消，orderId: {}, status: {}", orderId, order.getOrderStatus());
                return;
            }

            // 检查支付状态
            if (PayStatus.PAID.getCode().equals(order.getPayStatus())) {
                log.info("订单已支付，跳过取消，orderId: {}", orderId);
                return;
            }

            // 取消订单
            orderMapper.cancelOrder(orderId, "超时未支付，系统自动取消");

            log.info("订单超时取消成功，orderId: {}, orderNo: {}", orderId, event.getOrderNo());

        } catch (Exception e) {
            // 处理失败，删除幂等性Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("订单超时处理失败，orderId: {}", orderId, e);
            throw e;
        }
    }
}