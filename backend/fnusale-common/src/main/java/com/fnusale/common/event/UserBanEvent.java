package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户封禁事件
 * 用于封禁/解封后通知用户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBanEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型（BAN/UNBAN）
     */
    private String operateType;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 事件ID
     */
    private String eventId;
}