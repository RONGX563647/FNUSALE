package com.fnusale.common.vo.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单统计信息")
public class OrderStatisticsVO implements Serializable {

    @Schema(description = "待付款订单数")
    private Integer unpaidCount;

    @Schema(description = "待自提订单数")
    private Integer waitPickCount;

    @Schema(description = "已成交订单数")
    private Integer successCount;

    @Schema(description = "已取消订单数")
    private Integer cancelCount;

    @Schema(description = "退款中订单数")
    private Integer refundCount;

    @Schema(description = "总订单数")
    private Integer totalCount;
}