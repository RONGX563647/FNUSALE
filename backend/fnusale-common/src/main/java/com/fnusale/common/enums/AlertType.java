package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 告警类型枚举
 */
@Getter
@AllArgsConstructor
public enum AlertType {

    /**
     * 消息队列死信队列告警
     */
    MQ_DLQ("mq-dlq", "消息队列死信队列告警"),

    /**
     * 消息积压告警
     */
    MESSAGE_LAG("message-lag", "消息积压告警"),

    /**
     * 消息发送失败告警
     */
    MQ_SEND_FAILURE("mq-send-failure", "消息发送失败告警"),

    /**
     * 消息消费失败告警
     */
    MQ_CONSUME_FAILURE("mq-consume-failure", "消息消费失败告警"),

    /**
     * 系统告警
     */
    SYSTEM("system", "系统告警"),

    /**
     * 业务告警
     */
    BUSINESS("business", "业务告警");

    private final String code;
    private final String desc;
}