package com.fnusale.common.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布DTO
 */
@Data
@Schema(description = "商品发布请求")
public class ProductPublishDTO implements Serializable {

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @Schema(description = "品类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "品类不能为空")
    private Long categoryId;

    @Schema(description = "新旧程度（NEW-全新，90_NEW-9成新，80_NEW-8成新，70_NEW-7成新，OLD-老旧）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"NEW", "90_NEW", "80_NEW", "70_NEW", "OLD"})
    @NotBlank(message = "新旧程度不能为空")
    private String newDegree;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    @Positive(message = "售价必须大于0")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "商品图片URL列表（至少上传1张，最多9张）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://oss.example.com/img1.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "自提点ID")
    private Long pickPointId;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "是否秒杀商品（0-否，1-是）", allowableValues = {"0", "1"})
    private Integer isSeckill;

    @Schema(description = "秒杀库存")
    private Integer seckillStock;
}