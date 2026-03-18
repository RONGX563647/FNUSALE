package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核统计VO
 */
@Data
@Schema(description = "审核统计")
public class AuditStatisticsVO implements Serializable {

    @Schema(description = "待审核数量")
    private Integer pendingCount;

    @Schema(description = "今日审核通过数")
    private Integer todayPassCount;

    @Schema(description = "今日审核驳回数")
    private Integer todayRejectCount;

    @Schema(description = "今日审核通过率（%）")
    private Double todayPassRate;
}