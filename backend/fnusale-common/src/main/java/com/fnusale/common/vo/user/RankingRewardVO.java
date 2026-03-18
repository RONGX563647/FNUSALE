package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 排行榜奖励VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排行榜奖励")
public class RankingRewardVO implements Serializable {

    @Schema(description = "奖励ID")
    private Long id;

    @Schema(description = "排行类型")
    private String rankType;

    @Schema(description = "排行类型名称")
    private String rankTypeName;

    @Schema(description = "排行日期")
    private LocalDate rankDate;

    @Schema(description = "排名")
    private Integer rankPosition;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "奖励优惠券ID")
    private Long rewardCouponId;

    @Schema(description = "奖励优惠券名称")
    private String rewardCouponName;

    @Schema(description = "是否已领取")
    private Boolean isClaimed;
}