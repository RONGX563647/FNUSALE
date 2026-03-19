package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RabbitMQConfig;
import com.fnusale.product.mq.message.ProductEventMessage;
import com.fnusale.product.mq.message.UserBehaviorMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 统计服务消费者
 * 负责更新商品统计数据（收藏数、点赞数、浏览量等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsConsumer {

    private final StringRedisTemplate redisTemplate;

    private static final String PROCESSED_MSG_KEY = "mq:processed:stats:";
    private static final String PRODUCT_STATS_KEY = "product:stats:";
    private static final String USER_STATS_KEY = "user:stats:";

    /**
     * 消费商品事件消息 - 统计更新
     */
    @RabbitListener(queues = RabbitMQConfig.PRODUCT_STATS_QUEUE)
    public void handleProductEvent(ProductEventMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.debug("统计消费者收到商品事件: messageId={}, productId={}, eventType={}",
                message.getMessageId(), message.getProductId(), message.getEventType());

        try {
            // 更新用户发布商品数统计
            updateUserProductCount(message.getUserId(), message.getEventType());

            // 更新品类商品数统计
            if (message.getCategoryId() != null) {
                updateCategoryProductCount(message.getCategoryId(), message.getEventType());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("商品统计处理失败: messageId={}", message.getMessageId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 消费用户行为消息 - 统计更新
     */
    @RabbitListener(queues = RabbitMQConfig.BEHAVIOR_STATS_QUEUE)
    public void handleUserBehavior(UserBehaviorMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.debug("统计消费者收到用户行为: messageId={}, productId={}, behaviorType={}",
                message.getMessageId(), message.getProductId(), message.getBehaviorType());

        try {
            // 更新商品统计
            updateProductBehaviorStats(message.getProductId(), message.getBehaviorType());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("行为统计处理失败: messageId={}", message.getMessageId(), e);
            channel.basicNack(deliveryTag, false, true);
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

    /**
     * 更新商品行为统计
     */
    private void updateProductBehaviorStats(Long productId, String behaviorType) {
        if (productId == null) return;

        String key = PRODUCT_STATS_KEY + productId;
        switch (behaviorType) {
            case UserBehaviorMessage.BEHAVIOR_COLLECT:
                redisTemplate.opsForHash().increment(key, "collectCount", 1);
                break;
            case UserBehaviorMessage.BEHAVIOR_UNCOLLECT:
                redisTemplate.opsForHash().increment(key, "collectCount", -1);
                break;
            case UserBehaviorMessage.BEHAVIOR_LIKE:
                redisTemplate.opsForHash().increment(key, "likeCount", 1);
                break;
            case UserBehaviorMessage.BEHAVIOR_UNLIKE:
                redisTemplate.opsForHash().increment(key, "likeCount", -1);
                break;
            case UserBehaviorMessage.BEHAVIOR_BROWSE:
                redisTemplate.opsForHash().increment(key, "browseCount", 1);
                break;
        }
        log.debug("更新商品行为统计: productId={}, behaviorType={}", productId, behaviorType);
    }
}