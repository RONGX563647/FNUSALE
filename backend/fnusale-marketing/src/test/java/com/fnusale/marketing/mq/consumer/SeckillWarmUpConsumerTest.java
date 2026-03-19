package com.fnusale.marketing.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.event.SeckillWarmUpEvent;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 秒杀预热消费者测试
 */
@ExtendWith(MockitoExtension.class)
class SeckillWarmUpConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeckillWarmUpConsumer consumer;

    private SeckillWarmUpEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = SeckillWarmUpEvent.builder()
                .activityId(100L)
                .productId(1000L)
                .seckillPrice(new BigDecimal("99.00"))
                .stock(50)
                .startTime(LocalDateTime.now().plusHours(1))
                .eventId("warmup-001")
                .build();
    }

    @Nested
    @DisplayName("消费秒杀预热消息测试")
    class OnMessageTests {

        @Test
        @DisplayName("成功预热库存到Redis")
        void shouldWarmUpStockSuccessfully() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            consumer.onMessage(testEvent);

            // 验证库存设置
            verify(valueOperations).set(
                eq(MarketingConstants.SECKILL_STOCK_KEY_PREFIX + "100"),
                eq("50")
            );
            // 验证清理已购买用户集合
            verify(redisTemplate).delete(
                eq(MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + "100")
            );
        }

        @Test
        @DisplayName("预热失败抛出异常触发MQ重试")
        void shouldThrowExceptionOnFailure() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doThrow(new RuntimeException("Redis连接失败"))
                .when(valueOperations).set(anyString(), anyString());

            assertThrows(RuntimeException.class, () -> consumer.onMessage(testEvent));
        }
    }
}