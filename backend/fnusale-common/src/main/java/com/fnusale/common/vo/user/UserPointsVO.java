package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户积分VO
 */
@Data
@Schema(description = "用户积分")
public class UserPointsVO implements Serializable {

    @Schema(description = "累计获得积分")
    private Integer totalPoints;

    @Schema(description = "可用积分")
    private Integer availablePoints;

    @Schema(description = "已使用积分")
    private Integer usedPoints;

    @Schema(description = "连续签到天数")
    private Integer continuousSignDays;

    @Schema(description = "累计签到天数")
    private Integer totalSignDays;
}