package com.fnusale.user.service.impl;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.RankingRecord;
import com.fnusale.common.entity.RankingRewardLog;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.MyRankingVO;
import com.fnusale.common.vo.user.RankingRewardVO;
import com.fnusale.common.vo.user.RankingUserVO;
import com.fnusale.user.mapper.RankingRecordMapper;
import com.fnusale.user.mapper.RankingRewardLogMapper;
import com.fnusale.user.service.RankingCacheService;
import com.fnusale.user.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 * 基于 Redis ZSET 实现实时排行榜
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingCacheService rankingCacheService;
    private final RankingRecordMapper rankingRecordMapper;
    private final RankingRewardLogMapper rankingRewardLogMapper;

    private static final int RANKING_LIMIT = 100;
    private static final int REWARD_TOP_N = 10; // 前N名有奖励

    @Override
    public List<RankingUserVO> getActivityRanking(String type, String date) {
        String period = convertTypeToPeriod(type);
        return rankingCacheService.getTopN("ACTIVITY", period, RANKING_LIMIT);
    }

    @Override
    public List<RankingUserVO> getTradeRanking(String type, String date) {
        String period = convertTypeToPeriod(type);
        return rankingCacheService.getTopN("TRADE", period, RANKING_LIMIT);
    }

    @Override
    public List<RankingUserVO> getCreditRanking() {
        return rankingCacheService.getTopN("CREDIT", null, RANKING_LIMIT);
    }

    @Override
    public List<RankingUserVO> getRatingRanking() {
        return rankingCacheService.getTopN("RATING", null, RANKING_LIMIT);
    }

    @Override
    public MyRankingVO getMyRanking(Long userId) {
        return MyRankingVO.builder()
            .activity(getUserRankInfo(userId, "ACTIVITY", "daily"))
            .trade(getUserRankInfo(userId, "TRADE", "daily"))
            .credit(getUserRankInfo(userId, "CREDIT", null))
            .rating(getUserRankInfo(userId, "RATING", null))
            .build();
    }

    @Override
    public PageResult<RankingUserVO> getRankingHistory(Long userId, String rankType, Integer pageNum, Integer pageSize) {
        List<RankingRecord> records = rankingRecordMapper.selectHistoryByTypeAndUser(rankType, userId);

        List<RankingUserVO> voList = records.stream()
            .map(record -> RankingUserVO.builder()
                .rank(record.getRankPosition())
                .score(record.getScore())
                .build())
            .collect(Collectors.toList());

        // 分页处理
        int total = voList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<RankingUserVO> pageList = fromIndex < total ?
            voList.subList(fromIndex, toIndex) : Collections.emptyList();

        return new PageResult<>(pageNum, pageSize, total, pageList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimReward(Long userId, Long rewardId) {
        // 查询奖励记录
        RankingRewardLog rewardLog = rankingRewardLogMapper.selectById(rewardId);
        if (rewardLog == null) {
            throw new BusinessException("奖励不存在");
        }

        if (!rewardLog.getUserId().equals(userId)) {
            throw new BusinessException("无权领取此奖励");
        }

        if (rewardLog.getIsClaimed() == 1) {
            throw new BusinessException("奖励已领取");
        }

        // 领取奖励
        int updated = rankingRewardLogMapper.claimReward(rewardId, userId);
        if (updated == 0) {
            throw new BusinessException("领取失败，请重试");
        }

        // 发放积分奖励
        if (rewardLog.getRewardPoints() != null && rewardLog.getRewardPoints() > 0) {
            // TODO: 调用积分服务增加积分
            log.info("用户领取排行奖励, userId: {}, rewardId: {}, points: {}",
                userId, rewardId, rewardLog.getRewardPoints());
        }

        // 发放优惠券奖励
        if (rewardLog.getRewardCouponId() != null) {
            // TODO: 调用营销服务发放优惠券
            log.info("用户领取排行优惠券奖励, userId: {}, rewardId: {}, couponId: {}",
                userId, rewardId, rewardLog.getRewardCouponId());
        }
    }

    @Override
    public List<RankingRewardVO> getMyRewards(Long userId, Boolean isClaimed) {
        List<RankingRewardLog> logs;
        if (isClaimed == null) {
            logs = rankingRewardLogMapper.selectByUserId(userId);
        } else if (isClaimed) {
            logs = rankingRewardLogMapper.selectClaimedByUserId(userId);
        } else {
            logs = rankingRewardLogMapper.selectUnclaimedByUserId(userId);
        }

        return logs.stream()
            .map(this::buildRankingRewardVO)
            .collect(Collectors.toList());
    }

    // ==================== 分数更新方法 ====================

    @Override
    public void incrementActivityScore(Long userId, double score) {
        // 同时更新日榜、周榜、月榜
        rankingCacheService.incrementScore("ACTIVITY", "daily", userId, score);
        rankingCacheService.incrementScore("ACTIVITY", "weekly", userId, score);
        rankingCacheService.incrementScore("ACTIVITY", "monthly", userId, score);
        log.debug("活跃度分数增加: userId={}, score={}", userId, score);
    }

    @Override
    public void incrementTradeScore(Long userId, double amount) {
        rankingCacheService.incrementScore("TRADE", "daily", userId, amount);
        rankingCacheService.incrementScore("TRADE", "weekly", userId, amount);
        rankingCacheService.incrementScore("TRADE", "monthly", userId, amount);
        log.debug("交易分数增加: userId={}, amount={}", userId, amount);
    }

    @Override
    public void updateCreditScore(Long userId, double score) {
        // 信誉分直接设置，不是累加
        rankingCacheService.setScore("CREDIT", null, userId, score);
        log.debug("信誉分更新: userId={}, score={}", userId, score);
    }

    @Override
    public void updateRatingScore(Long userId, double rating) {
        // 评分直接设置
        rankingCacheService.setScore("RATING", null, userId, rating);
        log.debug("评分更新: userId={}, rating={}", userId, rating);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换排行类型到周期
     */
    private String convertTypeToPeriod(String type) {
        return switch (type) {
            case "weekly" -> "weekly";
            case "monthly" -> "monthly";
            default -> "daily";
        };
    }

    /**
     * 获取用户排名信息
     */
    private MyRankingVO.RankingInfo getUserRankInfo(Long userId, String rankType, String period) {
        Long rank = rankingCacheService.getUserRank(rankType, period, userId);
        BigDecimal score = rankingCacheService.getUserScore(rankType, period, userId);

        if (rank != null && score != null) {
            return MyRankingVO.RankingInfo.builder()
                .rank(rank.intValue())
                .score(score.toString())
                .inList(rank <= RANKING_LIMIT)
                .build();
        } else {
            return MyRankingVO.RankingInfo.builder()
                .rank(null)
                .score("0")
                .inList(false)
                .build();
        }
    }

    /**
     * 构建奖励VO
     */
    private RankingRewardVO buildRankingRewardVO(RankingRewardLog log) {
        RankingRewardVO.RankingRewardVOBuilder builder = RankingRewardVO.builder()
            .id(log.getId())
            .rankType(log.getRankType())
            .rankTypeName(getRankTypeName(log.getRankType()))
            .rankDate(log.getRankDate())
            .rankPosition(log.getRankPosition())
            .rewardPoints(log.getRewardPoints())
            .rewardCouponId(log.getRewardCouponId())
            .isClaimed(log.getIsClaimed() == 1);

        // TODO: 查询优惠券名称
        if (log.getRewardCouponId() != null) {
            builder.rewardCouponName("优惠券");
        }

        return builder.build();
    }

    /**
     * 获取排行类型名称
     */
    private String getRankTypeName(String rankType) {
        return switch (rankType) {
            case "ACTIVITY" -> "活跃度排行";
            case "TRADE" -> "交易排行";
            case "CREDIT" -> "信誉排行";
            case "RATING" -> "好评排行";
            default -> "未知排行";
        };
    }
}