package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 签到状态VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到状态")
public class SignStatusVO implements Serializable {

    @Schema(description = "今日是否已签到")
    private Boolean hasSigned;

    @Schema(description = "连续签到天数")
    private Integer continuousDays;

    @Schema(description = "累计签到天数")
    private Integer totalDays;

    @Schema(description = "今日可获得积分")
    private Integer todayReward;

    @Schema(description = "下次奖励需要的连续天数")
    private Integer nextRewardDays;

    @Schema(description = "下次奖励积分")
    private Integer nextRewardPoints;
}