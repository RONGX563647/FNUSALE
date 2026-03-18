package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 我的排名VO
 */
@Data
@Schema(description = "我的排名")
public class MyRankingVO implements Serializable {

    @Schema(description = "活跃度排行")
    private RankingInfo activity;

    @Schema(description = "交易排行")
    private RankingInfo trade;

    @Schema(description = "信誉排行")
    private RankingInfo credit;

    @Schema(description = "好评排行")
    private RankingInfo rating;

    @Data
    @Schema(description = "排名信息")
    public static class RankingInfo implements Serializable {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "得分")
        private String score;

        @Schema(description = "是否上榜（前100）")
        private Boolean inList;
    }
}