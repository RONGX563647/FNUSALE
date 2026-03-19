package com.fnusale.user.service.impl;

import com.fnusale.common.cache.RedisService;
import com.fnusale.common.constant.RedisKeyConstants;
import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.SignDTO;
import com.fnusale.common.entity.PointsLog;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.entity.UserSignRecord;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.user.SignRecordVO;
import com.fnusale.common.vo.user.SignResultVO;
import com.fnusale.common.vo.user.SignStatusVO;
import com.fnusale.user.mapper.PointsLogMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.mapper.UserSignRecordMapper;
import com.fnusale.user.service.RankingService;
import com.fnusale.user.service.UserSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 签到服务实现
 * 使用 Redis Bitmap 优化签到存储和查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignServiceImpl implements UserSignService {

    private final UserSignRecordMapper userSignRecordMapper;
    private final UserPointsMapper userPointsMapper;
    private final PointsLogMapper pointsLogMapper;
    private final RankingService rankingService;
    private final RedisService redisService;
    private final RedissonClient redissonClient;

    /**
     * 签到活跃度奖励分数
     */
    private static final double SIGN_ACTIVITY_SCORE = 10.0;

    /**
     * Bitmap Key 过期时间（3个月）
     */
    private static final Duration BITMAP_EXPIRE = Duration.ofDays(90);

    /**
     * 分布式锁等待时间和持有时间
     */
    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignResultVO sign(Long userId) {
        LocalDate today = LocalDate.now();
        String yearMonth = formatYearMonth(today);
        String bitmapKey = RedisKeyConstants.buildSignBitmapKey(userId, yearMonth);
        int dayOffset = today.getDayOfMonth() - 1;

        // 使用分布式锁防止并发签到
        String lockKey = "sign:" + userId + ":" + yearMonth + ":" + dayOffset;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            try {
                // 双重检查今日是否已签到
                Boolean hasSigned = redisService.getBit(bitmapKey, dayOffset);
                if (Boolean.TRUE.equals(hasSigned)) {
                    throw new BusinessException("今日已签到");
                }

                // 计算连续签到天数
                int continuousDays = calculateContinuousDays(userId, today);

                // 计算奖励积分
                int rewardPoints = calculateRewardPoints(continuousDays);

                // 设置签到位 (Bitmap)
                redisService.setBit(bitmapKey, dayOffset, true);
                redisService.expire(bitmapKey, BITMAP_EXPIRE);

                // 更新全局签到统计 Bitmap
                updateSignStat(today);

                // 更新签到排行榜
                updateSignRank(userId, today, rewardPoints);

                // 缓存连续签到天数
                cacheContinuousDays(userId, continuousDays);

                // 创建签到记录（持久化到数据库）
                UserSignRecord record = UserSignRecord.builder()
                        .userId(userId)
                        .signDate(today)
                        .signTime(LocalDateTime.now())
                        .continuousDays(continuousDays)
                        .rewardPoints(rewardPoints)
                        .isRepair(0)
                        .createTime(LocalDateTime.now())
                        .build();

                userSignRecordMapper.insert(record);

                // 增加积分
                addPointsWithLog(userId, rewardPoints, "SIGN_REWARD", "每日签到奖励");

                // 更新活跃度排行榜分数
                rankingService.incrementActivityScore(userId, SIGN_ACTIVITY_SCORE);

                SignResultVO result = SignResultVO.builder()
                        .success(true)
                        .continuousDays(continuousDays)
                        .rewardPoints(rewardPoints)
                        .hasContinuousReward(continuousDays >= 7)
                        .continuousRewardPoints(continuousDays >= 7 ? rewardPoints - UserConstants.SIGN_BASE_POINTS : 0)
                        .message("签到成功，获得" + rewardPoints + "积分")
                        .build();

                log.info("用户签到成功, userId: {}, continuousDays: {}, rewardPoints: {}", userId, continuousDays, rewardPoints);
                return result;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("请求被中断，请重试");
        }
    }

    @Override
    public SignStatusVO getSignStatus(Long userId) {
        LocalDate today = LocalDate.now();
        String yearMonth = formatYearMonth(today);
        String bitmapKey = RedisKeyConstants.buildSignBitmapKey(userId, yearMonth);
        int dayOffset = today.getDayOfMonth() - 1;

        // 检查今日是否已签到 (使用 Bitmap)
        Boolean hasSignedToday = redisService.getBit(bitmapKey, dayOffset);

        // 获取连续签到天数
        int continuousDays = 0;
        int todayRewardPoints = 0;

        if (Boolean.TRUE.equals(hasSignedToday)) {
            // 今日已签到，从缓存获取连续天数
            continuousDays = getCachedContinuousDays(userId);
            if (continuousDays == 0) {
                continuousDays = calculateContinuousDaysFromBitmap(userId, today);
            }
            todayRewardPoints = calculateRewardPoints(continuousDays);
        } else {
            // 今日未签到，计算截止昨天的连续天数
            continuousDays = calculateContinuousDaysFromBitmap(userId, today.minusDays(1));
        }

        // 获取月签到天数
        Long monthSignCount = redisService.bitCount(bitmapKey);

        return SignStatusVO.builder()
                .hasSigned(Boolean.TRUE.equals(hasSignedToday))
                .continuousDays(continuousDays)
                .totalDays(monthSignCount != null ? monthSignCount.intValue() : 0)
                .todayReward(todayRewardPoints)
                .build();
    }

    @Override
    public SignStatusVO getSignStatistics(Long userId) {
        return getSignStatus(userId);
    }

    @Override
    public PageResult<SignRecordVO> getSignRecords(Long userId, Integer pageNum, Integer pageSize) {
        // 获取最近签到记录
        List<UserSignRecord> records = userSignRecordMapper.selectByUserIdAndMonth(userId,
                LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        List<SignRecordVO> voList = records.stream()
                .map(this::buildSignRecordVO)
                .collect(Collectors.toList());

        return new PageResult<>(pageNum, pageSize, records.size(), voList);
    }

    @Override
    public List<String> getSignCalendar(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        String bitmapKey = RedisKeyConstants.buildSignBitmapKey(userId, formatYearMonth(yearMonth));

        List<String> signedDates = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();

        // 使用 Bitmap 获取签到状态
        for (int day = 1; day <= daysInMonth; day++) {
            Boolean signed = redisService.getBit(bitmapKey, day - 1);
            if (Boolean.TRUE.equals(signed)) {
                signedDates.add(yearMonth.atDay(day).toString());
            }
        }

        return signedDates;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignResultVO repairSign(Long userId, SignDTO dto) {
        LocalDate repairDate = LocalDate.parse(dto.getSignDate());
        LocalDate today = LocalDate.now();

        // 检查补签日期是否合法
        long daysBetween = ChronoUnit.DAYS.between(repairDate, today);
        if (daysBetween <= 0) {
            throw new BusinessException("只能补签过去的日期");
        }
        if (daysBetween > UserConstants.REPAIR_SIGN_MAX_DAYS) {
            throw new BusinessException("只能补签" + UserConstants.REPAIR_SIGN_MAX_DAYS + "天内的签到");
        }

        // 检查该日期是否已签到 (使用 Bitmap)
        String yearMonth = formatYearMonth(repairDate);
        String bitmapKey = RedisKeyConstants.buildSignBitmapKey(userId, yearMonth);
        int dayOffset = repairDate.getDayOfMonth() - 1;

        Boolean hasSigned = redisService.getBit(bitmapKey, dayOffset);
        if (Boolean.TRUE.equals(hasSigned)) {
            throw new BusinessException("该日期已签到，无需补签");
        }

        // 检查本月补签次数
        int repairCount = userSignRecordMapper.countRepairByUserIdAndMonth(userId,
                repairDate.getYear(), repairDate.getMonthValue());
        if (repairCount >= UserConstants.REPAIR_SIGN_MAX_MONTHLY) {
            throw new BusinessException("本月补签次数已达上限（最多" + UserConstants.REPAIR_SIGN_MAX_MONTHLY + "次）");
        }

        // 检查积分是否足够
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null || userPoints.getAvailablePoints() < UserConstants.REPAIR_SIGN_COST) {
            throw new BusinessException("积分不足，需要" + UserConstants.REPAIR_SIGN_COST + "积分");
        }

        // 计算连续签到天数（补签日期）
        int continuousDays = calculateContinuousDaysForRepair(userId, repairDate);

        // 扣减积分
        deductPointsWithLog(userId, UserConstants.REPAIR_SIGN_COST, "REPAIR_COST", "补签扣减");

        // 设置补签位 (Bitmap)
        redisService.setBit(bitmapKey, dayOffset, true);

        // 更新全局签到统计
        updateSignStat(repairDate);

        // 更新活跃度排行榜分数（补签也给活跃度）
        rankingService.incrementActivityScore(userId, SIGN_ACTIVITY_SCORE);

        // 创建补签记录
        UserSignRecord record = UserSignRecord.builder()
                .userId(userId)
                .signDate(repairDate)
                .signTime(LocalDateTime.now())
                .continuousDays(continuousDays)
                .rewardPoints(0)
                .isRepair(1)
                .createTime(LocalDateTime.now())
                .build();

        userSignRecordMapper.insert(record);

        SignResultVO result = SignResultVO.builder()
                .success(true)
                .continuousDays(continuousDays)
                .message("补签成功，消耗" + UserConstants.REPAIR_SIGN_COST + "积分")
                .build();

        log.info("用户补签成功, userId: {}, repairDate: {}, costPoints: {}", userId, repairDate, UserConstants.REPAIR_SIGN_COST);
        return result;
    }

    // ==================== 签到统计 ====================

    /**
     * 获取日签到人数统计
     */
    public Long getDailySignCount(LocalDate date) {
        return userSignRecordMapper.countByDate(date);
    }

    /**
     * 获取月签到人数统计
     */
    public Long getMonthlySignCount(String yearMonth) {
        String statKey = RedisKeyConstants.buildSignStatKey(yearMonth);
        Long count = redisService.bitCount(statKey);
        return count != null ? count : 0L;
    }

    // ==================== 签到排行榜 ====================

    /**
     * 获取月签到排行榜
     */
    public List<SignRankVO> getSignRankList(String yearMonth, int limit) {
        String rankKey = RedisKeyConstants.buildSignRankKey(yearMonth);
        Set<ZSetOperations.TypedTuple<String>> ranks = redisService.zRevRangeWithScores(rankKey, 0, limit - 1);

        List<SignRankVO> result = new ArrayList<>();
        int rank = 1;
        if (ranks != null) {
            for (ZSetOperations.TypedTuple<String> tuple : ranks) {
                SignRankVO vo = new SignRankVO();
                vo.setRank(rank++);
                vo.setUserId(Long.parseLong(tuple.getValue()));
                vo.setTotalPoints(tuple.getScore() != null ? tuple.getScore().intValue() : 0);
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 获取用户签到排名
     */
    public Long getUserSignRank(Long userId, String yearMonth) {
        String rankKey = RedisKeyConstants.buildSignRankKey(yearMonth);
        Long rank = redisService.zRevRank(rankKey, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    // ==================== 私有方法 ====================

    /**
     * 计算连续签到天数
     */
    private int calculateContinuousDays(Long userId, LocalDate today) {
        // 先检查昨天是否签到
        LocalDate yesterday = today.minusDays(1);
        String yesterdayMonth = formatYearMonth(yesterday);
        String yesterdayKey = RedisKeyConstants.buildSignBitmapKey(userId, yesterdayMonth);
        int yesterdayOffset = yesterday.getDayOfMonth() - 1;

        Boolean signedYesterday = redisService.getBit(yesterdayKey, yesterdayOffset);

        if (Boolean.TRUE.equals(signedYesterday)) {
            // 昨天签到了，获取缓存的连续天数 +1
            int cachedDays = getCachedContinuousDays(userId);
            return cachedDays > 0 ? cachedDays + 1 : 2;
        }

        return 1;
    }

    /**
     * 从 Bitmap 计算连续签到天数
     */
    private int calculateContinuousDaysFromBitmap(Long userId, LocalDate endDate) {
        int continuousDays = 0;
        LocalDate checkDate = endDate;

        // 最多检查 30 天
        for (int i = 0; i < 30; i++) {
            String monthKey = formatYearMonth(checkDate);
            String bitmapKey = RedisKeyConstants.buildSignBitmapKey(userId, monthKey);
            int offset = checkDate.getDayOfMonth() - 1;

            Boolean signed = redisService.getBit(bitmapKey, offset);

            if (Boolean.TRUE.equals(signed)) {
                continuousDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return continuousDays;
    }

    /**
     * 计算补签日期的连续签到天数
     */
    private int calculateContinuousDaysForRepair(Long userId, LocalDate repairDate) {
        LocalDate prevDate = repairDate.minusDays(1);
        String prevMonth = formatYearMonth(prevDate);
        String prevKey = RedisKeyConstants.buildSignBitmapKey(userId, prevMonth);
        int prevOffset = prevDate.getDayOfMonth() - 1;

        Boolean signedPrev = redisService.getBit(prevKey, prevOffset);
        if (Boolean.TRUE.equals(signedPrev)) {
            int cachedDays = getCachedContinuousDays(userId);
            return cachedDays > 0 ? cachedDays + 1 : 2;
        }
        return 1;
    }

    /**
     * 更新全局签到统计
     */
    private void updateSignStat(LocalDate date) {
        String statKey = RedisKeyConstants.buildSignStatKey(formatYearMonth(date));
        int offset = date.getDayOfMonth() - 1;
        redisService.setBit(statKey, offset, true);
        redisService.expire(statKey, BITMAP_EXPIRE);
    }

    /**
     * 更新签到排行榜
     */
    private void updateSignRank(Long userId, LocalDate date, int points) {
        String rankKey = RedisKeyConstants.buildSignRankKey(formatYearMonth(date));
        if (points > 0) {
            redisService.zIncrBy(rankKey, userId.toString(), points);
        }
        redisService.expire(rankKey, BITMAP_EXPIRE);
    }

    /**
     * 缓存连续签到天数
     */
    private void cacheContinuousDays(Long userId, int days) {
        String key = RedisKeyConstants.buildSignContinuousKey(userId);
        redisService.hSet(key, "days", String.valueOf(days));
        redisService.hSet(key, "updateTime", LocalDate.now().toString());
        redisService.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * 获取缓存的连续签到天数
     */
    private int getCachedContinuousDays(Long userId) {
        String key = RedisKeyConstants.buildSignContinuousKey(userId);
        String daysStr = redisService.hGet(key, "days");
        String updateTimeStr = redisService.hGet(key, "updateTime");

        if (daysStr != null && updateTimeStr != null) {
            LocalDate updateTime = LocalDate.parse(updateTimeStr);
            // 如果缓存是昨天的数据，说明今天还没签到
            if (updateTime.equals(LocalDate.now().minusDays(1))) {
                return Integer.parseInt(daysStr);
            }
        }
        return 0;
    }

    /**
     * 计算奖励积分
     */
    private int calculateRewardPoints(int continuousDays) {
        if (continuousDays >= 30) {
            return UserConstants.SIGN_30_DAYS_POINTS;
        } else if (continuousDays >= 14) {
            return UserConstants.SIGN_14_DAYS_POINTS;
        } else if (continuousDays >= 7) {
            return UserConstants.SIGN_7_DAYS_POINTS;
        } else {
            return UserConstants.SIGN_BASE_POINTS;
        }
    }

    /**
     * 增加积分并记录日志
     */
    private void addPointsWithLog(Long userId, Integer points, String changeType, String remark) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        int beforePoints = userPoints != null ? userPoints.getAvailablePoints() : 0;

        userPointsMapper.addPoints(userId, points);

        PointsLog pointsLog = PointsLog.builder()
                .userId(userId)
                .changeType(changeType)
                .changeAmount(points)
                .beforePoints(beforePoints)
                .afterPoints(beforePoints + points)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        pointsLogMapper.insert(pointsLog);
    }

    /**
     * 扣减积分并记录日志
     */
    private void deductPointsWithLog(Long userId, Integer points, String changeType, String remark) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        int beforePoints = userPoints != null ? userPoints.getAvailablePoints() : 0;

        userPointsMapper.deductPoints(userId, points);

        PointsLog pointsLog = PointsLog.builder()
                .userId(userId)
                .changeType(changeType)
                .changeAmount(-points)
                .beforePoints(beforePoints)
                .afterPoints(beforePoints - points)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        pointsLogMapper.insert(pointsLog);
    }

    /**
     * 构建签到记录VO
     */
    private SignRecordVO buildSignRecordVO(UserSignRecord record) {
        return SignRecordVO.builder()
                .id(record.getId())
                .signDate(record.getSignDate())
                .signTime(record.getSignTime() != null ? record.getSignTime().toString() : null)
                .continuousDays(record.getContinuousDays())
                .rewardPoints(record.getRewardPoints())
                .isRepair(record.getIsRepair() == 1)
                .build();
    }

    /**
     * 格式化年月
     */
    private String formatYearMonth(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * 格式化年月
     */
    private String formatYearMonth(YearMonth yearMonth) {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * 签到排行 VO
     */
    public static class SignRankVO {
        private int rank;
        private Long userId;
        private int totalPoints;

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(int totalPoints) {
            this.totalPoints = totalPoints;
        }
    }
}