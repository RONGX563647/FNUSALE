package com.fnusale.common.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品VO
 */
@Data
@Schema(description = "商品信息")
public class ProductVO implements Serializable {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "品类ID")
    private Long categoryId;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "新旧程度（NEW-全新，90_NEW-9成新，80_NEW-8成新，70_NEW-7成新，OLD-老旧）", allowableValues = {"NEW", "90_NEW", "80_NEW", "70_NEW", "OLD"})
    private String newDegree;

    @Schema(description = "新旧程度描述")
    private String newDegreeDesc;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "商品图片列表")
    private List<String> imageUrls;

    @Schema(description = "主图URL")
    private String mainImageUrl;

    @Schema(description = "是否秒杀商品（0-否，1-是）", allowableValues = {"0", "1"})
    private Integer isSeckill;

    @Schema(description = "秒杀库存")
    private Integer seckillStock;

    @Schema(description = "自提点ID")
    private Long pickPointId;

    @Schema(description = "自提点名称")
    private String pickPointName;

    @Schema(description = "商品状态（DRAFT-草稿，ON_SHELF-上架，SOLD_OUT-已成交，OFF_SHELF-下架，ILLEGAL-违规）", allowableValues = {"DRAFT", "ON_SHELF", "SOLD_OUT", "OFF_SHELF", "ILLEGAL"})
    private String productStatus;

    @Schema(description = "发布者ID")
    private Long userId;

    @Schema(description = "发布者用户名")
    private String publisherName;

    @Schema(description = "发布者信誉分")
    private Integer publisherCreditScore;

    @Schema(description = "距离（米）")
    private Double distance;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}