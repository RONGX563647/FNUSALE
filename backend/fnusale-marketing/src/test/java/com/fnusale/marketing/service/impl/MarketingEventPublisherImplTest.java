package com.fnusale.marketing.service.impl;

import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.CouponReceiveEvent;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.common.event.SeckillWarmUpEvent;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息发布者单元测试
 */
@ExtendWith(MockitoExtension.class)
class MarketingEventPublisherImplTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private MarketingEventPublisherImpl eventPublisher;

    private SeckillOrderEvent seckillOrderEvent;
    private CouponGrantEvent couponGrantEvent;
    private CouponReceiveEvent couponReceiveEvent;
    private SeckillWarmUpEvent seckillWarmUpEvent;

    @BeforeEach
    void setUp() {
        seckillOrderEvent = SeckillOrderEvent.builder()
                .userId(1L)
                .activityId(100L)
                .productId(1000L)
                .seckillPrice(new BigDecimal("99.00"))
                .quantity(1)
                .eventId("test-event-id-001")
                .seckillTime(LocalDateTime.now())
                .build();

        couponGrantEvent = CouponGrantEvent.builder()
                .couponId(1L)
                .userId(1L)
                .batchId("batch-001")
                .expireTime(LocalDateTime.now().plusDays(30))
                .eventId("event-001")
                .grantTime(LocalDateTime.now())
                .build();

        couponReceiveEvent = CouponReceiveEvent.builder()
                .couponId(1L)
                .userId(1L)
                .expireTime(LocalDateTime.now().plusDays(30))
                .eventId("receive-001")
                .receiveTime(LocalDateTime.now())
                .build();

        seckillWarmUpEvent = SeckillWarmUpEvent.builder()
                .activityId(100L)
                .productId(1000L)
                .seckillPrice(new BigDecimal("99.00"))
                .stock(50)
                .startTime(LocalDateTime.now().plusHours(1))
                .eventId("warmup-001")
                .build();
    }

    @Nested
    @DisplayName("秒杀订单消息测试")
    class SeckillOrderEventTests {

        @Test
        @DisplayName("异步发送秒杀订单消息")
        void shouldSendSeckillOrderEventAsync() {
            String expectedDestination = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.SECKILL_ORDER_TAG_CREATE;

            eventPublisher.publishSeckillOrderEvent(seckillOrderEvent);

            verify(rocketMQTemplate).asyncSend(eq(expectedDestination), eq(seckillOrderEvent), any(SendCallback.class));
        }

        @Test
        @DisplayName("同步发送顺序秒杀订单消息")
        void shouldSendSeckillOrderEventOrdered() {
            String expectedDestination = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.SECKILL_ORDER_TAG_CREATE;
            SendResult mockResult = mock(SendResult.class);
            MessageQueue mockQueue = new MessageQueue();
            when(mockResult.getMessageQueue()).thenReturn(mockQueue);
            when(mockResult.getMsgId()).thenReturn("msg-123");

            when(rocketMQTemplate.syncSendOrderly(expectedDestination, seckillOrderEvent, "user-1"))
                    .thenReturn(mockResult);

            assertDoesNotThrow(() -> eventPublisher.publishSeckillOrderEventOrdered(seckillOrderEvent, "user-1"));

            verify(rocketMQTemplate).syncSendOrderly(expectedDestination, seckillOrderEvent, "user-1");
        }

        @Test
        @DisplayName("顺序消息发送失败时抛出异常")
        void shouldThrowExceptionWhenOrderedSendFails() {
            String expectedDestination = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.SECKILL_ORDER_TAG_CREATE;

            when(rocketMQTemplate.syncSendOrderly(expectedDestination, seckillOrderEvent, "user-1"))
                    .thenThrow(new RuntimeException("MQ连接失败"));

            assertThrows(RuntimeException.class, () ->
                eventPublisher.publishSeckillOrderEventOrdered(seckillOrderEvent, "user-1"));
        }
    }

    @Nested
    @DisplayName("优惠券发放消息测试")
    class CouponGrantEventTests {

        @Test
        @DisplayName("异步发送优惠券发放消息")
        void shouldSendCouponGrantEventAsync() {
            String expectedDestination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;

            eventPublisher.publishCouponGrantEvent(couponGrantEvent);

            verify(rocketMQTemplate).asyncSend(eq(expectedDestination), eq(couponGrantEvent), any(SendCallback.class));
        }

        @Test
        @DisplayName("同步发送顺序优惠券发放消息")
        void shouldSendCouponGrantEventOrdered() {
            String expectedDestination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMessageQueue()).thenReturn(new MessageQueue());
            when(mockResult.getMsgId()).thenReturn("msg-456");

            when(rocketMQTemplate.syncSendOrderly(expectedDestination, couponGrantEvent, "user-1"))
                    .thenReturn(mockResult);

            assertDoesNotThrow(() -> eventPublisher.publishCouponGrantEventOrdered(couponGrantEvent, "user-1"));

            verify(rocketMQTemplate).syncSendOrderly(expectedDestination, couponGrantEvent, "user-1");
        }

        @Test
        @DisplayName("批量发送优惠券发放消息")
        void shouldSendCouponGrantBatch() {
            String expectedDestination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;
            List<CouponGrantEvent> events = Arrays.asList(couponGrantEvent, couponGrantEvent);
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMsgId()).thenReturn("batch-msg-001");

            when(rocketMQTemplate.syncSend(eq(expectedDestination), anyList())).thenReturn(mockResult);

            eventPublisher.publishCouponGrantBatch(events);

            verify(rocketMQTemplate).syncSend(eq(expectedDestination), anyList());
        }

        @Test
        @DisplayName("批量发送为空时不执行")
        void shouldNotSendWhenBatchIsEmpty() {
            eventPublisher.publishCouponGrantBatch(null);
            eventPublisher.publishCouponGrantBatch(List.of());

            verify(rocketMQTemplate, never()).syncSend(anyString(), anyList());
        }

        @Test
        @DisplayName("批量顺序发送优惠券发放消息")
        void shouldSendCouponGrantBatchOrdered() {
            String expectedDestination = RocketMQConstants.COUPON_GRANT_TOPIC + ":" + RocketMQConstants.COUPON_GRANT_TAG_BATCH;
            List<CouponGrantEvent> events = Arrays.asList(couponGrantEvent, couponGrantEvent);
            List<String> hashKeys = Arrays.asList("user-1", "user-2");
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMsgId()).thenReturn("msg-789");

            when(rocketMQTemplate.syncSendOrderly(anyString(), any(CouponGrantEvent.class), anyString()))
                    .thenReturn(mockResult);

            eventPublisher.publishCouponGrantBatchOrdered(events, hashKeys);

            verify(rocketMQTemplate, times(2)).syncSendOrderly(eq(expectedDestination), any(CouponGrantEvent.class), anyString());
        }

        @Test
        @DisplayName("批量顺序发送时hashKeys数量不匹配抛出异常")
        void shouldThrowExceptionWhenHashKeysSizeMismatch() {
            List<CouponGrantEvent> events = Arrays.asList(couponGrantEvent, couponGrantEvent);
            List<String> hashKeys = List.of("user-1"); // 数量不匹配

            assertThrows(IllegalArgumentException.class, () ->
                eventPublisher.publishCouponGrantBatchOrdered(events, hashKeys));
        }
    }

    @Nested
    @DisplayName("优惠券领取消息测试")
    class CouponReceiveEventTests {

        @Test
        @DisplayName("异步发送优惠券领取消息")
        void shouldSendCouponReceiveEventAsync() {
            String expectedDestination = RocketMQConstants.COUPON_RECEIVE_TOPIC + ":" + RocketMQConstants.COUPON_RECEIVE_TAG_RECEIVE;

            eventPublisher.publishCouponReceiveEvent(couponReceiveEvent);

            verify(rocketMQTemplate).asyncSend(eq(expectedDestination), eq(couponReceiveEvent), any(SendCallback.class));
        }

        @Test
        @DisplayName("同步发送顺序优惠券领取消息")
        void shouldSendCouponReceiveEventOrdered() {
            String expectedDestination = RocketMQConstants.COUPON_RECEIVE_TOPIC + ":" + RocketMQConstants.COUPON_RECEIVE_TAG_RECEIVE;
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMessageQueue()).thenReturn(new MessageQueue());
            when(mockResult.getMsgId()).thenReturn("msg-receive-001");

            when(rocketMQTemplate.syncSendOrderly(expectedDestination, couponReceiveEvent, "user-1"))
                    .thenReturn(mockResult);

            assertDoesNotThrow(() -> eventPublisher.publishCouponReceiveEventOrdered(couponReceiveEvent, "user-1"));

            verify(rocketMQTemplate).syncSendOrderly(expectedDestination, couponReceiveEvent, "user-1");
        }
    }

    @Nested
    @DisplayName("秒杀预热延迟消息测试")
    class SeckillWarmUpEventTests {

        @Test
        @DisplayName("发送秒杀预热延迟消息")
        void shouldSendSeckillWarmUpEventWithDelay() {
            String expectedDestination = RocketMQConstants.SECKILL_WARMUP_TOPIC + ":" + RocketMQConstants.SECKILL_WARMUP_TAG_STOCK;
            int delayLevel = 10; // 6分钟延迟
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMsgId()).thenReturn("delay-msg-001");

            when(rocketMQTemplate.syncSend(eq(expectedDestination), any(Message.class), eq(3000L), eq(delayLevel)))
                    .thenReturn(mockResult);

            eventPublisher.publishSeckillWarmUpEvent(seckillWarmUpEvent, delayLevel);

            verify(rocketMQTemplate).syncSend(eq(expectedDestination), any(Message.class), eq(3000L), eq(delayLevel));
        }

        @Test
        @DisplayName("延迟消息发送失败记录日志但不抛出异常")
        void shouldLogErrorWhenWarmUpSendFails() {
            String expectedDestination = RocketMQConstants.SECKILL_WARMUP_TOPIC + ":" + RocketMQConstants.SECKILL_WARMUP_TAG_STOCK;

            when(rocketMQTemplate.syncSend(eq(expectedDestination), any(Message.class), anyLong(), anyInt()))
                    .thenThrow(new RuntimeException("MQ连接失败"));

            // 不应该抛出异常
            assertDoesNotThrow(() -> eventPublisher.publishSeckillWarmUpEvent(seckillWarmUpEvent, 10));
        }
    }

    @Nested
    @DisplayName("优惠券过期提醒消息测试")
    class CouponExpireReminderEventTests {

        @Test
        @DisplayName("异步发送优惠券过期提醒消息")
        void shouldSendCouponExpireReminderEvent() {
            String expectedDestination = RocketMQConstants.COUPON_EXPIRE_REMINDER_TOPIC + ":" + RocketMQConstants.COUPON_EXPIRE_REMINDER_TAG_NOTIFY;
            CouponExpireReminderEvent event = CouponExpireReminderEvent.builder()
                    .eventId("expire-001")
                    .userId(1L)
                    .couponId(1L)
                    .couponName("测试优惠券")
                    .reduceAmount(new BigDecimal("10"))
                    .expireTime(LocalDateTime.now().plusDays(3))
                    .build();

            eventPublisher.publishCouponExpireReminderEvent(event);

            verify(rocketMQTemplate).asyncSend(eq(expectedDestination), eq(event), any(SendCallback.class));
        }
    }

    @Nested
    @DisplayName("同步消息发送测试")
    class SyncSendTests {

        @Test
        @DisplayName("同步发送消息成功")
        void shouldSendSyncSuccessfully() {
            String topic = "test-topic";
            String tag = "test-tag";
            SendResult mockResult = mock(SendResult.class);
            when(mockResult.getMsgId()).thenReturn("sync-msg-001");

            when(rocketMQTemplate.syncSend(anyString(), any(Object.class))).thenReturn(mockResult);

            boolean result = eventPublisher.sendSync(topic, tag, "test-message");

            assertTrue(result);
            verify(rocketMQTemplate).syncSend("test-topic:test-tag", "test-message");
        }

        @Test
        @DisplayName("同步发送消息失败返回false")
        void shouldReturnFalseWhenSyncSendFails() {
            when(rocketMQTemplate.syncSend(anyString(), any(Object.class)))
                    .thenThrow(new RuntimeException("MQ连接失败"));

            boolean result = eventPublisher.sendSync("topic", "tag", "message");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("原始消息发送测试")
    class RawMessageTests {

        @Test
        @DisplayName("发送原始消息")
        void shouldSendRawMessage() {
            SendResult mockResult = mock(SendResult.class);
            when(rocketMQTemplate.syncSend(anyString(), anyString())).thenReturn(mockResult);

            eventPublisher.sendRawMessage("topic", "tag", "raw-content");

            verify(rocketMQTemplate).syncSend("topic:tag", "raw-content");
        }
    }
}