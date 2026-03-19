package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.SeckillWarmUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀预热消费者
 * 消费秒杀预热消息，将库存预热到 Redis
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.SECKILL_WARMUP_TOPIC,
        consumerGroup = "seckill-warmup-consumer-group",
        selectorExpression = RocketMQConstants.SECKILL_WARMUP_TAG_STOCK
)
@RequiredArgsConstructor
public class SeckillWarmUpConsumer implements RocketMQListener<SeckillWarmUpEvent> {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void onMessage(SeckillWarmUpEvent event) {
        Long activityId = event.getActivityId();
        Long productId = event.getProductId();
        Integer stock = event.getStock();

        log.info("收到秒杀预热消息, activityId: {}, productId: {}, stock: {}",
                activityId, productId, stock);

        try {
            // 预热库存到 Redis
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
            redisTemplate.opsForValue().set(stockKey, stock.toString());

            // 初始化购买用户集合（空集合）
            String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
            redisTemplate.delete(boughtKey);

            // 设置过期时间（活动结束后 2 小时过期）
            long ttlSeconds = 7200;
            redisTemplate.expire(stockKey, ttlSeconds, TimeUnit.SECONDS);

            log.info("秒杀预热完成, activityId: {}, stock: {}", activityId, stock);
        } catch (Exception e) {
            log.error("秒杀预热失败, activityId: {}", activityId, e);
            throw e;
        }
    }
}