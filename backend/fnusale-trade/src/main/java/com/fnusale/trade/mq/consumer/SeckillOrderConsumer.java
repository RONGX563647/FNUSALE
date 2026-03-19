package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.SeckillOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

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

    /**
     * 订单创建幂等性 Key 前缀
     */
    private static final String SECKILL_ORDER_KEY_PREFIX = "seckill:order:";

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
            // TODO: 调用订单服务创建订单
            // 1. 检查库存是否足够（再次确认）
            // 2. 创建订单记录
            // 3. 扣减真实库存（同步到数据库）
            // 4. 发送订单创建成功通知

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
        // TODO: 实现订单创建逻辑
        // 1. 构建 Order 对象
        // 2. 设置订单类型为 SECKILL
        // 3. 设置秒杀价格
        // 4. 插入订单表
        // 5. 插入订单商品表
        // 6. 更新秒杀活动库存

        log.info("创建秒杀订单: userId={}, productId={}, seckillPrice={}",
                event.getUserId(), event.getProductId(), event.getSeckillPrice());
    }
}