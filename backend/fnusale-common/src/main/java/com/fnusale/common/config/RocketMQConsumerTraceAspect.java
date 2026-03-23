package com.fnusale.common.config;

import com.fnusale.common.log.LogConstants;
import com.fnusale.common.log.TraceContext;
import com.fnusale.common.util.UserContext;
import org.apache.rocketmq.common.message.MessageExt;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RocketMQ消费者TraceId切面
 * 在消息消费时从消息头获取TraceId并设置到当前线程上下文
 */
@Aspect
@Component
public class RocketMQConsumerTraceAspect {

    @Pointcut("execution(* org.apache.rocketmq.spring.core.RocketMQListener.onMessage(..))")
    public void rocketMQListenerPointcut() {
    }

    @Around("rocketMQListenerPointcut() && args(message)")
    public Object around(ProceedingJoinPoint joinPoint, Object message) throws Throwable {
        String traceId = null;
        String spanId = null;
        Long userId = null;
        String userRole = null;

        if (message instanceof MessageExt) {
            MessageExt messageExt = (MessageExt) message;
            traceId = messageExt.getUserProperty(LogConstants.TRACE_ID_HEADER);
            spanId = messageExt.getUserProperty(LogConstants.SPAN_ID_HEADER);
            String userIdStr = messageExt.getUserProperty(LogConstants.USER_ID_HEADER);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdStr);
                } catch (NumberFormatException ignored) {
                }
            }
            userRole = messageExt.getUserProperty(LogConstants.USER_ROLE_HEADER);
        }

        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceContext.generateTraceId();
        }
        if (spanId == null || spanId.isEmpty()) {
            spanId = TraceContext.generateSpanId();
        }

        TraceContext.init(traceId);
        TraceContext.setSpanId(spanId);

        if (userId != null) {
            TraceContext.setUserId(userId);
            UserContext.setCurrentUserId(userId);
        }
        if (userRole != null) {
            TraceContext.setUserRole(userRole);
            UserContext.setCurrentUserRole(userRole);
        }

        try {
            return joinPoint.proceed();
        } finally {
            TraceContext.clear();
            UserContext.clear();
        }
    }
}
