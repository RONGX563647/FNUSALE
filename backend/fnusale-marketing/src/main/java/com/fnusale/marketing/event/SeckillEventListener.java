package com.fnusale.marketing.event;

import com.fnusale.common.constant.LocalMessageStatus;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.log.TraceContext;
import com.fnusale.marketing.mapper.LocalMessageMapper;
import com.fnusale.marketing.service.MarketingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 秒杀事件监听器
 * 使用 @TransactionalEventListener 确保在事务提交后发送MQ消息
 * 企业级优化：
 * - 恢复TraceContext，支持异步事件链路追踪
 * - 异常时更新本地消息状态，让定时任务可以重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillEventListener {

    private final MarketingEventPublisher eventPublisher;
    private final LocalMessageMapper localMessageMapper;

    /**
     * 监听秒杀成功事件，在事务提交后发送MQ消息
     * 确保Redis库存扣减和本地消息表写入都成功后才发送消息
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSeckillSuccess(SeckillSuccessAppEvent event) {
        TraceContext.restore(event.getTraceContextSnapshot());

        try {
            boolean sent = eventPublisher.sendSync(
                    RocketMQConstants.SECKILL_ORDER_TOPIC,
                    RocketMQConstants.SECKILL_ORDER_TAG_CREATE,
                    event.getOrderEvent()
            );

            if (sent) {
                localMessageMapper.updateToSent(event.getLocalMessageId());
                log.info("[{}] 秒杀订单消息发送成功: eventId={}",
                        TraceContext.getTraceId(), event.getOrderEvent().getEventId());
            } else {
                handleSendFailure(event);
            }
        } catch (Exception e) {
            log.error("[{}] 秒杀订单消息发送异常: eventId={}",
                    TraceContext.getTraceId(), event.getOrderEvent().getEventId(), e);
            handleSendFailure(event);
        } finally {
            TraceContext.clear();
        }
    }

    /**
     * 处理消息发送失败
     * 更新本地消息状态为失败，让定时任务重试
     */
    private void handleSendFailure(SeckillSuccessAppEvent event) {
        try {
            LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(1);
            localMessageMapper.updateToFailed(event.getLocalMessageId(), nextRetryTime);
            log.warn("[{}] 秒杀订单消息发送失败，已更新状态待重试: eventId={}, localMessageId={}",
                    TraceContext.getTraceId(),
                    event.getOrderEvent().getEventId(), event.getLocalMessageId());
        } catch (Exception e) {
            log.error("[{}] 更新本地消息状态失败: localMessageId={}",
                    TraceContext.getTraceId(), event.getLocalMessageId(), e);
        }
    }
}
