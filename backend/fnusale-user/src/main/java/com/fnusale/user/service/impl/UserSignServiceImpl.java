package com.fnusale.user.service.impl;

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
import com.fnusale.user.service.UserSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignServiceImpl implements UserSignService {

    private final UserSignRecordMapper userSignRecordMapper;
    private final UserPointsMapper userPointsMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignResultVO sign(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        UserSignRecord existRecord = userSignRecordMapper.selectByUserIdAndDate(userId, today);
        if (existRecord != null) {
            throw new BusinessException("今日已签到");
        }

        // 计算连续签到天数
        int continuousDays = calculateContinuousDays(userId);

        // 计算奖励积分
        int rewardPoints = calculateRewardPoints(continuousDays);

        // 创建签到记录
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

        // 获取当前积分
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);

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
    }

    @Override
    public SignStatusVO getSignStatus(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        UserSignRecord todayRecord = userSignRecordMapper.selectByUserIdAndDate(userId, today);
        boolean hasSignedToday = todayRecord != null;

        // 获取连续签到天数
        int continuousDays = 0;
        int todayRewardPoints = 0;
        if (hasSignedToday) {
            continuousDays = todayRecord.getContinuousDays();
            todayRewardPoints = todayRecord.getRewardPoints();
        } else {
            continuousDays = calculateContinuousDays(userId);
        }

        // 获取总签到天数
        int totalSignDays = userSignRecordMapper.countByUserId(userId);

        return SignStatusVO.builder()
                .hasSigned(hasSignedToday)
                .continuousDays(continuousDays)
                .totalDays(totalSignDays)
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
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();

        List<UserSignRecord> records = userSignRecordMapper.selectByUserIdAndMonth(userId, year, monthValue);

        return records.stream()
                .map(record -> record.getSignDate().toString())
                .collect(Collectors.toList());
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

        // 检查该日期是否已签到
        UserSignRecord existRecord = userSignRecordMapper.selectByUserIdAndDate(userId, repairDate);
        if (existRecord != null) {
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

        // 获取剩余积分
        UserPoints updatedPoints = userPointsMapper.selectByUserId(userId);

        SignResultVO result = SignResultVO.builder()
                .success(true)
                .continuousDays(continuousDays)
                .message("补签成功，消耗" + UserConstants.REPAIR_SIGN_COST + "积分")
                .build();

        log.info("用户补签成功, userId: {}, repairDate: {}, costPoints: {}", userId, repairDate, UserConstants.REPAIR_SIGN_COST);
        return result;
    }

    /**
     * 计算连续签到天数
     */
    private int calculateContinuousDays(Long userId) {
        LocalDate today = LocalDate.now();
        UserSignRecord lastRecord = userSignRecordMapper.selectLatestByUserId(userId);

        if (lastRecord == null) {
            return 1;
        }

        // 如果最近一次签到是昨天，则连续天数+1
        if (lastRecord.getSignDate().equals(today.minusDays(1))) {
            return lastRecord.getContinuousDays() + 1;
        }

        // 否则重新计算
        return 1;
    }

    /**
     * 计算补签日期的连续签到天数
     */
    private int calculateContinuousDaysForRepair(Long userId, LocalDate repairDate) {
        // 查询补签日期前一天的签到记录
        UserSignRecord prevRecord = userSignRecordMapper.selectByUserIdAndDate(userId, repairDate.minusDays(1));
        if (prevRecord != null) {
            return prevRecord.getContinuousDays() + 1;
        }
        return 1;
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
        // 获取当前积分
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        int beforePoints = userPoints != null ? userPoints.getAvailablePoints() : 0;

        // 增加积分
        userPointsMapper.addPoints(userId, points);

        // 记录日志
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
}