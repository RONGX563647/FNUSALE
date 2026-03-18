package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动VO
 */
@Data
@Schema(description = "秒杀活动信息")
public class SeckillActivityVO implements Serializable {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态（NOT_START-未开始，ON_GOING-进行中，END-已结束）")
    private String activityStatus;

    @Schema(description = "是否已设置提醒")
    private Boolean reminded;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}