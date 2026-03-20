package com.fnusale.product.mq.producer;

import com.fnusale.product.mq.config.RocketMQConfig;
import com.fnusale.product.mq.message.AITaskMessage;
import com.fnusale.product.mq.message.ProductEventMessage;
import com.fnusale.product.mq.message.UserBehaviorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 消息发送工具类 - RocketMQ 实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送商品事件消息
     */
    public void sendProductEvent(ProductEventMessage message) {
        try {
            // 设置消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            if (message.getTimestamp() == null) {
                message.setTimestamp(System.currentTimeMillis());
            }

            String destination = RocketMQConfig.PRODUCT_EVENT_TOPIC + ":" + message.getEventType();

            rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(message).build()
            );

            log.info("发送商品事件消息成功: messageId={}, productId={}, eventType={}",
                    message.getMessageId(), message.getProductId(), message.getEventType());
        } catch (Exception e) {
            log.error("发送商品事件消息失败: productId={}, eventType={}",
                    message.getProductId(), message.getEventType(), e);
        }
    }

    /**
     * 发送用户行为消息
     */
    public void sendUserBehavior(UserBehaviorMessage message) {
        try {
            // 设置消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            if (message.getTimestamp() == null) {
                message.setTimestamp(System.currentTimeMillis());
            }

            String destination = RocketMQConfig.USER_BEHAVIOR_TOPIC + ":" + message.getBehaviorType();

            rocketMQTemplate.asyncSend(
                    destination,
                    MessageBuilder.withPayload(message).build(),
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.debug("异步发送用户行为消息成功: messageId={}, msgId={}",
                                    message.getMessageId(), sendResult.getMsgId());
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("异步发送用户行为消息失败: messageId={}",
                                    message.getMessageId(), e);
                        }
                    }
            );

            log.debug("发送用户行为消息成功: messageId={}, userId={}, productId={}, behaviorType={}",
                    message.getMessageId(), message.getUserId(), message.getProductId(), message.getBehaviorType());
        } catch (Exception e) {
            log.error("发送用户行为消息失败: userId={}, productId={}, behaviorType={}",
                    message.getUserId(), message.getProductId(), message.getBehaviorType(), e);
        }
    }

    /**
     * 发送AI任务消息
     */
    public void sendAITask(AITaskMessage message) {
        try {
            // 设置消息ID
            if (message.getTaskId() == null) {
                message.setTaskId(UUID.randomUUID().toString());
            }
            if (message.getTimestamp() == null) {
                message.setTimestamp(System.currentTimeMillis());
            }

            String destination = RocketMQConfig.AI_TASK_TOPIC + ":" + message.getTaskType();

            rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(message).build()
            );

            log.info("发送AI任务消息成功: taskId={}, taskType={}",
                    message.getTaskId(), message.getTaskType());
        } catch (Exception e) {
            log.error("发送AI任务消息失败: taskId={}, taskType={}",
                    message.getTaskId(), message.getTaskType(), e);
        }
    }

    /**
     * 发送商品发布事件
     */
    public void sendProductPublishEvent(Long productId, Long userId, String productName,
                                         Long categoryId, String categoryName, java.math.BigDecimal price) {
        ProductEventMessage message = ProductEventMessage.builder()
                .productId(productId)
                .userId(userId)
                .eventType(ProductEventMessage.EVENT_PUBLISH)
                .productName(productName)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .price(price)
                .productStatus("ON_SHELF")
                .build();
        sendProductEvent(message);
    }

    /**
     * 发送商品更新事件
     */
    public void sendProductUpdateEvent(Long productId, Long userId, String productName) {
        ProductEventMessage message = ProductEventMessage.builder()
                .productId(productId)
                .userId(userId)
                .eventType(ProductEventMessage.EVENT_UPDATE)
                .productName(productName)
                .build();
        sendProductEvent(message);
    }

    /**
     * 发送商品删除事件
     */
    public void sendProductDeleteEvent(Long productId, Long userId) {
        ProductEventMessage message = ProductEventMessage.builder()
                .productId(productId)
                .userId(userId)
                .eventType(ProductEventMessage.EVENT_DELETE)
                .build();
        sendProductEvent(message);
    }

    /**
     * 发送商品上架事件
     */
    public void sendProductOnShelfEvent(Long productId, Long userId) {
        ProductEventMessage message = ProductEventMessage.builder()
                .productId(productId)
                .userId(userId)
                .eventType(ProductEventMessage.EVENT_ON_SHELF)
                .oldStatus("OFF_SHELF")
                .productStatus("ON_SHELF")
                .build();
        sendProductEvent(message);
    }

    /**
     * 发送商品下架事件
     */
    public void sendProductOffShelfEvent(Long productId, Long userId) {
        ProductEventMessage message = ProductEventMessage.builder()
                .productId(productId)
                .userId(userId)
                .eventType(ProductEventMessage.EVENT_OFF_SHELF)
                .oldStatus("ON_SHELF")
                .productStatus("OFF_SHELF")
                .build();
        sendProductEvent(message);
    }

    /**
     * 发送收藏行为
     */
    public void sendCollectBehavior(Long userId, Long productId) {
        UserBehaviorMessage message = UserBehaviorMessage.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorMessage.BEHAVIOR_COLLECT)
                .behaviorTime(System.currentTimeMillis())
                .build();
        sendUserBehavior(message);
    }

    /**
     * 发送取消收藏行为
     */
    public void sendUncollectBehavior(Long userId, Long productId) {
        UserBehaviorMessage message = UserBehaviorMessage.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorMessage.BEHAVIOR_UNCOLLECT)
                .behaviorTime(System.currentTimeMillis())
                .build();
        sendUserBehavior(message);
    }

    /**
     * 发送点赞行为
     */
    public void sendLikeBehavior(Long userId, Long productId) {
        UserBehaviorMessage message = UserBehaviorMessage.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorMessage.BEHAVIOR_LIKE)
                .behaviorTime(System.currentTimeMillis())
                .build();
        sendUserBehavior(message);
    }

    /**
     * 发送取消点赞行为
     */
    public void sendUnlikeBehavior(Long userId, Long productId) {
        UserBehaviorMessage message = UserBehaviorMessage.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorMessage.BEHAVIOR_UNLIKE)
                .behaviorTime(System.currentTimeMillis())
                .build();
        sendUserBehavior(message);
    }

    /**
     * 发送浏览行为
     */
    public void sendBrowseBehavior(Long userId, Long productId) {
        UserBehaviorMessage message = UserBehaviorMessage.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorMessage.BEHAVIOR_BROWSE)
                .behaviorTime(System.currentTimeMillis())
                .build();
        sendUserBehavior(message);
    }

    /**
     * 发送AI品类识别任务
     */
    public void sendCategoryRecognizeTask(String taskId, Long userId, String imageUrl) {
        AITaskMessage message = AITaskMessage.builder()
                .taskId(taskId)
                .userId(userId)
                .taskType(AITaskMessage.TASK_CATEGORY_RECOGNIZE)
                .imageUrl(imageUrl)
                .build();
        sendAITask(message);
    }
}