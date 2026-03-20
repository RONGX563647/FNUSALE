package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户认证审核事件
 * 用于认证通过/驳回后通知用户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthAuditEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 认证结果（PASS/REJECT）
     */
    private String auditResult;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 事件ID
     */
    private String eventId;
}