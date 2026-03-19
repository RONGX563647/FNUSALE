package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.Order;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.PayStatus;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单消费者
 * 消费秒杀订单消息，异步创建订单
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.SECKILL_ORDER_TOPIC,
        consumerGroup = "seckill-order-consumer-group",
        selectorExpression = RocketMQConstants.SECKILL_ORDER_TAG_CREATE
)
@RequiredArgsConstructor
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderEvent> {

    private final StringRedisTemplate redisTemplate;
    private final OrderMapper orderMapper;

    /**
     * 订单创建幂等性 Key 前缀
     */
    private static final String SECKILL_ORDER_KEY_PREFIX = "seckill:order:";
    private static final String ORDER_NO_KEY_PREFIX = "order:no:";

    @Override
    public void onMessage(SeckillOrderEvent event) {
        Long userId = event.getUserId();
        Long activityId = event.getActivityId();
        String eventId = event.getEventId();

        log.info("收到秒杀订单消息, userId: {}, activityId: {}, eventId: {}", userId, activityId, eventId);

        // 幂等性检查
        String idempotentKey = SECKILL_ORDER_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("秒杀订单已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            createSeckillOrder(event);
            log.info("秒杀订单创建成功, userId: {}, activityId: {}", userId, activityId);
        } catch (Exception e) {
            // 处理失败，删除幂等性 Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("秒杀订单创建失败, userId: {}, activityId: {}", userId, activityId, e);
            throw e;
        }
    }

    /**
     * 创建秒杀订单
     */
    private void createSeckillOrder(SeckillOrderEvent event) {
        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(event.getUserId());
        order.setProductId(event.getProductId());
        order.setProductPrice(event.getSeckillPrice());
        order.setCouponDeductAmount(BigDecimal.ZERO);
        order.setActualPayAmount(event.getSeckillPrice());
        order.setPayType("SECKILL"); // 秒杀订单特殊标记
        order.setPayStatus(PayStatus.PAID.getCode()); // 秒杀订单默认已支付
        order.setOrderStatus(OrderStatus.WAIT_PICK.getCode());

        orderMapper.insert(order);

        log.info("创建秒杀订单成功: orderId={}, orderNo={}, userId={}, productId={}, seckillPrice={}",
                order.getId(), orderNo, event.getUserId(), event.getProductId(), event.getSeckillPrice());
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String key = ORDER_NO_KEY_PREFIX + dateStr;
        Long seq = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 2, TimeUnit.DAYS);
        return "XS" + dateStr + String.format("%06d", seq);
    }
}