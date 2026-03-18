package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 排行榜用户VO
 */
@Data
@Schema(description = "排行榜用户")
public class RankingUserVO implements Serializable {

    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "是否当前用户")
    private Boolean isCurrentUser;
}