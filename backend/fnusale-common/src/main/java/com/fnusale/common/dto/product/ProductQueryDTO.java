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

    @Schema(description = "新旧程度（NEW-全新，90_NEW-9成新，80_NEW-8成新，70_NEW-7成新，OLD-老旧）", allowableValues = {"NEW", "90_NEW", "80_NEW", "70_NEW", "OLD"})
    private String newDegree;

    @Schema(description = "是否秒杀商品（0-否，1-是）", allowableValues = {"0", "1"})
    private Integer isSeckill;

    @Schema(description = "商品状态（DRAFT-草稿，ON_SHELF-上架，SOLD_OUT-已成交，OFF_SHELF-下架，ILLEGAL-违规）", allowableValues = {"DRAFT", "ON_SHELF", "SOLD_OUT", "OFF_SHELF", "ILLEGAL"})
    private String productStatus;

    @Schema(description = "排序字段（price-价格，distance-距离，time-时间）", allowableValues = {"price", "distance", "time"})
    private String sortBy;

    @Schema(description = "排序方式（asc-升序，desc-降序）", allowableValues = {"asc", "desc"})
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