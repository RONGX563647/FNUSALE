package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IM消息撤回事件
 * 用于消息撤回广播
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImMessageRecallEvent {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 撤回者ID
     */
    private Long recallUserId;

    /**
     * 接收者ID（被通知方）
     */
    private Long notifyUserId;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;

    /**
     * 事件创建时间
     */
    private LocalDateTime eventTime;
}