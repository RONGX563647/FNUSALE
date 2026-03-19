package com.fnusale.common.vo.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单评价VO
 */
@Data
@Schema(description = "订单评价信息")
public class EvaluationVO implements Serializable {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "评价者ID")
    private Long evaluatorId;

    @Schema(description = "评价者用户名")
    private String evaluatorName;

    @Schema(description = "评价者头像")
    private String evaluatorAvatar;

    @Schema(description = "被评价者ID")
    private Long evaluatedId;

    @Schema(description = "被评价者用户名")
    private String evaluatedName;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "评分（1-5星）")
    private Integer score;

    @Schema(description = "评价标签")
    private String evaluationTag;

    @Schema(description = "评价内容")
    private String evaluationContent;

    @Schema(description = "评价图片地址")
    private String evaluationImageUrl;

    @Schema(description = "卖家回复内容")
    private String replyContent;

    @Schema(description = "卖家回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}