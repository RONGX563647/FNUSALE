package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.marketing.config.IdempotentKeyConfig;
import com.fnusale.marketing.mapper.CouponMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券发放消费者
 * 消费优惠券发放消息，异步发放优惠券
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.COUPON_GRANT_TOPIC,
        consumerGroup = "coupon-grant-consumer-group",
        selectorExpression = RocketMQConstants.COUPON_GRANT_TAG_BATCH,
        maxReconsumeTimes = 3,
        consumeThreadMin = 10,
        consumeThreadMax = 20,
        consumeTimeout = 15,
        messageModel = MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class CouponGrantConsumer implements RocketMQListener<CouponGrantEvent> {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_GRANT_KEY_PREFIX = "coupon:grant:";

    @Override
    public void onMessage(CouponGrantEvent event) {
        Long couponId = event.getCouponId();
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到优惠券发放消息, couponId: {}, userId: {}, eventId: {}", couponId, userId, eventId);

        String idempotentKey = COUPON_GRANT_KEY_PREFIX + eventId;
        long expireSeconds = IdempotentKeyConfig.calculateDefaultExpireSeconds();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", expireSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.info("优惠券已发放，跳过, eventId: {}", eventId);
            return;
        }

        try {
            if (couponMapper.selectById(couponId) == null) {
                log.warn("优惠券不存在, couponId: {}", couponId);
                return;
            }

            if (userCouponMapper.countByUserAndCoupon(userId, couponId) > 0) {
                log.info("用户已领取该优惠券, userId: {}, couponId: {}", userId, couponId);
                return;
            }

            int rows = couponMapper.incrementReceivedCount(couponId);
            if (rows == 0) {
                log.warn("优惠券库存不足, couponId: {}", couponId);
                return;
            }

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .receiveTime(LocalDateTime.now())
                    .expireTime(event.getExpireTime())
                    .couponStatus(MarketingConstants.COUPON_STATUS_UNUSED)
                    .createTime(LocalDateTime.now())
                    .build();

            userCouponMapper.insert(userCoupon);

            log.info("优惠券发放成功, couponId: {}, userId: {}", couponId, userId);
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("优惠券发放失败, couponId: {}, userId: {}", couponId, userId, e);
            throw e;
        }
    }
}