package com.fnusale.admin.service.impl;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.ProductAuditEvent;
import com.fnusale.common.event.UserAuthAuditEvent;
import com.fnusale.common.event.UserBanEvent;
import com.fnusale.admin.service.AdminEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

/**
 * Admin 事件发布服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventPublisherImpl implements AdminEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publishProductAuditEvent(ProductAuditEvent event) {
        String destination = RocketMQConstants.PRODUCT_AUDIT_TOPIC + ":" + RocketMQConstants.PRODUCT_AUDIT_TAG_NOTIFY;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("商品审核消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("商品审核消息发送失败, eventId: {}, productId: {}", event.getEventId(), event.getProductId(), e);
            }
        });
    }

    @Override
    public void publishUserAuthAuditEvent(UserAuthAuditEvent event) {
        String destination = RocketMQConstants.USER_AUTH_TOPIC + ":" + RocketMQConstants.USER_AUTH_TAG_NOTIFY;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("用户认证审核消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("用户认证审核消息发送失败, eventId: {}, userId: {}", event.getEventId(), event.getUserId(), e);
            }
        });
    }

    @Override
    public void publishUserBanEvent(UserBanEvent event) {
        String destination = RocketMQConstants.USER_BAN_TOPIC + ":" + RocketMQConstants.USER_BAN_TAG_NOTIFY;
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("用户封禁消息发送成功, eventId: {}, msgId: {}", event.getEventId(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("用户封禁消息发送失败, eventId: {}, userId: {}", event.getEventId(), event.getUserId(), e);
            }
        });
    }
}