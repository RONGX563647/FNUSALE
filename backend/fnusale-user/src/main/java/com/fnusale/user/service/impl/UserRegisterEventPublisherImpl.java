package com.fnusale.user.service.impl;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.UserRegisterEvent;
import com.fnusale.user.service.UserRegisterEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

/**
 * 用户注册事件发布服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserRegisterEventPublisherImpl implements UserRegisterEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publishRegisterEvent(UserRegisterEvent event) {
        // 发送初始化积分消息
        sendAsyncMessage(
                RocketMQConstants.USER_REGISTER_TOPIC + ":" + RocketMQConstants.USER_REGISTER_TAG_INIT_POINTS,
                event
        );

        // 发送欢迎通知消息
        sendAsyncMessage(
                RocketMQConstants.USER_REGISTER_TOPIC + ":" + RocketMQConstants.USER_REGISTER_TAG_WELCOME,
                event
        );

        // 发送新人优惠券消息
        sendAsyncMessage(
                RocketMQConstants.USER_REGISTER_TOPIC + ":" + RocketMQConstants.USER_REGISTER_TAG_NEW_USER_COUPON,
                event
        );

        log.info("用户注册事件已发布, userId: {}, eventId: {}", event.getUserId(), event.getEventId());
    }

    /**
     * 异步发送消息
     */
    private void sendAsyncMessage(String destination, UserRegisterEvent event) {
        rocketMQTemplate.asyncSend(destination, event, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.debug("消息发送成功, destination: {}, msgId: {}", destination, sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("消息发送失败, destination: {}, userId: {}", destination, event.getUserId(), e);
            }
        });
    }
}