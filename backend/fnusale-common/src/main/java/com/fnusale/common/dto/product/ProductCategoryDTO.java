package com.fnusale.common.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品品类DTO
 */
@Data
@Schema(description = "商品品类请求")
public class ProductCategoryDTO implements Serializable {

    @Schema(description = "品类名称（如\"教材\"\"耳机\"\"篮球\"\"洗衣机\"）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "品类名称不能为空")
    private String categoryName;

    @Schema(description = "父品类ID（多级分类，如\"电子产品\"→\"耳机\"）")
    private Long parentCategoryId;

    @Schema(description = "AI分类映射值（对接阿里云视觉AI返回结果，自动匹配品类）")
    private String aiMappingValue;

    @Schema(description = "启用状态（0-禁用，1-启用）", defaultValue = "1")
    private Integer enableStatus = 1;
}