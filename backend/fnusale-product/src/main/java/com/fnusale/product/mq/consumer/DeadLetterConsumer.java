package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RabbitMQConfig;
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
 * 死信队列消费者
 * 处理失败的消息，进行重试或告警
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final StringRedisTemplate redisTemplate;

    private static final String DEAD_MSG_KEY = "mq:dead:product:";
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 消费死信消息
     */
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(org.springframework.amqp.core.Message message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.warn("收到死信消息: messageId={}, routingKey={}", messageId, routingKey);

        try {
            // 记录死信消息
            recordDeadMessage(messageId, routingKey, message.getBody());

            // 检查重试次数
            int retryCount = getRetryCount(messageId);
            if (retryCount < MAX_RETRY_COUNT) {
                // 增加重试计数
                incrementRetryCount(messageId);
                log.warn("死信消息待人工处理: messageId={}, retryCount={}", messageId, retryCount + 1);
            } else {
                // 超过最大重试次数，发送告警
                sendAlert(messageId, routingKey);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("死信消息处理失败: messageId={}", messageId, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 记录死信消息
     */
    private void recordDeadMessage(String messageId, String routingKey, byte[] body) {
        if (messageId == null) {
            messageId = java.util.UUID.randomUUID().toString();
        }
        String key = DEAD_MSG_KEY + messageId;
        String value = String.format("{\"routingKey\":\"%s\",\"body\":\"%s\",\"timestamp\":%d}",
                routingKey, new String(body), System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取重试次数
     */
    private int getRetryCount(String messageId) {
        if (messageId == null) return MAX_RETRY_COUNT;
        String count = redisTemplate.opsForValue().get(DEAD_MSG_KEY + messageId + ":retry");
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 增加重试计数
     */
    private void incrementRetryCount(String messageId) {
        if (messageId == null) return;
        redisTemplate.opsForValue().increment(DEAD_MSG_KEY + messageId + ":retry");
    }

    /**
     * 发送告警
     */
    private void sendAlert(String messageId, String routingKey) {
        // TODO: 对接告警系统（邮件/短信/Webhook）
        log.error("消息处理失败告警: messageId={}, routingKey={}, 已超过最大重试次数", messageId, routingKey);
    }
}