package com.fnusale.trade.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.trade.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单消息生产者
 * 负责发送各类订单相关消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送订单超时取消消息（延迟消息）
     * 默认30分钟后触发
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     */
    public void sendOrderTimeoutMessage(Long orderId, String orderNo) {
        sendOrderTimeoutMessage(orderId, orderNo, 30);
    }

    /**
     * 发送订单超时取消消息（延迟消息）
     *
     * @param orderId   订单ID
     * @param orderNo   订单编号
     * @param delayMinutes 延迟分钟数
     */
    public void sendOrderTimeoutMessage(Long orderId, String orderNo, int delayMinutes) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .eventId(UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .build();

        String destination = RocketMQConstants.ORDER_TIMEOUT_TOPIC + ":" + RocketMQConstants.ORDER_TIMEOUT_TAG_CANCEL;

        try {
            // 使用延迟消息，delayLevel参考：
            // 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            // Level 16 = 30分钟
            int delayLevel = convertToDelayLevel(delayMinutes);

            Message<OrderEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .setHeader("DELAY", delayLevel)
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("订单超时消息发送成功，orderId: {}, delayLevel: {}", orderId, delayLevel);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("订单超时消息发送失败，orderId: {}", orderId, e);
                }
            });

            log.info("发送订单超时消息，orderId: {}, orderNo: {}, delayMinutes: {}", orderId, orderNo, delayMinutes);
        } catch (Exception e) {
            log.error("发送订单超时消息异常，orderId: {}", orderId, e);
        }
    }

    /**
     * 发送支付成功消息
     */
    public void sendPaySuccessMessage(OrderPayEvent event) {
        String destination = RocketMQConstants.ORDER_PAY_TOPIC + ":" + RocketMQConstants.ORDER_PAY_TAG_SUCCESS;

        try {
            Message<OrderPayEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("支付成功消息发送成功，orderId: {}", event.getOrderId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("支付成功消息发送失败，orderId: {}", event.getOrderId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送支付成功消息异常，orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 发送订单完成消息（确认收货）
     */
    public void sendOrderCompleteMessage(OrderCompleteEvent event) {
        String destination = RocketMQConstants.ORDER_COMPLETE_TOPIC + ":" + RocketMQConstants.ORDER_COMPLETE_TAG_UPDATE_PRODUCT;

        try {
            Message<OrderCompleteEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("订单完成消息发送成功，orderId: {}", event.getOrderId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("订单完成消息发送失败，orderId: {}", event.getOrderId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送订单完成消息异常，orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 发送退款处理消息
     */
    public void sendRefundMessage(OrderRefundEvent event) {
        String destination = RocketMQConstants.ORDER_REFUND_TOPIC + ":" + RocketMQConstants.ORDER_REFUND_TAG_PROCESS;

        try {
            Message<OrderRefundEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("退款消息发送成功，orderId: {}", event.getOrderId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("退款消息发送失败，orderId: {}", event.getOrderId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送退款消息异常，orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 发送评价消息
     */
    public void sendEvaluationMessage(OrderEvaluationEvent event) {
        String destination = RocketMQConstants.ORDER_EVALUATION_TOPIC + ":" + RocketMQConstants.ORDER_EVALUATION_TAG_UPDATE_RATING;

        try {
            Message<OrderEvaluationEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("评价消息发送成功，orderId: {}", event.getOrderId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("评价消息发送失败，orderId: {}", event.getOrderId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送评价消息异常，orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 发送纠纷创建消息
     */
    public void sendDisputeCreateMessage(DisputeEvent event) {
        String destination = RocketMQConstants.TRADE_DISPUTE_TOPIC + ":" + RocketMQConstants.TRADE_DISPUTE_TAG_CREATE;

        try {
            Message<DisputeEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("纠纷创建消息发送成功，disputeId: {}", event.getDisputeId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("纠纷创建消息发送失败，disputeId: {}", event.getDisputeId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送纠纷创建消息异常，disputeId: {}", event.getDisputeId(), e);
        }
    }

    /**
     * 发送订单创建通知消息（通知卖家）
     */
    public void sendOrderCreateNotifyMessage(OrderEvent event) {
        String destination = RocketMQConstants.ORDER_CREATE_TOPIC + ":" + RocketMQConstants.ORDER_CREATE_TAG_NOTIFY_SELLER;

        try {
            Message<OrderEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("KEYS", event.getEventId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("订单创建通知消息发送成功，orderId: {}", event.getOrderId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("订单创建通知消息发送失败，orderId: {}", event.getOrderId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送订单创建通知消息异常，orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 将分钟数转换为RocketMQ延迟级别
     * RocketMQ延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 对应级别：    1  2   3   4  5  6  7  8  9 10 11 12 13 14  15  16  17 18 19
     */
    private int convertToDelayLevel(int minutes) {
        if (minutes <= 1) return 5;      // 1分钟
        if (minutes <= 2) return 6;      // 2分钟
        if (minutes <= 3) return 7;      // 3分钟
        if (minutes <= 4) return 8;      // 4分钟
        if (minutes <= 5) return 9;      // 5分钟
        if (minutes <= 6) return 10;     // 6分钟
        if (minutes <= 7) return 11;     // 7分钟
        if (minutes <= 8) return 12;     // 8分钟
        if (minutes <= 9) return 13;     // 9分钟
        if (minutes <= 10) return 14;    // 10分钟
        if (minutes <= 20) return 15;    // 20分钟
        if (minutes <= 30) return 16;    // 30分钟
        if (minutes <= 60) return 17;    // 1小时
        return 18;                        // 2小时
    }
}