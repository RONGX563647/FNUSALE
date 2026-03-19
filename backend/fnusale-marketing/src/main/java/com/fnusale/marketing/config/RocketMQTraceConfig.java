package com.fnusale.marketing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ 消息轨迹配置
 *
 * 消息轨迹功能可以记录消息从生产到消费的全过程，便于问题排查和性能分析。
 *
 * 轨迹信息包括:
 * - 生产时间、Broker 地址、消息 ID
 * - 发送耗时、发送状态
 * - 消费时间、消费耗时、消费状态
 *
 * 开启方式:
 * 1. Broker 端配置 traceTopicEnable=true
 * 2. 生产者端通过 rocketmq.producer.enable-msg-trace=true 配置
 * 3. 消费者端通过 @RocketMQMessageListener 的属性配置
 *
 * 查询轨迹:
 * 1. RocketMQ Console 的消息轨迹查询功能
 * 2. 通过代码查询 TRACE_TOPIC
 */
@Slf4j
@Configuration
public class RocketMQTraceConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    /**
     * 轨迹 Topic
     * 默认为 RMQ_SYS_TRACE_TOPIC
     */
    public static final String TRACE_TOPIC = "RMQ_SYS_TRACE_TOPIC";

    /**
     * RocketMQ Console 地址（用于构建轨迹查询链接）
     */
    @Value("${rocketmq.console.url:http://localhost:8080}")
    private String consoleUrl;

    /**
     * 构建轨迹查询 URL
     *
     * @param messageId 消息 ID
     * @return 查询 URL
     */
    public String buildTraceQueryUrl(String messageId) {
        return consoleUrl + "/#/messageTrace?messageId=" + messageId;
    }

    /**
     * 记录消息轨迹日志
     *
     * @param topic      消息 Topic
     * @param messageId  消息 ID
     * @param operation  操作类型（SEND/CONSUME）
     * @param status     状态（SUCCESS/FAIL）
     * @param durationMs 耗时（毫秒）
     */
    public void logTrace(String topic, String messageId, String operation, String status, long durationMs) {
        log.info("消息轨迹: topic={}, messageId={}, operation={}, status={}, durationMs={}, traceUrl={}",
                topic, messageId, operation, status, durationMs, buildTraceQueryUrl(messageId));
    }
}