package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 签到结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到结果")
public class SignResultVO implements Serializable {

    @Schema(description = "签到是否成功")
    private Boolean success;

    @Schema(description = "获得积分")
    private Integer rewardPoints;

    @Schema(description = "连续签到天数")
    private Integer continuousDays;

    @Schema(description = "是否获得连续签到奖励")
    private Boolean hasContinuousReward;

    @Schema(description = "连续签到奖励积分")
    private Integer continuousRewardPoints;

    @Schema(description = "消息提示")
    private String message;
}