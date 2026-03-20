package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IM消息发送事件
 * 用于消息队列异步处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImMessageSendEvent {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型 (TEXT/IMAGE/VOICE)
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 语音时长（秒）
     */
    private Integer duration;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 事件创建时间
     */
    private LocalDateTime eventTime;
}