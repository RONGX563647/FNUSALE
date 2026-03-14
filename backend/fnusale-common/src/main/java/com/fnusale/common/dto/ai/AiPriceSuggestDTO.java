package com.fnusale.common.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * AI价格建议DTO
 */
@Data
@Schema(description = "AI价格建议请求")
public class AiPriceSuggestDTO implements Serializable {

    @Schema(description = "品类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "品类ID不能为空")
    private Long categoryId;

    @Schema(description = "新旧程度（NEW-全新，90_NEW-9成新，80_NEW-8成新，70_NEW-7成新，OLD-老旧）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"NEW", "90_NEW", "80_NEW", "70_NEW", "OLD"})
    @NotBlank(message = "新旧程度不能为空")
    private String newDegree;

    @Schema(description = "商品名称")
    private String productName;
}