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
 * 通知服务消费者
 * 负责发送各类通知消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    private final StringRedisTemplate redisTemplate;

    /**
     * 消费商品事件消息 - 通知
     */
    @RabbitListener(queues = RabbitMQConfig.PRODUCT_NOTIFY_QUEUE)
    public void handleProductEvent(ProductEventMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("通知消费者收到消息: messageId={}, productId={}, eventType={}",
                message.getMessageId(), message.getProductId(), message.getEventType());

        try {
            switch (message.getEventType()) {
                case ProductEventMessage.EVENT_PUBLISH:
                    notifyFollowersNewProduct(message);
                    break;
                case ProductEventMessage.EVENT_ON_SHELF:
                    notifyInterestedUsers(message);
                    break;
                case ProductEventMessage.EVENT_OFF_SHELF:
                    notifyProductOffShelf(message);
                    break;
                case ProductEventMessage.EVENT_SOLD_OUT:
                    notifyProductSoldOut(message);
                    break;
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("通知处理失败: messageId={}", message.getMessageId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 消费用户行为消息 - 点赞通知
     */
    @RabbitListener(queues = RabbitMQConfig.BEHAVIOR_RECOMMEND_QUEUE)
    public void handleUserBehavior(UserBehaviorMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.debug("推荐消费者收到消息: messageId={}, behaviorType={}",
                message.getMessageId(), message.getBehaviorType());

        try {
            // 将用户行为同步到推荐系统
            syncToRecommendSystem(message);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("推荐数据处理失败: messageId={}", message.getMessageId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 通知关注者有新商品发布
     */
    private void notifyFollowersNewProduct(ProductEventMessage message) {
        // TODO: 实现通知关注者逻辑
        // 1. 查询关注该用户的其他用户
        // 2. 查询关注该品类的用户
        // 3. 发送IM消息或推送通知
        log.info("通知关注者新商品发布: userId={}, productId={}",
                message.getUserId(), message.getProductId());
    }

    /**
     * 通知感兴趣的用户商品上架
     */
    private void notifyInterestedUsers(ProductEventMessage message) {
        // TODO: 实现通知感兴趣用户逻辑
        log.info("通知感兴趣用户商品上架: productId={}", message.getProductId());
    }

    /**
     * 通知商品下架
     */
    private void notifyProductOffShelf(ProductEventMessage message) {
        // TODO: 实现下架通知逻辑
        // 通知正在咨询该商品的买家
        log.info("通知商品下架: productId={}", message.getProductId());
    }

    /**
     * 通知商品已售出
     */
    private void notifyProductSoldOut(ProductEventMessage message) {
        // TODO: 实现售出通知逻辑
        log.info("通知商品已售出: productId={}", message.getProductId());
    }

    /**
     * 同步用户行为到推荐系统
     */
    private void syncToRecommendSystem(UserBehaviorMessage message) {
        // TODO: 实现推荐系统同步逻辑
        // 可以写入专门的推荐数据表或发送到推荐服务
        String key = "recommend:user:" + message.getUserId() + ":behaviors";
        redisTemplate.opsForList().rightPush(key, message.getProductId() + ":" + message.getBehaviorType());
        // 只保留最近1000条行为记录
        redisTemplate.opsForList().trim(key, -1000, -1);

        log.debug("同步行为到推荐系统: userId={}, productId={}, behaviorType={}",
                message.getUserId(), message.getProductId(), message.getBehaviorType());
    }
}