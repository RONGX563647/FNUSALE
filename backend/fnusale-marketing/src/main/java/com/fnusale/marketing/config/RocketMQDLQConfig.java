package com.fnusale.marketing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ 死信队列配置
 *
 * RocketMQ 默认死信队列规则:
 * - 消费重试超过最大次数（默认16次）后，消息进入死信队列
 * - 死信队列 Topic 格式: %DLQ% + ConsumerGroup
 *
 * 例如: consumerGroup = "coupon-grant-consumer-group"
 * 死信队列 Topic = "%DLQ%coupon-grant-consumer-group"
 */
@Slf4j
@Configuration
public class RocketMQDLQConfig {

    /**
     * 死信队列 Topic 前缀
     */
    public static final String DLQ_TOPIC_PREFIX = "%DLQ%";

    /**
     * 消费者组与死信队列映射
     */
    private static final Map<String, String> CONSUMER_DLQ_MAPPING = new HashMap<>();

    static {
        // 注册消费者组的死信队列
        CONSUMER_DLQ_MAPPING.put("coupon-grant-consumer-group", DLQ_TOPIC_PREFIX + "coupon-grant-consumer-group");
        CONSUMER_DLQ_MAPPING.put("coupon-receive-consumer-group", DLQ_TOPIC_PREFIX + "coupon-receive-consumer-group");
        CONSUMER_DLQ_MAPPING.put("user-register-coupon-group", DLQ_TOPIC_PREFIX + "user-register-coupon-group");
        CONSUMER_DLQ_MAPPING.put("seckill-warmup-consumer-group", DLQ_TOPIC_PREFIX + "seckill-warmup-consumer-group");
        CONSUMER_DLQ_MAPPING.put("coupon-expire-reminder-consumer-group", DLQ_TOPIC_PREFIX + "coupon-expire-reminder-consumer-group");
    }

    /**
     * 获取死信队列 Topic
     *
     * @param consumerGroup 消费者组
     * @return 死信队列 Topic
     */
    public static String getDLQTopic(String consumerGroup) {
        return DLQ_TOPIC_PREFIX + consumerGroup;
    }

    /**
     * 获取所有死信队列配置
     */
    public static Map<String, String> getConsumerDLQMapping() {
        return new HashMap<>(CONSUMER_DLQ_MAPPING);
    }

    /**
     * 记录死信消息处理日志
     */
    public static void logDLQMessage(String consumerGroup, String messageId, String reason) {
        String dlqTopic = getDLQTopic(consumerGroup);
        log.warn("消息进入死信队列: consumerGroup={}, dlqTopic={}, messageId={}, reason={}",
                consumerGroup, dlqTopic, messageId, reason);
    }
}