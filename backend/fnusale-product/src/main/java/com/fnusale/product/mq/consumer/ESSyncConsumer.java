package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RocketMQConfig;
import com.fnusale.product.mq.message.ProductEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * ES同步消费者
 * 负责将商品数据同步到Elasticsearch
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConfig.PRODUCT_EVENT_TOPIC,
        consumerGroup = RocketMQConfig.ES_SYNC_CONSUMER_GROUP,
        selectorExpression = "*"
)
public class ESSyncConsumer implements RocketMQListener<ProductEventMessage> {

    private final StringRedisTemplate redisTemplate;

    private static final String PROCESSED_MSG_KEY = "mq:processed:product:event:";

    /**
     * 消费商品事件消息 - ES同步
     */
    @Override
    public void onMessage(ProductEventMessage message) {
        log.info("ES同步消费者收到消息: messageId={}, productId={}, eventType={}",
                message.getMessageId(), message.getProductId(), message.getEventType());

        try {
            // 幂等性检查
            if (isProcessed(message.getMessageId())) {
                log.info("消息已处理，跳过: messageId={}", message.getMessageId());
                return;
            }

            // 根据事件类型处理
            switch (message.getEventType()) {
                case ProductEventMessage.EVENT_PUBLISH:
                case ProductEventMessage.EVENT_UPDATE:
                case ProductEventMessage.EVENT_ON_SHELF:
                    syncToES(message);
                    break;
                case ProductEventMessage.EVENT_DELETE:
                case ProductEventMessage.EVENT_OFF_SHELF:
                    removeFromES(message);
                    break;
                default:
                    log.warn("未知事件类型: {}", message.getEventType());
            }

            // 标记消息已处理
            markProcessed(message.getMessageId());

            log.info("ES同步处理完成: productId={}", message.getProductId());

        } catch (Exception e) {
            log.error("ES同步处理失败: messageId={}, productId={}",
                    message.getMessageId(), message.getProductId(), e);
            throw new RuntimeException("ES同步处理失败", e);
        }
    }

    /**
     * 同步商品到ES
     */
    private void syncToES(ProductEventMessage message) {
        // TODO: 实现ES同步逻辑
        // 1. 根据productId查询完整商品信息
        // 2. 构建ES文档
        // 3. 调用ES API进行索引

        log.info("同步商品到ES: productId={}, productName={}",
                message.getProductId(), message.getProductName());

        // 示例：记录待同步状态
        redisTemplate.opsForValue().set("es:sync:pending:" + message.getProductId(), "1");
    }

    /**
     * 从ES移除商品
     */
    private void removeFromES(ProductEventMessage message) {
        // TODO: 实现ES删除逻辑
        log.info("从ES移除商品: productId={}", message.getProductId());

        // 清除同步状态
        redisTemplate.delete("es:sync:pending:" + message.getProductId());
    }

    /**
     * 检查消息是否已处理
     */
    private boolean isProcessed(String messageId) {
        if (messageId == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(PROCESSED_MSG_KEY + messageId));
    }

    /**
     * 标记消息已处理
     */
    private void markProcessed(String messageId) {
        if (messageId == null) return;
        redisTemplate.opsForValue().set(PROCESSED_MSG_KEY + messageId, "1");
    }
}