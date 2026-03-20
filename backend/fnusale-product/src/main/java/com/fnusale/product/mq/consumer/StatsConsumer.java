package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RocketMQConfig;
import com.fnusale.product.mq.message.ProductEventMessage;
import com.fnusale.product.mq.message.UserBehaviorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 统计服务消费者
 * 负责更新商品统计数据（收藏数、点赞数、浏览量等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConfig.PRODUCT_EVENT_TOPIC,
        consumerGroup = RocketMQConfig.STATS_CONSUMER_GROUP,
        selectorExpression = "*"
)
public class StatsConsumer implements RocketMQListener<ProductEventMessage> {

    private final StringRedisTemplate redisTemplate;

    private static final String PRODUCT_STATS_KEY = "product:stats:";
    private static final String USER_STATS_KEY = "user:stats:";

    /**
     * 消费商品事件消息 - 统计更新
     */
    @Override
    public void onMessage(ProductEventMessage message) {
        log.debug("统计消费者收到商品事件: messageId={}, productId={}, eventType={}",
                message.getMessageId(), message.getProductId(), message.getEventType());

        try {
            // 更新用户发布商品数统计
            updateUserProductCount(message.getUserId(), message.getEventType());

            // 更新品类商品数统计
            if (message.getCategoryId() != null) {
                updateCategoryProductCount(message.getCategoryId(), message.getEventType());
            }

        } catch (Exception e) {
            log.error("商品统计处理失败: messageId={}", message.getMessageId(), e);
            throw new RuntimeException("商品统计处理失败", e);
        }
    }

    /**
     * 更新用户发布商品数
     */
    private void updateUserProductCount(Long userId, String eventType) {
        if (userId == null) return;

        String key = USER_STATS_KEY + userId + ":product_count";
        switch (eventType) {
            case ProductEventMessage.EVENT_PUBLISH:
                redisTemplate.opsForValue().increment(key);
                break;
            case ProductEventMessage.EVENT_DELETE:
                redisTemplate.opsForValue().decrement(key);
                break;
        }
        log.debug("更新用户商品数: userId={}, eventType={}", userId, eventType);
    }

    /**
     * 更新品类商品数
     */
    private void updateCategoryProductCount(Long categoryId, String eventType) {
        if (categoryId == null) return;

        String key = "category:stats:" + categoryId + ":product_count";
        switch (eventType) {
            case ProductEventMessage.EVENT_PUBLISH:
                redisTemplate.opsForValue().increment(key);
                break;
            case ProductEventMessage.EVENT_DELETE:
                redisTemplate.opsForValue().decrement(key);
                break;
        }
        log.debug("更新品类商品数: categoryId={}, eventType={}", categoryId, eventType);
    }
}