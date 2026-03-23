package com.fnusale.marketing.event;

import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.log.TraceContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 秒杀成功应用事件
 * 用于在事务提交后发送MQ消息，确保数据一致性
 * 企业级优化：携带TraceContext快照，支持异步事件链路追踪
 */
@Getter
public class SeckillSuccessAppEvent extends ApplicationEvent {

    private final SeckillOrderEvent orderEvent;
    private final Long localMessageId;
    private final TraceContext.TraceContextSnapshot traceContextSnapshot;

    public SeckillSuccessAppEvent(Object source, SeckillOrderEvent orderEvent, Long localMessageId) {
        super(source);
        this.orderEvent = orderEvent;
        this.localMessageId = localMessageId;
        this.traceContextSnapshot = TraceContext.snapshot();
    }
}
