package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.event.CouponReceiveEvent;
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
 * 优惠券领取消费者测试
 */
@ExtendWith(MockitoExtension.class)
class CouponReceiveConsumerTest {

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CouponReceiveConsumer consumer;

    private CouponReceiveEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = CouponReceiveEvent.builder()
                .couponId(1L)
                .userId(1L)
                .expireTime(LocalDateTime.now().plusDays(30))
                .eventId("receive-001")
                .receiveTime(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("消费优惠券领取消息测试")
    class OnMessageTests {

        @Test
        @DisplayName("首次消费成功 - 异步入库")
        void shouldConsumeSuccessfully() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(userCouponMapper.insert(any())).thenReturn(1);

            consumer.onMessage(testEvent);

            verify(userCouponMapper).insert(any());
        }

        @Test
        @DisplayName("幂等性检查 - 重复消息跳过")
        void shouldSkipDuplicateMessage() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false);

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
        }

        @Test
        @DisplayName("用户已领取时跳过（双重检查）")
        void shouldSkipWhenUserAlreadyReceived() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(1);

            consumer.onMessage(testEvent);

            verify(userCouponMapper, never()).insert(any());
        }

        @Test
        @DisplayName("处理失败时删除幂等键允许重试")
        void shouldDeleteIdempotentKeyOnFailure() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(userCouponMapper.insert(any())).thenThrow(new RuntimeException("DB错误"));

            assertThrows(RuntimeException.class, () -> consumer.onMessage(testEvent));

            verify(redisTemplate).delete(anyString());
        }
    }
}