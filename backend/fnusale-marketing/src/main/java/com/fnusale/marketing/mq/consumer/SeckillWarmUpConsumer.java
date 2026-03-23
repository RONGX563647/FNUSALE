package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.SeckillWarmUpEvent;
import com.fnusale.marketing.config.IdempotentKeyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageModel;
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
        selectorExpression = RocketMQConstants.SECKILL_WARMUP_TAG_STOCK,
        maxReconsumeTimes = 3,
        consumeThreadMin = 5,
        consumeThreadMax = 10,
        consumeTimeout = 5,
        messageModel = MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class SeckillWarmUpConsumer implements RocketMQListener<SeckillWarmUpEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String SECKILL_WARMUP_KEY_PREFIX = "seckill:warmup:";

    @Override
    public void onMessage(SeckillWarmUpEvent event) {
        Long activityId = event.getActivityId();
        String eventId = event.getEventId();

        log.info("收到秒杀预热消息, activityId: {}, eventId: {}", activityId, eventId);

        String idempotentKey = SECKILL_WARMUP_KEY_PREFIX + eventId;
        long expireSeconds = IdempotentKeyConfig.calculateExpireSeconds(5);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", expireSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.info("秒杀预热已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
            redisTemplate.opsForValue().set(stockKey, event.getStock().toString());

            String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
            redisTemplate.delete(boughtKey);

            long ttlSeconds = 7200;
            redisTemplate.expire(stockKey, ttlSeconds, TimeUnit.SECONDS);

            log.info("秒杀预热完成, activityId: {}, stock: {}", activityId, event.getStock());
        } catch (Exception e) {
            log.error("秒杀预热失败, activityId: {}", activityId, e);
            throw e;
        }
    }
}