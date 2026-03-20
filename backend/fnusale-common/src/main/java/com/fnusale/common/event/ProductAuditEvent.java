package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品审核事件
 * 用于审核通过/驳回后通知用户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAuditEvent {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品发布者ID
     */
    private Long userId;

    /**
     * 审核结果（PASS/REJECT）
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