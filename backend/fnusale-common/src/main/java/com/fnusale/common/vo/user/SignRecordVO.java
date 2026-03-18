package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 签到记录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到记录")
public class SignRecordVO implements Serializable {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "签到日期")
    private LocalDate signDate;

    @Schema(description = "签到时间")
    private String signTime;

    @Schema(description = "连续签到天数")
    private Integer continuousDays;

    @Schema(description = "获得积分")
    private Integer rewardPoints;

    @Schema(description = "是否补签")
    private Boolean isRepair;
}