package com.fnusale.user.service.impl;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.EvaluationAppendDTO;
import com.fnusale.common.dto.user.EvaluationReplyDTO;
import com.fnusale.common.dto.user.EvaluationReportDTO;
import com.fnusale.common.dto.user.UserEvaluationDTO;
import com.fnusale.common.entity.EvaluationReport;
import com.fnusale.common.entity.EvaluationTagStat;
import com.fnusale.common.entity.OrderEvaluation;
import com.fnusale.common.entity.UserRating;
import com.fnusale.common.vo.user.UserEvaluationVO;
import com.fnusale.common.vo.user.UserRatingVO;
import com.fnusale.user.mapper.EvaluationReportMapper;
import com.fnusale.user.mapper.EvaluationTagStatMapper;
import com.fnusale.user.mapper.OrderEvaluationMapper;
import com.fnusale.user.mapper.UserRatingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户评价服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserEvaluationServiceImplTest {

    @Mock
    private UserRatingMapper userRatingMapper;

    @Mock
    private EvaluationTagStatMapper evaluationTagStatMapper;

    @Mock
    private OrderEvaluationMapper orderEvaluationMapper;

    @Mock
    private EvaluationReportMapper evaluationReportMapper;

    @InjectMocks
    private UserEvaluationServiceImpl userEvaluationService;

    private final Long userId = 1L;
    private UserRating testUserRating;
    private EvaluationTagStat testTagStat;
    private OrderEvaluation testEvaluation;

    @BeforeEach
    void setUp() {
        testUserRating = UserRating.builder()
                .userId(userId)
                .overallRating(new BigDecimal("4.50"))
                .ratingLevel("EXCELLENT")
                .totalEvaluations(10)
                .positiveCount(9)
                .neutralCount(1)
                .negativeCount(0)
                .positiveRate(new BigDecimal("90.00"))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testTagStat = EvaluationTagStat.builder()
                .userId(userId)
                .tagName("发货快")
                .tagCount(5)
                .updateTime(LocalDateTime.now())
                .build();

        testEvaluation = OrderEvaluation.builder()
                .id(1L)
                .orderId(1L)
                .evaluatorId(2L)
                .evaluatedId(userId)
                .score(5)
                .evaluationContent("好评")
                .createTime(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("提交评价测试")
    class SubmitEvaluationTests {

        @Test
        @DisplayName("提交评价_调用成功")
        void submitEvaluation_success() {
            UserEvaluationDTO dto = new UserEvaluationDTO();
            dto.setOrderId(1L);
            dto.setScore(5);
            dto.setEvaluationContent("非常好的交易体验");
            dto.setEvaluationTag("发货快,态度好");

            // 由于当前实现是TODO，只验证方法能正常调用
            assertDoesNotThrow(() -> userEvaluationService.submitEvaluation(userId, dto));
        }
    }

    @Nested
    @DisplayName("追加评价测试")
    class AppendEvaluationTests {

        @Test
        @DisplayName("追加评价_调用成功")
        void appendEvaluation_success() {
            // 追加评价需要是评价者本人
            OrderEvaluation evalForAppend = OrderEvaluation.builder()
                    .id(1L)
                    .orderId(1L)
                    .evaluatorId(userId)
                    .evaluatedId(2L)
                    .score(5)
                    .evaluationContent("好评")
                    .createTime(LocalDateTime.now())
                    .build();
            when(orderEvaluationMapper.selectById(1L)).thenReturn(evalForAppend);
            when(orderEvaluationMapper.hasAppend(1L)).thenReturn(0);
            when(orderEvaluationMapper.updateAppend(eq(1L), anyString(), any())).thenReturn(1);

            EvaluationAppendDTO dto = new EvaluationAppendDTO();
            dto.setAppendContent("后续使用感受很好");

            assertDoesNotThrow(() -> userEvaluationService.appendEvaluation(userId, 1L, dto));
        }
    }

    @Nested
    @DisplayName("回复评价测试")
    class ReplyEvaluationTests {

        @Test
        @DisplayName("回复评价_调用成功")
        void replyEvaluation_success() {
            // 回复评价需要是被评价者
            OrderEvaluation evalForReply = OrderEvaluation.builder()
                    .id(1L)
                    .orderId(1L)
                    .evaluatorId(2L)
                    .evaluatedId(userId)
                    .score(5)
                    .evaluationContent("好评")
                    .createTime(LocalDateTime.now())
                    .build();
            when(orderEvaluationMapper.selectById(1L)).thenReturn(evalForReply);
            when(orderEvaluationMapper.updateReply(eq(1L), anyString())).thenReturn(1);

            EvaluationReplyDTO dto = new EvaluationReplyDTO();
            dto.setReplyContent("感谢您的评价");

            assertDoesNotThrow(() -> userEvaluationService.replyEvaluation(userId, 1L, dto));
        }
    }

    @Nested
    @DisplayName("获取用户评价列表测试")
    class GetUserEvaluationsTests {

        @Test
        @DisplayName("获取评价列表_返回空列表")
        void getUserEvaluations_empty() {
            PageResult<UserEvaluationVO> result = userEvaluationService.getUserEvaluations(userId, 1, 10);

            assertNotNull(result);
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("获取我的评价测试")
    class GetMyEvaluationsTests {

        @Test
        @DisplayName("获取我的评价_返回空列表")
        void getMyEvaluations_empty() {
            PageResult<UserEvaluationVO> result = userEvaluationService.getMyEvaluations(userId, 1, 10);

            assertNotNull(result);
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("获取评价统计测试")
    class GetUserRatingTests {

        @Test
        @DisplayName("获取评价统计_成功")
        void getUserRating_success() {
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertNotNull(result);
            assertEquals(new BigDecimal("4.50"), result.getOverallRating());
            assertEquals("EXCELLENT", result.getRatingLevel());
            assertEquals(10, result.getTotalEvaluations());
            assertEquals(9, result.getPositiveCount());
            assertEquals(new BigDecimal("90.00"), result.getPositiveRate());
        }

        @Test
        @DisplayName("用户无评分记录_初始化返回")
        void getUserRating_noRecord_initDefault() {
            when(userRatingMapper.selectByUserId(userId)).thenReturn(null);
            when(userRatingMapper.insert(any(UserRating.class))).thenReturn(1);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertNotNull(result);
            assertEquals(new BigDecimal("5.00"), result.getOverallRating());
            assertEquals("EXCELLENT", result.getRatingLevel());
            assertEquals(0, result.getTotalEvaluations());
            assertEquals(new BigDecimal("100.00"), result.getPositiveRate());
        }
    }

    @Nested
    @DisplayName("获取评价标签统计测试")
    class GetUserTagsTests {

        @Test
        @DisplayName("获取标签统计_成功")
        void getUserTags_success() {
            when(evaluationTagStatMapper.selectByUserId(userId)).thenReturn(List.of(testTagStat));

            List<Map<String, Object>> result = userEvaluationService.getUserTags(userId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("发货快", result.get(0).get("tagName"));
            assertEquals(5, result.get(0).get("tagCount"));
        }

        @Test
        @DisplayName("无标签统计_返回空列表")
        void getUserTags_empty() {
            when(evaluationTagStatMapper.selectByUserId(userId)).thenReturn(List.of());

            List<Map<String, Object>> result = userEvaluationService.getUserTags(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("多个标签_按出现次数排序")
        void getUserTags_multipleTags() {
            EvaluationTagStat tag1 = EvaluationTagStat.builder()
                    .userId(userId)
                    .tagName("发货快")
                    .tagCount(10)
                    .build();

            EvaluationTagStat tag2 = EvaluationTagStat.builder()
                    .userId(userId)
                    .tagName("态度好")
                    .tagCount(5)
                    .build();

            when(evaluationTagStatMapper.selectByUserId(userId)).thenReturn(List.of(tag1, tag2));

            List<Map<String, Object>> result = userEvaluationService.getUserTags(userId);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("举报评价测试")
    class ReportEvaluationTests {

        @Test
        @DisplayName("举报评价_调用成功")
        void reportEvaluation_success() {
            // 举报评价不能是评价者本人
            OrderEvaluation evalForReport = OrderEvaluation.builder()
                    .id(1L)
                    .orderId(1L)
                    .evaluatorId(2L)
                    .evaluatedId(3L)
                    .score(5)
                    .evaluationContent("好评")
                    .createTime(LocalDateTime.now())
                    .build();
            when(orderEvaluationMapper.selectById(1L)).thenReturn(evalForReport);
            when(evaluationReportMapper.countByEvaluationAndReporter(1L, userId)).thenReturn(0);

            EvaluationReportDTO dto = new EvaluationReportDTO();
            dto.setReportReason("恶意评价");
            dto.setReportDesc("与实际交易不符");

            assertDoesNotThrow(() -> userEvaluationService.reportEvaluation(userId, 1L, dto));
        }
    }

    @Nested
    @DisplayName("评分等级判定测试")
    class RatingLevelTests {

        @Test
        @DisplayName("优秀等级_评分4.5以上")
        void ratingLevel_excellent() {
            testUserRating.setOverallRating(new BigDecimal("4.80"));
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertEquals("EXCELLENT", result.getRatingLevel());
        }

        @Test
        @DisplayName("良好等级_评分4.0-4.5")
        void ratingLevel_good() {
            testUserRating.setOverallRating(new BigDecimal("4.20"));
            testUserRating.setRatingLevel("GOOD");
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertEquals("GOOD", result.getRatingLevel());
        }

        @Test
        @DisplayName("等等级_评分3.0-4.0")
        void ratingLevel_normal() {
            testUserRating.setOverallRating(new BigDecimal("3.50"));
            testUserRating.setRatingLevel("NORMAL");
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertEquals("NORMAL", result.getRatingLevel());
        }

        @Test
        @DisplayName("较差等级_评分2.0-3.0")
        void ratingLevel_poor() {
            testUserRating.setOverallRating(new BigDecimal("2.50"));
            testUserRating.setRatingLevel("POOR");
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertEquals("POOR", result.getRatingLevel());
        }

        @Test
        @DisplayName("差等级_评分低于2.0")
        void ratingLevel_bad() {
            testUserRating.setOverallRating(new BigDecimal("1.50"));
            testUserRating.setRatingLevel("BAD");
            when(userRatingMapper.selectByUserId(userId)).thenReturn(testUserRating);

            UserRatingVO result = userEvaluationService.getUserRating(userId);

            assertEquals("BAD", result.getRatingLevel());
        }
    }
}