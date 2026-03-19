package com.fnusale.user.service;

import com.fnusale.common.vo.user.RankingUserVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 排行榜缓存服务
 * 基于 Redis ZSET 实现实时排行榜
 */
public interface RankingCacheService {

    /**
     * 增加分数
     * @param rankType 排行类型 (ACTIVITY/TRADE/CREDIT/RATING)
     * @param period 周期类型 (daily/weekly/monthly)，CREDIT和RATING传null
     * @param userId 用户ID
     * @param score 增加的分数
     */
    void incrementScore(String rankType, String period, Long userId, double score);

    /**
     * 设置分数（覆盖原有值）
     * @param rankType 排行类型
     * @param period 周期类型
     * @param userId 用户ID
     * @param score 分数值
     */
    void setScore(String rankType, String period, Long userId, double score);

    /**
     * 获取前N名
     * @param rankType 排行类型
     * @param period 周期类型
     * @param n 数量
     * @return 排行列表
     */
    List<RankingUserVO> getTopN(String rankType, String period, int n);

    /**
     * 获取用户排名（从1开始）
     * @param rankType 排行类型
     * @param period 周期类型
     * @param userId 用户ID
     * @return 排名，不存在返回null
     */
    Long getUserRank(String rankType, String period, Long userId);

    /**
     * 获取用户分数
     * @param rankType 排行类型
     * @param period 周期类型
     * @param userId 用户ID
     * @return 分数，不存在返回null
     */
    BigDecimal getUserScore(String rankType, String period, Long userId);

    /**
     * 移除用户
     * @param rankType 排行类型
     * @param period 周期类型
     * @param userId 用户ID
     */
    void removeUser(String rankType, String period, Long userId);

    /**
     * 获取ZSET大小
     * @param rankType 排行类型
     * @param period 周期类型
     * @return 成员数量
     */
    Long getSize(String rankType, String period);

    /**
     * 删除整个排行榜
     * @param rankType 排行类型
     * @param period 周期类型
     */
    void deleteRanking(String rankType, String period);

    /**
     * 获取排行榜所有数据（用于持久化）
     * @param rankType 排行类型
     * @param period 周期类型
     * @param limit 最大数量
     * @return 排行数据，每项为 [userId, score] 数组
     */
    List<Object[]> getAllWithScores(String rankType, String period, int limit);
}