package com.fnusale.marketing.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RocketMQ 消息监控指标配置
 * 暴露消息发送/消费相关指标给 Prometheus
 */
@Configuration
public class RocketMQMetricsConfig {

    private final MeterRegistry meterRegistry;

    public RocketMQMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // ==================== 消息发送指标 ====================

    /**
     * 消息发送成功计数器
     */
    @Getter
    private final AtomicLong messagesSentSuccess = new AtomicLong(0);

    /**
     * 消息发送失败计数器
     */
    @Getter
    private final AtomicLong messagesSentFailed = new AtomicLong(0);

    /**
     * 消息消费成功计数器
     */
    @Getter
    private final AtomicLong messagesConsumedSuccess = new AtomicLong(0);

    /**
     * 消息消费失败计数器
     */
    @Getter
    private final AtomicLong messagesConsumedFailed = new AtomicLong(0);

    /**
     * 消息发送耗时（纳秒）
     */
    @Getter
    private final AtomicLong sendLatencyNanos = new AtomicLong(0);

    /**
     * 配置监控指标
     */
    public void registerMetrics() {
        // 消息发送成功数
        Gauge.builder("rocketmq_producer_messages_sent_total", messagesSentSuccess, AtomicLong::get)
                .description("Total number of messages sent successfully")
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);

        // 消息发送失败数
        Gauge.builder("rocketmq_producer_messages_sent_failed", messagesSentFailed, AtomicLong::get)
                .description("Total number of messages failed to send")
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);

        // 消息消费成功数
        Gauge.builder("rocketmq_consumer_messages_consumed_total", messagesConsumedSuccess, AtomicLong::get)
                .description("Total number of messages consumed successfully")
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);

        // 消息消费失败数
        Gauge.builder("rocketmq_consumer_messages_consumed_failed", messagesConsumedFailed, AtomicLong::get)
                .description("Total number of messages failed to consume")
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);

        // 消息发送延迟
        Gauge.builder("rocketmq_producer_send_latency_nanoseconds", sendLatencyNanos, AtomicLong::get)
                .description("Message send latency in nanoseconds")
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);
    }

    /**
     * 记录消息发送成功
     */
    public void recordSendSuccess() {
        messagesSentSuccess.incrementAndGet();
    }

    /**
     * 记录消息发送失败
     */
    public void recordSendFailure() {
        messagesSentFailed.incrementAndGet();
    }

    /**
     * 记录消息消费成功
     */
    public void recordConsumeSuccess() {
        messagesConsumedSuccess.incrementAndGet();
    }

    /**
     * 记录消息消费失败
     */
    public void recordConsumeFailure() {
        messagesConsumedFailed.incrementAndGet();
    }

    /**
     * 记录消息发送延迟
     */
    public void recordSendLatency(long nanos) {
        sendLatencyNanos.set(nanos);
    }

    /**
     * 获取消息发送计时器
     */
    public Timer getSendTimer(String topic) {
        return Timer.builder("rocketmq_producer_send_duration")
                .description("Message send duration")
                .tag("topic", topic)
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);
    }

    /**
     * 获取消息消费计时器
     */
    public Timer getConsumeTimer(String topic, String consumerGroup) {
        return Timer.builder("rocketmq_consumer_consume_duration")
                .description("Message consume duration")
                .tag("topic", topic)
                .tag("consumer_group", consumerGroup)
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);
    }

    /**
     * 获取消息计数器
     */
    public Counter getMessageCounter(String name, String topic, String result) {
        return Counter.builder(name)
                .tag("topic", topic)
                .tag("result", result)
                .tag("service", "fnusale-marketing")
                .register(meterRegistry);
    }
}