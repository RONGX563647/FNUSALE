package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户优惠券信息")
public class UserCouponVO implements Serializable {

    @Schema(description = "用户优惠券ID")
    private Long id;

    @Schema(description = "优惠券ID")
    private Long couponId;

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

    @Schema(description = "状态（UNUSED-未使用，USED-已使用，EXPIRED-已过期）")
    private String couponStatus;

    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;

    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "是否可用")
    private Boolean usable;
}