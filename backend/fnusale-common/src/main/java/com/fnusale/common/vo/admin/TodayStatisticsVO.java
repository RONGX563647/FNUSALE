package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 今日数据概览VO
 */
@Data
@Schema(description = "今日数据概览")
public class TodayStatisticsVO implements Serializable {

    @Schema(description = "新增用户数")
    private Integer newUserCount;

    @Schema(description = "活跃用户数")
    private Integer activeUserCount;

    @Schema(description = "商品发布数")
    private Integer productPublishCount;

    @Schema(description = "成交订单数")
    private Integer orderSuccessCount;

    @Schema(description = "成交金额")
    private BigDecimal orderSuccessAmount;

    @Schema(description = "待审核商品数")
    private Integer pendingAuditCount;

    @Schema(description = "待审核认证数")
    private Integer pendingAuthCount;
}