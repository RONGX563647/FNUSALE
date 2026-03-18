package com.fnusale.user.service.impl;

import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.SignDTO;
import com.fnusale.common.entity.PointsLog;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.entity.UserSignRecord;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.SignRecordVO;
import com.fnusale.common.vo.user.SignResultVO;
import com.fnusale.common.vo.user.SignStatusVO;
import com.fnusale.common.common.PageResult;
import com.fnusale.user.mapper.PointsLogMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.mapper.UserSignRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 签到服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserSignServiceImplTest {

    @Mock
    private UserSignRecordMapper userSignRecordMapper;

    @Mock
    private UserPointsMapper userPointsMapper;

    @Mock
    private PointsLogMapper pointsLogMapper;

    @InjectMocks
    private UserSignServiceImpl userSignService;

    private final Long userId = 1L;
    private UserPoints testUserPoints;
    private UserSignRecord testSignRecord;

    @BeforeEach
    void setUp() {
        testUserPoints = new UserPoints();
        testUserPoints.setUserId(userId);
        testUserPoints.setTotalPoints(100);
        testUserPoints.setAvailablePoints(50);
        testUserPoints.setUsedPoints(50);

        testSignRecord = new UserSignRecord();
        testSignRecord.setId(1L);
        testSignRecord.setUserId(userId);
        testSignRecord.setSignDate(LocalDate.now());
        testSignRecord.setSignTime(LocalDateTime.now());
        testSignRecord.setContinuousDays(1);
        testSignRecord.setRewardPoints(1);
        testSignRecord.setIsRepair(0);
    }

    @Nested
    @DisplayName("每日签到测试")
    class SignTests {

        @Test
        @DisplayName("首次签到_成功")
        void sign_firstTime_success() {
            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(null);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertEquals(1, result.getContinuousDays());
            assertEquals(UserConstants.SIGN_BASE_POINTS, result.getRewardPoints());
        }

        @Test
        @DisplayName("连续签到第二天_成功")
        void sign_secondDay_success() {
            UserSignRecord yesterdayRecord = new UserSignRecord();
            yesterdayRecord.setSignDate(LocalDate.now().minusDays(1));
            yesterdayRecord.setContinuousDays(1);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(yesterdayRecord);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertNotNull(result);
            assertEquals(2, result.getContinuousDays());
        }

        @Test
        @DisplayName("连续签到第7天_获得额外奖励")
        void sign_seventhDay_extraReward() {
            UserSignRecord yesterdayRecord = new UserSignRecord();
            yesterdayRecord.setSignDate(LocalDate.now().minusDays(1));
            yesterdayRecord.setContinuousDays(6);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(yesterdayRecord);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertNotNull(result);
            assertEquals(7, result.getContinuousDays());
            assertEquals(UserConstants.SIGN_7_DAYS_POINTS, result.getRewardPoints());
            assertTrue(result.getHasContinuousReward());
        }

        @Test
        @DisplayName("连续签到第14天_获得更高奖励")
        void sign_fourteenthDay_higherReward() {
            UserSignRecord yesterdayRecord = new UserSignRecord();
            yesterdayRecord.setSignDate(LocalDate.now().minusDays(1));
            yesterdayRecord.setContinuousDays(13);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(yesterdayRecord);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertEquals(14, result.getContinuousDays());
            assertEquals(UserConstants.SIGN_14_DAYS_POINTS, result.getRewardPoints());
        }

        @Test
        @DisplayName("连续签到第30天_获得最高奖励")
        void sign_thirtiethDay_maxReward() {
            UserSignRecord yesterdayRecord = new UserSignRecord();
            yesterdayRecord.setSignDate(LocalDate.now().minusDays(1));
            yesterdayRecord.setContinuousDays(29);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(yesterdayRecord);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertEquals(30, result.getContinuousDays());
            assertEquals(UserConstants.SIGN_30_DAYS_POINTS, result.getRewardPoints());
        }

        @Test
        @DisplayName("今日已签到_抛出异常")
        void sign_alreadySigned_throwsException() {
            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(testSignRecord);

            assertThrows(BusinessException.class, () -> userSignService.sign(userId));
        }

        @Test
        @DisplayName("断签后重新签到_连续天数重置")
        void sign_afterBreak_resetContinuous() {
            // 昨天没签到，前天签到
            UserSignRecord twoDaysAgoRecord = new UserSignRecord();
            twoDaysAgoRecord.setSignDate(LocalDate.now().minusDays(2));
            twoDaysAgoRecord.setContinuousDays(5);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(twoDaysAgoRecord);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            SignResultVO result = userSignService.sign(userId);

            assertEquals(1, result.getContinuousDays()); // 重新开始计数
        }
    }

    @Nested
    @DisplayName("获取签到状态测试")
    class GetSignStatusTests {

        @Test
        @DisplayName("今日已签到_返回正确状态")
        void getSignStatus_signedToday_success() {
            testSignRecord.setContinuousDays(5);
            testSignRecord.setRewardPoints(1);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(testSignRecord);
            when(userSignRecordMapper.countByUserId(userId)).thenReturn(10);

            SignStatusVO result = userSignService.getSignStatus(userId);

            assertNotNull(result);
            assertTrue(result.getHasSigned());
            assertEquals(5, result.getContinuousDays());
            assertEquals(10, result.getTotalDays());
        }

        @Test
        @DisplayName("今日未签到_返回正确状态")
        void getSignStatus_notSignedToday_success() {
            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(null);
            when(userSignRecordMapper.countByUserId(userId)).thenReturn(5);

            SignStatusVO result = userSignService.getSignStatus(userId);

            assertNotNull(result);
            assertFalse(result.getHasSigned());
            assertEquals(0, result.getTodayReward());
        }
    }

    @Nested
    @DisplayName("获取签到记录测试")
    class GetSignRecordsTests {

        @Test
        @DisplayName("获取签到记录_成功")
        void getSignRecords_success() {
            when(userSignRecordMapper.selectByUserIdAndMonth(eq(userId), anyInt(), anyInt()))
                    .thenReturn(List.of(testSignRecord));

            PageResult<SignRecordVO> result = userSignService.getSignRecords(userId, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getList().size());
        }
    }

    @Nested
    @DisplayName("获取签到日历测试")
    class GetSignCalendarTests {

        @Test
        @DisplayName("获取月签到日历_成功")
        void getSignCalendar_success() {
            when(userSignRecordMapper.selectByUserIdAndMonth(eq(userId), eq(2024), eq(3)))
                    .thenReturn(List.of(testSignRecord));

            List<String> result = userSignService.getSignCalendar(userId, "2024-03");

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("补签测试")
    class RepairSignTests {

        @Test
        @DisplayName("补签_成功")
        void repairSign_success() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().minusDays(1).toString());

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.countRepairByUserIdAndMonth(eq(userId), anyInt(), anyInt())).thenReturn(0);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);
            when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);

            SignResultVO result = userSignService.repairSign(userId, dto);

            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertTrue(result.getMessage().contains("消耗"));
        }

        @Test
        @DisplayName("补签未来日期_抛出异常")
        void repairSign_futureDate_throwsException() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().plusDays(1).toString());

            assertThrows(BusinessException.class, () -> userSignService.repairSign(userId, dto));
        }

        @Test
        @DisplayName("补签超过最大天数限制_抛出异常")
        void repairSign_exceedMaxDays_throwsException() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().minusDays(UserConstants.REPAIR_SIGN_MAX_DAYS + 1).toString());

            assertThrows(BusinessException.class, () -> userSignService.repairSign(userId, dto));
        }

        @Test
        @DisplayName("补签已签到日期_抛出异常")
        void repairSign_alreadySigned_throwsException() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().minusDays(1).toString());

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(testSignRecord);

            assertThrows(BusinessException.class, () -> userSignService.repairSign(userId, dto));
        }

        @Test
        @DisplayName("本月补签次数已达上限_抛出异常")
        void repairSign_maxMonthlyReached_throwsException() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().minusDays(1).toString());

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.countRepairByUserIdAndMonth(eq(userId), anyInt(), anyInt()))
                    .thenReturn(UserConstants.REPAIR_SIGN_MAX_MONTHLY);

            assertThrows(BusinessException.class, () -> userSignService.repairSign(userId, dto));
        }

        @Test
        @DisplayName("积分不足_抛出异常")
        void repairSign_insufficientPoints_throwsException() {
            SignDTO dto = new SignDTO();
            dto.setSignDate(LocalDate.now().minusDays(1).toString());

            testUserPoints.setAvailablePoints(UserConstants.REPAIR_SIGN_COST - 1);

            when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
            when(userSignRecordMapper.countRepairByUserIdAndMonth(eq(userId), anyInt(), anyInt())).thenReturn(0);
            when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

            assertThrows(BusinessException.class, () -> userSignService.repairSign(userId, dto));
        }
    }

    @Nested
    @DisplayName("积分奖励计算测试")
    class RewardPointsTests {

        @Test
        @DisplayName("第1-6天奖励基础积分")
        void calculateRewardPoints_baseReward() {
            // 通过签到验证
            for (int day = 1; day <= 6; day++) {
                UserSignRecord prevRecord = day > 1 ? createRecord(day - 1) : null;
                when(userSignRecordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);
                when(userSignRecordMapper.selectLatestByUserId(userId)).thenReturn(prevRecord);
                when(userSignRecordMapper.insert(any(UserSignRecord.class))).thenReturn(1);
                when(userPointsMapper.selectByUserId(userId)).thenReturn(testUserPoints);

                SignResultVO result = userSignService.sign(userId);
                assertEquals(UserConstants.SIGN_BASE_POINTS, result.getRewardPoints());

                reset(userSignRecordMapper, userPointsMapper);
            }
        }

        private UserSignRecord createRecord(int continuousDays) {
            UserSignRecord record = new UserSignRecord();
            record.setSignDate(LocalDate.now().minusDays(1));
            record.setContinuousDays(continuousDays);
            return record;
        }
    }
}