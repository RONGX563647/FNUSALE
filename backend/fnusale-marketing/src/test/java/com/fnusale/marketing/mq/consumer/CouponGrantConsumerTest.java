package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.marketing.mapper.CouponMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 优惠券发放消费者测试
 */
@ExtendWith(MockitoExtension.class)
class CouponGrantConsumerTest {

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CouponGrantConsumer consumer;

    private CouponGrantEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = CouponGrantEvent.builder()
                .couponId(1L)
                .userId(1L)
                .batchId("batch-001")
                .expireTime(LocalDateTime.now().plusDays(30))
                .eventId("event-001")
                .grantTime(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("消费优惠券发放消息测试")
    class OnMessageTests {

        @Test
        @DisplayName("首次消费成功")
        void shouldConsumeSuccessfully() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(couponMapper.selectById(1L)).thenReturn(mock(com.fnusale.common.entity.Coupon.class));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(1);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);

            consumer.onMessage(testEvent);

            verify(userCouponMapper).insert(any(UserCoupon.class));
            verify(couponMapper).incrementReceivedCount(1L);
        }

        @Test
        @DisplayName("幂等性检查 - 重复消息跳过")
        void shouldSkipDuplicateMessage() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false); // 已存在

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
            verify(couponMapper, never()).incrementReceivedCount(anyLong());
        }

        @Test
        @DisplayName("优惠券不存在时跳过")
        void shouldSkipWhenCouponNotFound() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(couponMapper.selectById(1L)).thenReturn(null);

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
        }

        @Test
        @DisplayName("用户已领取时跳过")
        void shouldSkipWhenUserAlreadyReceived() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(couponMapper.selectById(1L)).thenReturn(mock(com.fnusale.common.entity.Coupon.class));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(1); // 已领取

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
        }

        @Test
        @DisplayName("库存不足时跳过")
        void shouldSkipWhenOutOfStock() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(couponMapper.selectById(1L)).thenReturn(mock(com.fnusale.common.entity.Coupon.class));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(0); // 库存不足

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
        }

        @Test
        @DisplayName("处理失败时删除幂等键允许重试")
        void shouldDeleteIdempotentKeyOnFailure() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(couponMapper.selectById(1L)).thenReturn(mock(com.fnusale.common.entity.Coupon.class));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(1);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenThrow(new RuntimeException("DB错误"));

            // 抛出异常以触发MQ重试
            assertThrows(RuntimeException.class, () -> consumer.onMessage(testEvent));

            verify(redisTemplate).delete(anyString());
        }
    }
}