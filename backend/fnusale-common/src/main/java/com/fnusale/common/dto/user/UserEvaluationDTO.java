package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户评价DTO
 */
@Data
@Schema(description = "用户评价请求")
public class UserEvaluationDTO implements Serializable {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "评分(1-5星)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    @Schema(description = "评价标签（多个用逗号分隔）")
    private String evaluationTag;

    @Schema(description = "评价内容")
    private String evaluationContent;

    @Schema(description = "评价图片地址")
    private String evaluationImageUrl;

    @Schema(description = "是否匿名(0-否, 1-是)")
    private Integer isAnonymous;
}