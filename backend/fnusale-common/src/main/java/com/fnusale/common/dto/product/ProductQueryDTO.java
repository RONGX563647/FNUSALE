package com.fnusale.common.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品查询DTO
 */
@Data
@Schema(description = "商品查询请求")
public class ProductQueryDTO implements Serializable {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "品类ID")
    private Long categoryId;

    @Schema(description = "最低价格")
    private BigDecimal minPrice;

    @Schema(description = "最高价格")
    private BigDecimal maxPrice;

    @Schema(description = "新旧程度")
    private String newDegree;

    @Schema(description = "是否秒杀商品")
    private Integer isSeckill;

    @Schema(description = "商品状态")
    private String productStatus;

    @Schema(description = "排序字段（price/distance/time）")
    private String sortBy;

    @Schema(description = "排序方式（asc/desc）")
    private String sortOrder;

    @Schema(description = "用户经度（用于距离排序）")
    private String longitude;

    @Schema(description = "用户纬度（用于距离排序）")
    private String latitude;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
}