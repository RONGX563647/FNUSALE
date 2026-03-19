package com.fnusale.common.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品品类VO
 */
@Data
@Schema(description = "商品品类信息")
public class ProductCategoryVO implements Serializable {

    @Schema(description = "品类ID")
    private Long id;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "父品类ID")
    private Long parentCategoryId;

    @Schema(description = "AI分类映射值")
    private String aiMappingValue;

    @Schema(description = "启用状态")
    private Integer enableStatus;

    @Schema(description = "子品类列表")
    private List<ProductCategoryVO> children;
}