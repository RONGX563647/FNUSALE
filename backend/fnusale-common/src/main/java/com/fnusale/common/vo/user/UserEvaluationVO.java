package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户评价VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户评价")
public class UserEvaluationVO implements Serializable {

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

    @Schema(description = "评分(1-5星)")
    private Integer score;

    @Schema(description = "评价标签")
    private String evaluationTag;

    @Schema(description = "评价内容")
    private String evaluationContent;

    @Schema(description = "评价图片地址")
    private String evaluationImageUrl;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "卖家回复内容")
    private String replyContent;

    @Schema(description = "卖家回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "追加评价内容")
    private String appendContent;

    @Schema(description = "追加评价图片")
    private String appendImageUrl;

    @Schema(description = "追加评价时间")
    private LocalDateTime appendTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}