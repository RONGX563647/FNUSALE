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
 * 通知服务消费者
 * 负责发送各类通知消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConfig.PRODUCT_EVENT_TOPIC,
        consumerGroup = RocketMQConfig.NOTIFY_CONSUMER_GROUP,
        selectorExpression = "*"
)
public class NotifyConsumer implements RocketMQListener<ProductEventMessage> {

    private final StringRedisTemplate redisTemplate;

    /**
     * 消费商品事件消息 - 通知
     */
    @Override
    public void onMessage(ProductEventMessage message) {
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

        } catch (Exception e) {
            log.error("通知处理失败: messageId={}", message.getMessageId(), e);
            throw new RuntimeException("通知处理失败", e);
        }
    }

    /**
     * 通知关注者有新商品发布
     */
    private void notifyFollowersNewProduct(ProductEventMessage message) {
        // TODO: 实现通知关注者逻辑
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
        log.info("通知商品下架: productId={}", message.getProductId());
    }

    /**
     * 通知商品已售出
     */
    private void notifyProductSoldOut(ProductEventMessage message) {
        // TODO: 实现售出通知逻辑
        log.info("通知商品已售出: productId={}", message.getProductId());
    }
}