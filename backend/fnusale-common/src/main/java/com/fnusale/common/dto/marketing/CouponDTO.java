package com.fnusale.common.dto.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券DTO
 */
@Data
@Schema(description = "优惠券请求")
public class CouponDTO implements Serializable {

    @Schema(description = "优惠券名称（如\"毕业季满20减5券\"）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "优惠券名称不能为空")
    private String couponName;

    @Schema(description = "类型（FULL_REDUCE-满减，DIRECT_REDUCE-直降，CATEGORY-品类券）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"FULL_REDUCE", "DIRECT_REDUCE", "CATEGORY"})
    @NotBlank(message = "优惠券类型不能为空")
    private String couponType;

    @Schema(description = "满减金额（couponType为FULL_REDUCE时必填，如满20减5则填20）")
    private BigDecimal fullAmount;

    @Schema(description = "抵扣金额（如满20减5则填5）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "抵扣金额不能为空")
    @Positive(message = "抵扣金额必须大于0")
    private BigDecimal reduceAmount;

    @Schema(description = "品类ID（couponType为CATEGORY时必填）")
    private Long categoryId;

    @Schema(description = "发放总数", defaultValue = "100")
    private Integer totalCount = 100;

    @Schema(description = "有效期开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "有效期结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "启用状态（0-禁用，1-启用）", defaultValue = "1")
    private Integer enableStatus = 1;
}