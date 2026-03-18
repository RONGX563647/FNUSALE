package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待审核商品VO
 */
@Data
@Schema(description = "待审核商品信息")
public class PendingProductVO implements Serializable {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "主图URL")
    private String mainImageUrl;

    @Schema(description = "商品描述")
    private String productDesc;
}