package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录VO
 */
@Data
@Schema(description = "审核记录")
public class AuditRecordVO implements Serializable {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "审核管理员ID")
    private Long adminId;

    @Schema(description = "审核管理员名称")
    private String adminName;

    @Schema(description = "审核结果（PASS/REJECT）")
    private String auditResult;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
}