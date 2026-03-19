package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.UserRegisterEvent;
import com.fnusale.marketing.mapper.CouponMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户注册事件消费者 - 发放新人优惠券
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.USER_REGISTER_TOPIC,
        consumerGroup = "user-register-coupon-group",
        selectorExpression = RocketMQConstants.USER_REGISTER_TAG_NEW_USER_COUPON
)
@RequiredArgsConstructor
public class UserRegisterCouponConsumer implements RocketMQListener<UserRegisterEvent> {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 新人优惠券发放幂等性 Key 前缀
     */
    private static final String NEW_USER_COUPON_KEY_PREFIX = "user:coupon:new:";

    /**
     * 新人优惠券 ID（实际项目中应该从配置或数据库读取）
     * TODO: 配置新人优惠券 ID
     */
    private static final Long NEW_USER_COUPON_ID = 1L;

    /**
     * 优惠券有效期（天）
     */
    private static final int COUPON_VALID_DAYS = 30;

    @Override
    public void onMessage(UserRegisterEvent event) {
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到用户注册新人优惠券消息, userId: {}, eventId: {}", userId, eventId);

        // 幂等性检查
        String idempotentKey = NEW_USER_COUPON_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("新人优惠券已发放，跳过, userId: {}, eventId: {}", userId, eventId);
            return;
        }

        try {
            // 检查优惠券是否存在
            if (couponMapper.selectById(NEW_USER_COUPON_ID) == null) {
                log.warn("新人优惠券不存在, couponId: {}", NEW_USER_COUPON_ID);
                return;
            }

            // 检查用户是否已领取
            if (userCouponMapper.countByUserAndCoupon(userId, NEW_USER_COUPON_ID) > 0) {
                log.info("用户已领取新人优惠券, userId: {}", userId);
                return;
            }

            // 发放新人优惠券
            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .couponId(NEW_USER_COUPON_ID)
                    .receiveTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plusDays(COUPON_VALID_DAYS))
                    .couponStatus("UNUSED")
                    .createTime(LocalDateTime.now())
                    .build();

            userCouponMapper.insert(userCoupon);

            log.info("新人优惠券发放成功, userId: {}, couponId: {}", userId, NEW_USER_COUPON_ID);
        } catch (Exception e) {
            // 处理失败，删除幂等性 Key，允许重试
            redisTemplate.delete(idempotentKey);
            log.error("新人优惠券发放失败, userId: {}", userId, e);
            throw e;
        }
    }
}