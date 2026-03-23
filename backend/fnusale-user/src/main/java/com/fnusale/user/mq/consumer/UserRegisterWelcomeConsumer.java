package com.fnusale.user.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.UserRegisterEvent;
import com.fnusale.common.util.DesensitizeUtil;
import com.fnusale.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户注册事件消费者 - 发送欢迎通知
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.USER_REGISTER_TOPIC,
        consumerGroup = "user-register-welcome-group",
        selectorExpression = RocketMQConstants.USER_REGISTER_TAG_WELCOME
)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
public class UserRegisterWelcomeConsumer implements RocketMQListener<UserRegisterEvent> {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    /**
     * 欢迎消息幂等性 Key 前缀
     */
    private static final String WELCOME_KEY_PREFIX = "user:welcome:";

    @Override
    public void onMessage(UserRegisterEvent event) {
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到用户注册欢迎通知消息, userId: {}, eventId: {}", userId, eventId);

        // 幂等性检查
        String idempotentKey = WELCOME_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("欢迎通知已发送，跳过, userId: {}, eventId: {}", userId, eventId);
            return;
        }

        try {
            // TODO: 调用 IM 服务发送欢迎消息
            // 目前先记录日志，后续可以集成 IM 服务或邮件/短信服务
            sendWelcomeNotification(event);

            log.info("欢迎通知发送成功, userId: {}", userId);
        } catch (Exception e) {
            // 处理失败，删除幂等性 Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("欢迎通知发送失败, userId: {}", userId, e);
            throw e;
        }
    }

    /**
     * 发送欢迎通知
     */
    private void sendWelcomeNotification(UserRegisterEvent event) {
        // 发送欢迎邮件
        if (event.getEmail() != null && !event.getEmail().isEmpty()) {
            try {
                emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
                log.info("欢迎邮件发送成功 - email: {}", DesensitizeUtil.email(event.getEmail()));
            } catch (Exception e) {
                log.error("欢迎邮件发送失败 - email: {}, 错误: {}", 
                    DesensitizeUtil.email(event.getEmail()), e.getMessage(), e);
            }
        }

        // TODO: 后续集成短信服务
        if (event.getPhone() != null && !event.getPhone().isEmpty()) {
            log.info("欢迎短信发送（待集成） - phone: {}", DesensitizeUtil.phone(event.getPhone()));
        }

        // TODO: 后续集成 IM 服务
        log.info("欢迎站内消息发送（待集成） - userId: {}", event.getUserId());
    }

    /**
     * 手机号脱敏（使用 DesensitizeUtil）
     */
    private String maskPhone(String phone) {
        return DesensitizeUtil.phone(phone);
    }

    /**
     * 邮箱脱敏（使用 DesensitizeUtil）
     */
    private String maskEmail(String email) {
        return DesensitizeUtil.email(email);
    }
}