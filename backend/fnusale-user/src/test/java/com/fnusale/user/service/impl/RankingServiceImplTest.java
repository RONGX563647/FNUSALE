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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 排行榜服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private RankingRecordMapper rankingRecordMapper;

    @Mock
    private RankingRewardLogMapper rankingRewardLogMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRatingMapper userRatingMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RankingServiceImpl rankingService;

    private final Long userId = 1L;
    private RankingRecord testRecord;
    private User testUser;
    private RankingRewardLog testRewardLog;

    @BeforeEach
    void setUp() {
        testRecord = new RankingRecord();
        testRecord.setId(1L);
        testRecord.setRankType("ACTIVITY");
        testRecord.setRankDate(LocalDate.now());
        testRecord.setUserId(userId);
        testRecord.setRankPosition(1);
        testRecord.setScore(new BigDecimal("100.00"));
        testRecord.setCreateTime(LocalDateTime.now());

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("测试用户");
        testUser.setAvatarUrl("http://example.com/avatar.jpg");
        testUser.setCreditScore(100);

        testRewardLog = new RankingRewardLog();
        testRewardLog.setId(1L);
        testRewardLog.setUserId(userId);
        testRewardLog.setRankType("ACTIVITY");
        testRewardLog.setRankDate(LocalDate.now());
        testRewardLog.setRankPosition(1);
        testRewardLog.setRewardPoints(100);
        testRewardLog.setIsClaimed(0);
        testRewardLog.setCreateTime(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("活跃度排行榜测试")
    class GetActivityRankingTests {

        @Test
        @DisplayName("获取日榜_成功")
        void getActivityRanking_daily_success() {
            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(eq("ACTIVITY"), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of(testRecord));
            when(userMapper.selectById(userId)).thenReturn(testUser);

            List<RankingUserVO> result = rankingService.getActivityRanking("daily", null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getRank());
            assertEquals("测试用户", result.get(0).getUsername());
        }

        @Test
        @DisplayName("获取周榜_成功")
        void getActivityRanking_weekly_success() {
            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(eq("ACTIVITY"), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of(testRecord));
            when(userMapper.selectById(userId)).thenReturn(testUser);

            List<RankingUserVO> result = rankingService.getActivityRanking("weekly", null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("空排行榜_返回空列表")
        void getActivityRanking_empty() {
            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(anyString(), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of());

            List<RankingUserVO> result = rankingService.getActivityRanking("daily", null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("交易排行榜测试")
    class GetTradeRankingTests {

        @Test
        @DisplayName("获取交易排行_成功")
        void getTradeRanking_success() {
            testRecord.setRankType("TRADE");
            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(eq("TRADE"), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of(testRecord));
            when(userMapper.selectById(userId)).thenReturn(testUser);

            List<RankingUserVO> result = rankingService.getTradeRanking("daily", null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("信誉排行榜测试")
    class GetCreditRankingTests {

        @Test
        @DisplayName("获取信誉排行_成功")
        void getCreditRanking_success() {
            testRecord.setRankType("CREDIT");
            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(eq("CREDIT"), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of(testRecord));
            when(userMapper.selectById(userId)).thenReturn(testUser);

            List<RankingUserVO> result = rankingService.getCreditRanking();

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("好评排行榜测试")
    class GetRatingRankingTests {

        @Test
        @DisplayName("获取好评排行_成功")
        void getRatingRanking_success() {
            testRecord.setRankType("RATING");
            UserRating userRating = new UserRating();
            userRating.setOverallRating(new BigDecimal("4.80"));

            when(valueOperations.get(anyString())).thenReturn(null);
            when(rankingRecordMapper.selectByTypeAndDate(eq("RATING"), any(LocalDate.class), anyInt()))
                    .thenReturn(List.of(testRecord));
            when(userMapper.selectById(userId)).thenReturn(testUser);
            when(userRatingMapper.selectByUserId(userId)).thenReturn(userRating);

            List<RankingUserVO> result = rankingService.getRatingRanking();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(new BigDecimal("4.80"), result.get(0).getRating());
        }
    }

    @Nested
    @DisplayName("我的排名测试")
    class GetMyRankingTests {

        @Test
        @DisplayName("获取我的排名_成功")
        void getMyRanking_success() {
            when(rankingRecordMapper.selectByTypeDateAndUser(anyString(), any(LocalDate.class), eq(userId)))
                    .thenReturn(testRecord);

            MyRankingVO result = rankingService.getMyRanking(userId);

            assertNotNull(result);
            assertNotNull(result.getActivity());
            assertNotNull(result.getTrade());
            assertNotNull(result.getCredit());
            assertNotNull(result.getRating());
            assertEquals(1, result.getActivity().getRank());
            assertTrue(result.getActivity().getInList());
        }

        @Test
        @DisplayName("未上榜_返回null排名")
        void getMyRanking_notInList() {
            when(rankingRecordMapper.selectByTypeDateAndUser(anyString(), any(LocalDate.class), eq(userId)))
                    .thenReturn(null);

            MyRankingVO result = rankingService.getMyRanking(userId);

            assertNotNull(result);
            assertNull(result.getActivity().getRank());
            assertFalse(result.getActivity().getInList());
        }
    }

    @Nested
    @DisplayName("排行榜历史测试")
    class GetRankingHistoryTests {

        @Test
        @DisplayName("获取历史记录_成功")
        void getRankingHistory_success() {
            when(rankingRecordMapper.selectHistoryByTypeAndUser("ACTIVITY", userId))
                    .thenReturn(List.of(testRecord));

            PageResult<RankingUserVO> result = rankingService.getRankingHistory(userId, "ACTIVITY", 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }

        @Test
        @DisplayName("无历史记录_返回空列表")
        void getRankingHistory_empty() {
            when(rankingRecordMapper.selectHistoryByTypeAndUser("ACTIVITY", userId))
                    .thenReturn(List.of());

            PageResult<RankingUserVO> result = rankingService.getRankingHistory(userId, "ACTIVITY", 1, 10);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("领取奖励测试")
    class ClaimRewardTests {

        @Test
        @DisplayName("领取奖励_成功")
        void claimReward_success() {
            when(rankingRewardLogMapper.selectById(1L)).thenReturn(testRewardLog);
            when(rankingRewardLogMapper.claimReward(1L, userId)).thenReturn(1);

            assertDoesNotThrow(() -> rankingService.claimReward(userId, 1L));
        }

        @Test
        @DisplayName("奖励不存在_抛出异常")
        void claimReward_notFound_throwsException() {
            when(rankingRewardLogMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> rankingService.claimReward(userId, 999L));
        }

        @Test
        @DisplayName("无权领取_抛出异常")
        void claimReward_notOwner_throwsException() {
            testRewardLog.setUserId(2L);
            when(rankingRewardLogMapper.selectById(1L)).thenReturn(testRewardLog);

            assertThrows(BusinessException.class, () -> rankingService.claimReward(userId, 1L));
        }

        @Test
        @DisplayName("已领取_抛出异常")
        void claimReward_alreadyClaimed_throwsException() {
            testRewardLog.setIsClaimed(1);
            when(rankingRewardLogMapper.selectById(1L)).thenReturn(testRewardLog);

            assertThrows(BusinessException.class, () -> rankingService.claimReward(userId, 1L));
        }
    }

    @Nested
    @DisplayName("我的奖励列表测试")
    class GetMyRewardsTests {

        @Test
        @DisplayName("获取所有奖励_成功")
        void getMyRewards_all_success() {
            when(rankingRewardLogMapper.selectByUserId(userId)).thenReturn(List.of(testRewardLog));

            List<RankingRewardVO> result = rankingService.getMyRewards(userId, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("活跃度排行", result.get(0).getRankTypeName());
        }

        @Test
        @DisplayName("获取未领取奖励_成功")
        void getMyRewards_unclaimed_success() {
            when(rankingRewardLogMapper.selectUnclaimedByUserId(userId)).thenReturn(List.of(testRewardLog));

            List<RankingRewardVO> result = rankingService.getMyRewards(userId, false);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertFalse(result.get(0).getIsClaimed());
        }

        @Test
        @DisplayName("获取已领取奖励_成功")
        void getMyRewards_claimed_success() {
            testRewardLog.setIsClaimed(1);
            when(rankingRewardLogMapper.selectClaimedByUserId(userId)).thenReturn(List.of(testRewardLog));

            List<RankingRewardVO> result = rankingService.getMyRewards(userId, true);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsClaimed());
        }
    }
}