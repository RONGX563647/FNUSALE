package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户评价分VO
 */
@Data
@Schema(description = "用户评价分")
public class UserRatingVO implements Serializable {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "综合评分")
    private BigDecimal overallRating;

    @Schema(description = "评分等级")
    private String ratingLevel;

    @Schema(description = "累计评价数")
    private Integer totalEvaluations;

    @Schema(description = "好评数")
    private Integer positiveCount;

    @Schema(description = "中评数")
    private Integer neutralCount;

    @Schema(description = "差评数")
    private Integer negativeCount;

    @Schema(description = "好评率（%）")
    private BigDecimal positiveRate;

    @Schema(description = "近30天评价数")
    private Integer last30dEvaluations;

    @Schema(description = "近30天评分")
    private BigDecimal last30dRating;
}