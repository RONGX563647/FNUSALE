package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券VO
 */
@Data
@Schema(description = "优惠券信息")
public class CouponVO implements Serializable {

    @Schema(description = "优惠券ID")
    private Long id;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "类型（FULL_REDUCE-满减，DIRECT_REDUCE-直降，CATEGORY-品类券）")
    private String couponType;

    @Schema(description = "满减门槛金额")
    private BigDecimal fullAmount;

    @Schema(description = "抵扣金额")
    private BigDecimal reduceAmount;

    @Schema(description = "品类ID")
    private Long categoryId;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "发放总数")
    private Integer totalCount;

    @Schema(description = "已领数量")
    private Integer receivedCount;

    @Schema(description = "剩余数量")
    private Integer remainCount;

    @Schema(description = "有效期开始时间")
    private LocalDateTime startTime;

    @Schema(description = "有效期结束时间")
    private LocalDateTime endTime;

    @Schema(description = "启用状态（0-禁用，1-启用）")
    private Integer enableStatus;

    @Schema(description = "是否已领取")
    private Boolean received;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}