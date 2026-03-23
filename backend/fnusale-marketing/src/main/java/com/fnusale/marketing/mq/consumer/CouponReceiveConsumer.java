package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponReceiveEvent;
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
 * 优惠券领取消费者
 * 消费领券消息，异步写入数据库
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.COUPON_RECEIVE_TOPIC,
        consumerGroup = "coupon-receive-consumer-group",
        selectorExpression = RocketMQConstants.COUPON_RECEIVE_TAG_RECEIVE,
        maxReconsumeTimes = 3,
        consumeThreadMin = 10,
        consumeThreadMax = 20,
        consumeTimeout = 15,
        messageModel = MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class CouponReceiveConsumer implements RocketMQListener<CouponReceiveEvent> {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_RECEIVE_KEY_PREFIX = "coupon:receive:";

    @Override
    public void onMessage(CouponReceiveEvent event) {
        Long couponId = event.getCouponId();
        Long userId = event.getUserId();
        String eventId = event.getEventId();

        log.info("收到优惠券领取消息, couponId: {}, userId: {}, eventId: {}", couponId, userId, eventId);

        String idempotentKey = COUPON_RECEIVE_KEY_PREFIX + eventId;
        long expireSeconds = IdempotentKeyConfig.calculateDefaultExpireSeconds();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", expireSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.info("优惠券已领取，跳过, eventId: {}", eventId);
            return;
        }

        try {
            if (userCouponMapper.countByUserAndCoupon(userId, couponId) > 0) {
                log.info("用户已领取该优惠券, userId: {}, couponId: {}", userId, couponId);
                return;
            }

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .receiveTime(event.getReceiveTime())
                    .expireTime(event.getExpireTime())
                    .couponStatus(MarketingConstants.COUPON_STATUS_UNUSED)
                    .createTime(LocalDateTime.now())
                    .build();

            userCouponMapper.insert(userCoupon);

            log.info("优惠券领取入库成功, couponId: {}, userId: {}, userCouponId: {}",
                    couponId, userId, userCoupon.getId());
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("优惠券领取入库失败, couponId: {}, userId: {}", couponId, userId, e);
            throw e;
        }
    }
}