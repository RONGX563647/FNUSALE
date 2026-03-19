package com.fnusale.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserRating;
import com.fnusale.common.vo.user.RankingUserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserRatingMapper;
import com.fnusale.user.service.RankingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 排行榜缓存服务实现
 * 基于 Redis ZSET 实现实时排行榜
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheServiceImpl implements RankingCacheService {

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final UserRatingMapper userRatingMapper;

    private static final String RANKING_KEY_PREFIX = "ranking:zset:";

    @Override
    public void incrementScore(String rankType, String period, Long userId, double score) {
        String key = buildKey(rankType, period);
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), score);
        log.debug("排行榜分数增加: key={}, userId={}, score={}", key, userId, score);
    }

    @Override
    public void setScore(String rankType, String period, Long userId, double score) {
        String key = buildKey(rankType, period);
        redisTemplate.opsForZSet().add(key, userId.toString(), score);
        log.debug("排行榜分数设置: key={}, userId={}, score={}", key, userId, score);
    }

    @Override
    public List<RankingUserVO> getTopN(String rankType, String period, int n) {
        String key = buildKey(rankType, period);

        // 使用 ZREVRANGE 获取分数从高到低的前N名
        Set<ZSetOperations.TypedTuple<String>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有用户ID
        List<Long> userIds = tuples.stream()
            .map(tuple -> Long.parseLong(tuple.getValue()))
            .collect(Collectors.toList());

        // 批量查询用户信息，避免 N+1 查询
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        // 批量查询评分信息（仅信誉榜和好评榜需要）
        Map<Long, UserRating> ratingMap = Collections.emptyMap();
        if ("CREDIT".equals(rankType) || "RATING".equals(rankType)) {
            LambdaQueryWrapper<UserRating> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(UserRating::getUserId, userIds);
            List<UserRating> ratings = userRatingMapper.selectList(wrapper);
            ratingMap = ratings.stream()
                .collect(Collectors.toMap(UserRating::getUserId, Function.identity()));
        }

        // 构建结果
        List<RankingUserVO> result = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long userId = Long.parseLong(tuple.getValue());
            Double score = tuple.getScore();

            RankingUserVO.RankingUserVOBuilder builder = RankingUserVO.builder()
                .rank(rank++)
                .userId(userId)
                .score(score != null ? BigDecimal.valueOf(score) : BigDecimal.ZERO);

            // 从 Map 获取用户信息
            User user = userMap.get(userId);
            if (user != null) {
                builder.username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .creditScore(user.getCreditScore());
            }

            // 对于信誉榜和好评榜，补充评分信息
            if ("CREDIT".equals(rankType) || "RATING".equals(rankType)) {
                UserRating rating = ratingMap.get(userId);
                if (rating != null) {
                    builder.rating(rating.getOverallRating());
                }
            }

            result.add(builder.build());
        }

        return result;
    }

    @Override
    public Long getUserRank(String rankType, String period, Long userId) {
        String key = buildKey(rankType, period);
        // ZREVRANK 返回的是从0开始的索引，需要+1转换为排名
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    @Override
    public BigDecimal getUserScore(String rankType, String period, Long userId) {
        String key = buildKey(rankType, period);
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        return score != null ? BigDecimal.valueOf(score) : null;
    }

    @Override
    public void removeUser(String rankType, String period, Long userId) {
        String key = buildKey(rankType, period);
        redisTemplate.opsForZSet().remove(key, userId.toString());
        log.debug("排行榜移除用户: key={}, userId={}", key, userId);
    }

    @Override
    public Long getSize(String rankType, String period) {
        String key = buildKey(rankType, period);
        Long size = redisTemplate.opsForZSet().zCard(key);
        return size != null ? size : 0L;
    }

    @Override
    public void deleteRanking(String rankType, String period) {
        String key = buildKey(rankType, period);
        redisTemplate.delete(key);
        log.info("排行榜已删除: key={}", key);
    }

    @Override
    public List<Object[]> getAllWithScores(String rankType, String period, int limit) {
        String key = buildKey(rankType, period);

        Set<ZSetOperations.TypedTuple<String>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long userId = Long.parseLong(tuple.getValue());
            Double score = tuple.getScore();
            result.add(new Object[]{userId, score != null ? score : 0.0});
        }

        return result;
    }

    /**
     * 构建 Redis Key
     * @param rankType 排行类型 (ACTIVITY/TRADE/CREDIT/RATING)
     * @param period 周期类型 (daily/weekly/monthly)，CREDIT和RATING传null
     * @return Redis Key
     */
    private String buildKey(String rankType, String period) {
        if (period == null || period.isEmpty()) {
            // 信誉榜和好评榜不分周期
            return RANKING_KEY_PREFIX + rankType.toLowerCase();
        }
        return RANKING_KEY_PREFIX + rankType.toLowerCase() + ":" + period;
    }
}