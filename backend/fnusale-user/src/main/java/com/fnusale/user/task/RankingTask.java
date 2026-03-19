package com.fnusale.user.task;

import com.fnusale.common.entity.RankingRecord;
import com.fnusale.common.entity.RankingRewardLog;
import com.fnusale.user.mapper.RankingRecordMapper;
import com.fnusale.user.mapper.RankingRewardLogMapper;
import com.fnusale.user.service.RankingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 排行榜定时任务
 * 负责持久化排行数据到MySQL，并重置周期排行榜
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingTask {

    private final RankingCacheService rankingCacheService;
    private final RankingRecordMapper rankingRecordMapper;
    private final RankingRewardLogMapper rankingRewardLogMapper;

    private static final int RANKING_LIMIT = 100;
    private static final int REWARD_TOP_N = 10;

    // 奖励积分配置
    private static final int[] REWARD_POINTS = {100, 80, 60, 50, 40, 30, 25, 20, 15, 10};

    /**
     * 每日 00:05 执行
     * 持久化日榜数据并重置
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void persistAndResetDailyRanking() {
        log.info("开始持久化日榜数据...");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 持久化活跃度日榜
        persistRanking("ACTIVITY", "daily", yesterday);

        // 持久化交易日榜
        persistRanking("TRADE", "daily", yesterday);

        log.info("日榜数据持久化完成");
    }

    /**
     * 每周一 00:10 执行
     * 持久化周榜数据并重置
     */
    @Scheduled(cron = "0 10 0 ? * MON")
    public void persistAndResetWeeklyRanking() {
        log.info("开始持久化周榜数据...");
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);

        // 持久化活跃度周榜
        persistRanking("ACTIVITY", "weekly", lastWeek);

        // 持久化交易周榜
        persistRanking("TRADE", "weekly", lastWeek);

        log.info("周榜数据持久化完成");
    }

    /**
     * 每月一日 00:15 执行
     * 持久化月榜数据并重置
     */
    @Scheduled(cron = "0 15 0 1 * ?")
    public void persistAndResetMonthlyRanking() {
        log.info("开始持久化月榜数据...");
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        // 持久化活跃度月榜
        persistRanking("ACTIVITY", "monthly", lastMonth);

        // 持久化交易月榜
        persistRanking("TRADE", "monthly", lastMonth);

        log.info("月榜数据持久化完成");
    }

    /**
     * 持久化排行榜数据
     */
    private void persistRanking(String rankType, String period, LocalDate rankDate) {
        try {
            // 从 ZSET 获取所有数据
            List<Object[]> data = rankingCacheService.getAllWithScores(rankType, period, RANKING_LIMIT);

            if (data.isEmpty()) {
                log.info("排行榜无数据: rankType={}, period={}", rankType, period);
                return;
            }

            // 写入数据库
            int rank = 1;
            for (Object[] item : data) {
                Long userId = (Long) item[0];
                Double score = (Double) item[1];

                // 插入排行记录
                RankingRecord record = RankingRecord.builder()
                    .rankType(rankType)
                    .rankDate(rankDate)
                    .userId(userId)
                    .rankPosition(rank)
                    .score(BigDecimal.valueOf(score))
                    .createTime(LocalDateTime.now())
                    .build();
                rankingRecordMapper.insert(record);

                // 为前N名生成奖励
                if (rank <= REWARD_TOP_N) {
                    createRewardLog(userId, rankType, rankDate, rank);
                }

                rank++;
            }

            // 删除 ZSET
            rankingCacheService.deleteRanking(rankType, period);

            log.info("排行榜持久化完成: rankType={}, period={}, count={}", rankType, period, data.size());

        } catch (Exception e) {
            log.error("排行榜持久化失败: rankType={}, period={}", rankType, period, e);
        }
    }

    /**
     * 创建奖励记录
     */
    private void createRewardLog(Long userId, String rankType, LocalDate rankDate, int rank) {
        int rewardPoints = 0;
        if (rank <= REWARD_POINTS.length) {
            rewardPoints = REWARD_POINTS[rank - 1];
        }

        RankingRewardLog rewardLog = RankingRewardLog.builder()
            .userId(userId)
            .rankType(rankType)
            .rankDate(rankDate)
            .rankPosition(rank)
            .rewardPoints(rewardPoints)
            .isClaimed(0)
            .createTime(LocalDateTime.now())
            .build();

        rankingRewardLogMapper.insert(rewardLog);
        log.debug("创建奖励记录: userId={}, rankType={}, rank={}, points={}",
            userId, rankType, rank, rewardPoints);
    }
}