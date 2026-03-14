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
 * 秒杀活动DTO
 */
@Data
@Schema(description = "秒杀活动请求")
public class SeckillActivityDTO implements Serializable {

    @Schema(description = "活动名称（如\"每晚8点教材秒杀\"）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "秒杀价格（元，低于商品原价）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀价格不能为空")
    @Positive(message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀总库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀库存不能为空")
    @Positive(message = "秒杀库存必须大于0")
    private Integer totalStock;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;
}