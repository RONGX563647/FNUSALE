package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RabbitMQConfig;
import com.fnusale.product.mq.message.AITaskMessage;
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
 * AI任务消费者
 * 处理异步AI识别任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AITaskConsumer {

    private final StringRedisTemplate redisTemplate;

    private static final String AI_RESULT_KEY = "ai:task:result:";

    /**
     * 消费AI任务消息
     */
    @RabbitListener(queues = RabbitMQConfig.AI_TASK_QUEUE)
    public void handleAITask(AITaskMessage message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("AI任务消费者收到消息: taskId={}, taskType={}",
                message.getTaskId(), message.getTaskType());

        try {
            switch (message.getTaskType()) {
                case AITaskMessage.TASK_CATEGORY_RECOGNIZE:
                    processCategoryRecognition(message);
                    break;
                case AITaskMessage.TASK_PRICE_SUGGEST:
                    processPriceSuggestion(message);
                    break;
                default:
                    log.warn("未知AI任务类型: {}", message.getTaskType());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("AI任务处理失败: taskId={}", message.getTaskId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 处理品类识别任务
     */
    private void processCategoryRecognition(AITaskMessage message) {
        log.info("开始AI品类识别: taskId={}, imageUrl={}", message.getTaskId(), message.getImageUrl());

        try {
            // TODO: 对接阿里云视觉AI进行品类识别
            // 1. 调用阿里云API识别图片
            // 2. 解析返回结果，匹配品类
            // 3. 存储识别结果

            // 模拟识别结果
            String mockResult = "{\"categoryId\":1,\"categoryName\":\"数码产品\",\"confidence\":0.95}";
            saveTaskResult(message.getTaskId(), mockResult);

            log.info("AI品类识别完成: taskId={}", message.getTaskId());

        } catch (Exception e) {
            log.error("AI品类识别异常: taskId={}", message.getTaskId(), e);
            saveTaskResult(message.getTaskId(), "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 处理价格建议任务
     */
    private void processPriceSuggestion(AITaskMessage message) {
        log.info("开始AI价格建议: taskId={}, productId={}", message.getTaskId(), message.getProductId());

        try {
            // TODO: 基于历史数据实现价格建议
            // 1. 查询同类商品历史成交价
            // 2. 分析商品成色、品牌等因素
            // 3. 生成价格建议区间

            String mockResult = "{\"minPrice\":80.00,\"maxPrice\":120.00,\"suggestedPrice\":99.00}";
            saveTaskResult(message.getTaskId(), mockResult);

            log.info("AI价格建议完成: taskId={}", message.getTaskId());

        } catch (Exception e) {
            log.error("AI价格建议异常: taskId={}", message.getTaskId(), e);
            saveTaskResult(message.getTaskId(), "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 保存任务结果
     */
    private void saveTaskResult(String taskId, String result) {
        String key = AI_RESULT_KEY + taskId;
        redisTemplate.opsForValue().set(key, result);
        // 结果保存1小时
        redisTemplate.expire(key, 1, java.util.concurrent.TimeUnit.HOURS);
    }
}