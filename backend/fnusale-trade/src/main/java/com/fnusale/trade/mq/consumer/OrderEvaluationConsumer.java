package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.trade.client.UserClient;
import com.fnusale.trade.event.OrderEvaluationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 评价消费者
 * 处理评价提交后的异步操作：
 * 1. 更新卖家评分统计
 * 2. 更新商品评价统计
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_EVALUATION_TOPIC,
        consumerGroup = "order-evaluation-consumer-group",
        selectorExpression = RocketMQConstants.ORDER_EVALUATION_TAG_UPDATE_RATING
)
@RequiredArgsConstructor
public class OrderEvaluationConsumer implements RocketMQListener<OrderEvaluationEvent> {

    private final UserClient userClient;
    private final StringRedisTemplate redisTemplate;

    private static final String EVALUATION_KEY_PREFIX = "evaluation:process:";

    @Override
    public void onMessage(OrderEvaluationEvent event) {
        Long evaluationId = event.getEvaluationId();
        Long sellerId = event.getSellerId();
        String eventId = event.getEventId();

        log.info("收到评价消息，evaluationId: {}, sellerId: {}, score: {}, eventId: {}",
                evaluationId, sellerId, event.getScore(), eventId);

        // 幂等性检查
        String idempotentKey = EVALUATION_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("评价消息已处理，跳过，evaluationId: {}", evaluationId);
            return;
        }

        try {
            // 1. 更新卖家评分统计
            try {
                userClient.updateRating(sellerId, event.getScore());
                log.info("卖家评分更新成功，sellerId: {}, score: {}", sellerId, event.getScore());
            } catch (Exception e) {
                log.error("更新卖家评分失败，sellerId: {}", sellerId, e);
                // 评分更新失败不影响主流程
            }

            // 2. 更新商品评价统计（可以发送到商品服务处理）
            updateProductEvaluationStats(event);

            log.info("评价后续处理完成，evaluationId: {}", evaluationId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("评价消息处理失败，evaluationId: {}", evaluationId, e);
            throw e;
        }
    }

    /**
     * 更新商品评价统计
     */
    private void updateProductEvaluationStats(OrderEvaluationEvent event) {
        // TODO: 发送消息到商品服务更新评价统计
        // 商品服务需要维护：评价总数、平均评分、好评率等
        log.info("更新商品评价统计，productId: {}, score: {}", event.getProductId(), event.getScore());
    }
}