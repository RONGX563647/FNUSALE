package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.trade.event.DisputeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 纠纷消费者
 * 处理纠纷相关的异步操作：
 * 1. 通知双方当事人
 * 2. 通知管理员处理
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.TRADE_DISPUTE_TOPIC,
        consumerGroup = "trade-dispute-consumer-group",
        selectorExpression = RocketMQConstants.TRADE_DISPUTE_TAG_CREATE + " || " +
                RocketMQConstants.TRADE_DISPUTE_TAG_PROCESS + " || " +
                RocketMQConstants.TRADE_DISPUTE_TAG_RESOLVE
)
@RequiredArgsConstructor
public class TradeDisputeConsumer implements RocketMQListener<DisputeEvent> {

    private final StringRedisTemplate redisTemplate;

    private static final String DISPUTE_KEY_PREFIX = "dispute:process:";

    @Override
    public void onMessage(DisputeEvent event) {
        Long disputeId = event.getDisputeId();
        String eventId = event.getEventId();

        log.info("收到纠纷消息，disputeId: {}, orderId: {}, eventId: {}",
                disputeId, event.getOrderId(), eventId);

        // 幂等性检查
        String idempotentKey = DISPUTE_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("纠纷消息已处理，跳过，disputeId: {}", disputeId);
            return;
        }

        try {
            // 根据纠纷状态执行不同操作
            // 由于我们通过selectorExpression过滤，这里可以根据具体业务处理

            // 1. 通知发起者
            notifyInitiator(event);

            // 2. 通知被投诉者
            notifyAccused(event);

            // 3. 通知管理员（如果有处理结果）
            if (event.getResult() != null) {
                log.info("纠纷已处理，disputeId: {}, result: {}", disputeId, event.getResult());
            }

            log.info("纠纷消息处理完成，disputeId: {}", disputeId);

        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("纠纷消息处理失败，disputeId: {}", disputeId, e);
            throw e;
        }
    }

    /**
     * 通知发起者
     */
    private void notifyInitiator(DisputeEvent event) {
        // TODO: 集成IM服务发送站内信
        log.info("通知纠纷发起者，initiatorId: {}, disputeId: {}", event.getInitiatorId(), event.getDisputeId());
    }

    /**
     * 通知被投诉者
     */
    private void notifyAccused(DisputeEvent event) {
        // TODO: 集成IM服务发送站内信
        log.info("通知被投诉者，accusedId: {}, disputeId: {}", event.getAccusedId(), event.getDisputeId());
    }
}