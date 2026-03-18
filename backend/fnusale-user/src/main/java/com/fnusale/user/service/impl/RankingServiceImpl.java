package com.fnusale.user.service.impl;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.RankingRecord;
import com.fnusale.common.entity.RankingRewardLog;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserRating;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.MyRankingVO;
import com.fnusale.common.vo.user.RankingRewardVO;
import com.fnusale.common.vo.user.RankingUserVO;
import com.fnusale.user.mapper.RankingRecordMapper;
import com.fnusale.user.mapper.RankingRewardLogMapper;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserRatingMapper;
import com.fnusale.user.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingRecordMapper rankingRecordMapper;
    private final RankingRewardLogMapper rankingRewardLogMapper;
    private final UserMapper userMapper;
    private final UserRatingMapper userRatingMapper;
    private final StringRedisTemplate redisTemplate;

    private static final int RANKING_LIMIT = 100;
    private static final String RANKING_CACHE_PREFIX = "ranking:";
    private static final long CACHE_EXPIRE_HOURS = 1;

    @Override
    public List<RankingUserVO> getActivityRanking(String type, String date) {
        LocalDate rankDate = parseRankDate(type, date);
        String cacheKey = RANKING_CACHE_PREFIX + "activity:" + type + ":" + rankDate;

        // 尝试从缓存获取
        String cachedRank = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRank != null) {
            return parseCachedRanking(cachedRank, "ACTIVITY");
        }

        // 从数据库获取
        List<RankingRecord> records = rankingRecordMapper.selectByTypeAndDate("ACTIVITY", rankDate, RANKING_LIMIT);
        List<RankingUserVO> result = buildRankingUserVOList(records, "ACTIVITY");

        // 缓存结果
        cacheRankingResult(cacheKey, result);

        return result;
    }

    @Override
    public List<RankingUserVO> getTradeRanking(String type, String date) {
        LocalDate rankDate = parseRankDate(type, date);
        String cacheKey = RANKING_CACHE_PREFIX + "trade:" + type + ":" + rankDate;

        String cachedRank = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRank != null) {
            return parseCachedRanking(cachedRank, "TRADE");
        }

        List<RankingRecord> records = rankingRecordMapper.selectByTypeAndDate("TRADE", rankDate, RANKING_LIMIT);
        List<RankingUserVO> result = buildRankingUserVOList(records, "TRADE");

        cacheRankingResult(cacheKey, result);

        return result;
    }

    @Override
    public List<RankingUserVO> getCreditRanking() {
        String cacheKey = RANKING_CACHE_PREFIX + "credit:current";

        String cachedRank = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRank != null) {
            return parseCachedRanking(cachedRank, "CREDIT");
        }

        // 信誉排行从用户表直接查询
        List<RankingRecord> records = rankingRecordMapper.selectByTypeAndDate("CREDIT", LocalDate.now(), RANKING_LIMIT);
        List<RankingUserVO> result = buildRankingUserVOList(records, "CREDIT");

        cacheRankingResult(cacheKey, result);

        return result;
    }

    @Override
    public List<RankingUserVO> getRatingRanking() {
        String cacheKey = RANKING_CACHE_PREFIX + "rating:current";

        String cachedRank = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRank != null) {
            return parseCachedRanking(cachedRank, "RATING");
        }

        List<RankingRecord> records = rankingRecordMapper.selectByTypeAndDate("RATING", LocalDate.now(), RANKING_LIMIT);
        List<RankingUserVO> result = buildRankingUserVOList(records, "RATING");

        cacheRankingResult(cacheKey, result);

        return result;
    }

    @Override
    public MyRankingVO getMyRanking(Long userId) {
        LocalDate today = LocalDate.now();

        return MyRankingVO.builder()
                .activity(getUserRankInfo(userId, "ACTIVITY", today))
                .trade(getUserRankInfo(userId, "TRADE", today))
                .credit(getUserRankInfo(userId, "CREDIT", today))
                .rating(getUserRankInfo(userId, "RATING", today))
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

    /**
     * 解析排行日期
     */
    private LocalDate parseRankDate(String type, String date) {
        if (date != null && !date.isEmpty()) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        LocalDate today = LocalDate.now();
        return switch (type) {
            case "weekly" -> today.minusWeeks(1);
            case "monthly" -> today.minusMonths(1);
            default -> today;
        };
    }

    /**
     * 获取用户排名信息
     */
    private MyRankingVO.RankingInfo getUserRankInfo(Long userId, String rankType, LocalDate date) {
        RankingRecord record = rankingRecordMapper.selectByTypeDateAndUser(rankType, date, userId);

        if (record != null) {
            return MyRankingVO.RankingInfo.builder()
                    .rank(record.getRankPosition())
                    .score(record.getScore() != null ? record.getScore().toString() : "0")
                    .inList(record.getRankPosition() <= RANKING_LIMIT)
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
     * 构建排行用户VO列表
     */
    private List<RankingUserVO> buildRankingUserVOList(List<RankingRecord> records, String rankType) {
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        return records.stream()
                .map(record -> {
                    RankingUserVO.RankingUserVOBuilder builder = RankingUserVO.builder()
                            .rank(record.getRankPosition())
                            .userId(record.getUserId())
                            .score(record.getScore());

                    // 查询用户信息
                    User user = userMapper.selectById(record.getUserId());
                    if (user != null) {
                        builder.username(user.getUsername())
                                .avatarUrl(user.getAvatarUrl())
                                .creditScore(user.getCreditScore());
                    }

                    // 查询评分信息
                    if ("RATING".equals(rankType) || "CREDIT".equals(rankType)) {
                        UserRating rating = userRatingMapper.selectByUserId(record.getUserId());
                        if (rating != null) {
                            builder.rating(rating.getOverallRating());
                        }
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 缓存排行结果
     */
    private void cacheRankingResult(String key, List<RankingUserVO> result) {
        try {
            StringBuilder sb = new StringBuilder();
            for (RankingUserVO vo : result) {
                sb.append(vo.getUserId()).append(",")
                        .append(vo.getRank()).append(",")
                        .append(vo.getScore()).append(";");
            }
            redisTemplate.opsForValue().set(key, sb.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存排行数据失败", e);
        }
    }

    /**
     * 解析缓存的排行数据
     */
    private List<RankingUserVO> parseCachedRanking(String cached, String rankType) {
        List<RankingUserVO> result = new ArrayList<>();
        String[] items = cached.split(";");
        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split(",");
            if (parts.length >= 3) {
                RankingUserVO.RankingUserVOBuilder builder = RankingUserVO.builder()
                        .userId(Long.parseLong(parts[0]))
                        .rank(Integer.parseInt(parts[1]))
                        .score(new BigDecimal(parts[2]));

                // 补充用户信息
                User user = userMapper.selectById(Long.parseLong(parts[0]));
                if (user != null) {
                    builder.username(user.getUsername())
                            .avatarUrl(user.getAvatarUrl())
                            .creditScore(user.getCreditScore());
                }

                result.add(builder.build());
            }
        }
        return result;
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