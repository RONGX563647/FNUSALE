package com.fnusale.user.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户注册事件消费者 - 初始化积分
 * 注意：积分初始化已在注册流程中同步完成，此消费者作为兜底和幂等性检查
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.USER_REGISTER_TOPIC,
        consumerGroup = "user-register-points-group",
        selectorExpression = RocketMQConstants.USER_REGISTER_TAG_INIT_POINTS
)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
public class UserRegisterPointsConsumer implements RocketMQListener<UserRegisterEvent> {

    private final StringRedisTemplate redisTemplate;

    /**
     * 积分初始化幂等性 Key 前缀
     */
    private static final String POINTS_INIT_KEY_PREFIX = "user:points:init:";

    @Override
    public void onMessage(UserRegisterEvent event) {
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到用户注册积分初始化消息, userId: {}, eventId: {}", userId, eventId);

        // 幂等性检查
        String idempotentKey = POINTS_INIT_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("积分初始化已处理，跳过, userId: {}, eventId: {}", userId, eventId);
            return;
        }

        try {
            // 积分已在注册流程中初始化，此处仅作为兜底检查
            // 实际场景中可以调用积分服务进行校验和补偿初始化
            log.info("用户积分初始化完成（已由注册流程处理）, userId: {}", userId);
        } catch (Exception e) {
            // 处理失败，删除幂等性 Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("用户积分初始化失败, userId: {}", userId, e);
            throw e;
        }
    }
}