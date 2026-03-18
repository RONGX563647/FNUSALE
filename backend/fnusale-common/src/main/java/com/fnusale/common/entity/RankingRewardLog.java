package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排行榜奖励记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ranking_reward_log")
public class RankingRewardLog implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排行类型 (ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER)
     */
    private String rankType;

    /**
     * 排行日期
     */
    private LocalDate rankDate;

    /**
     * 排名
     */
    private Integer rankPosition;

    /**
     * 奖励积分
     */
    private Integer rewardPoints;

    /**
     * 奖励优惠券ID
     */
    private Long rewardCouponId;

    /**
     * 是否已领取 (0-未领取, 1-已领取)
     */
    private Integer isClaimed;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}