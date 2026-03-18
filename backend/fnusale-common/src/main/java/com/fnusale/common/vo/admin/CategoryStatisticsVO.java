package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 品类统计VO
 */
@Data
@Schema(description = "品类统计")
public class CategoryStatisticsVO implements Serializable {

    @Schema(description = "品类ID")
    private Long categoryId;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "商品数量")
    private Integer productCount;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "成交金额")
    private BigDecimal orderAmount;

    @Schema(description = "占比（%）")
    private BigDecimal percentage;
}