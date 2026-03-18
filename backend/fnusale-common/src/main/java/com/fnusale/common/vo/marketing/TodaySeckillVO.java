package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 今日秒杀VO
 */
@Data
@Schema(description = "今日秒杀时间表")
public class TodaySeckillVO implements Serializable {

    @Schema(description = "时间段（如 10:00）")
    private String timeSlot;

    @Schema(description = "该时间段的秒杀活动")
    private List<SeckillActivityVO> activities;
}