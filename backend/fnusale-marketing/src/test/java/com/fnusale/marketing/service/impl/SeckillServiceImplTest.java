package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.service.SeckillAntiFraudService;
import com.fnusale.marketing.service.core.SeckillCoreService;
import com.fnusale.marketing.service.core.SeckillManageService;
import com.fnusale.marketing.service.core.SeckillQueryService;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 秒杀服务门面测试（v4优化：解耦重构后）
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceImplTest {

    @Mock
    private SeckillCoreService coreService;

    @Mock
    private SeckillQueryService queryService;

    @Mock
    private SeckillManageService manageService;

    @Mock
    private SeckillAntiFraudService antiFraudService;

    @Mock
    private SeckillActivityMapper activityMapper;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private SeckillActivity testActivity;
    private SeckillActivityVO testActivityVO;
    private SeckillActivityDTO testActivityDTO;

    @BeforeEach
    void setUp() {
        testActivity = SeckillActivity.builder()
                .id(1L)
                .activityName("测试秒杀活动")
                .productId(100L)
                .seckillPrice(new BigDecimal("99.00"))
                .totalStock(10)
                .remainStock(5)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .activityStatus(MarketingConstants.SECKILL_STATUS_NOT_START)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testActivityVO = new SeckillActivityVO();
        testActivityVO.setId(1L);
        testActivityVO.setActivityName("测试秒杀活动");

        testActivityDTO = new SeckillActivityDTO();
        testActivityDTO.setActivityName("新秒杀活动");
        testActivityDTO.setProductId(100L);
        testActivityDTO.setSeckillPrice(new BigDecimal("88.00"));
        testActivityDTO.setTotalStock(20);
        testActivityDTO.setStartTime(LocalDateTime.now().plusHours(1));
        testActivityDTO.setEndTime(LocalDateTime.now().plusHours(2));
    }

    @Nested
    @DisplayName("门面委托测试")
    class FacadeDelegationTests {

        @Test
        @DisplayName("getSeckillList委托给QueryService")
        void shouldDelegateGetSeckillList() {
            when(queryService.getSeckillList(1L)).thenReturn(List.of(testActivityVO));

            List<SeckillActivityVO> result = seckillService.getSeckillList(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(queryService).getSeckillList(1L);
        }

        @Test
        @DisplayName("getActivityDetail委托给QueryService")
        void shouldDelegateGetActivityDetail() {
            when(queryService.getActivityDetail(1L)).thenReturn(testActivityVO);

            SeckillActivityVO result = seckillService.getActivityDetail(1L);

            assertNotNull(result);
            verify(queryService).getActivityDetail(1L);
        }

        @Test
        @DisplayName("getSeckillProductDetail委托给QueryService")
        void shouldDelegateGetSeckillProductDetail() {
            when(queryService.getSeckillProductDetail(100L)).thenReturn(new Object());

            Object result = seckillService.getSeckillProductDetail(100L);

            assertNotNull(result);
            verify(queryService).getSeckillProductDetail(100L);
        }

        @Test
        @DisplayName("getSeckillResult委托给QueryService")
        void shouldDelegateGetSeckillResult() {
            SeckillResultVO mockResult = SeckillResultVO.fail("测试");
            when(queryService.getSeckillResult(1L, 1L)).thenReturn(mockResult);

            SeckillResultVO result = seckillService.getSeckillResult(1L, 1L);

            assertNotNull(result);
            verify(queryService).getSeckillResult(1L, 1L);
        }

        @Test
        @DisplayName("getTodaySeckills委托给QueryService")
        void shouldDelegateGetTodaySeckills() {
            when(queryService.getTodaySeckills(1L)).thenReturn(Collections.emptyList());

            List<TodaySeckillVO> result = seckillService.getTodaySeckills(1L);

            assertNotNull(result);
            verify(queryService).getTodaySeckills(1L);
        }

        @Test
        @DisplayName("getTimeSlots委托给QueryService")
        void shouldDelegateGetTimeSlots() {
            when(queryService.getTimeSlots()).thenReturn(List.of("10:00", "14:00"));

            List<String> result = seckillService.getTimeSlots();

            assertNotNull(result);
            verify(queryService).getTimeSlots();
        }

        @Test
        @DisplayName("createActivity委托给ManageService")
        void shouldDelegateCreateActivity() {
            when(manageService.createActivity(testActivityDTO)).thenReturn(1L);

            seckillService.createActivity(testActivityDTO);

            verify(manageService).createActivity(testActivityDTO);
        }

        @Test
        @DisplayName("updateActivity委托给ManageService")
        void shouldDelegateUpdateActivity() {
            doNothing().when(manageService).updateActivity(1L, testActivityDTO);

            seckillService.updateActivity(1L, testActivityDTO);

            verify(manageService).updateActivity(1L, testActivityDTO);
        }

        @Test
        @DisplayName("deleteActivity委托给ManageService")
        void shouldDelegateDeleteActivity() {
            doNothing().when(manageService).deleteActivity(1L);

            seckillService.deleteActivity(1L);

            verify(manageService).deleteActivity(1L);
        }

        @Test
        @DisplayName("setReminder委托给ManageService")
        void shouldDelegateSetReminder() {
            doNothing().when(manageService).setReminder(1L, 1L);

            seckillService.setReminder(1L, 1L);

            verify(manageService).setReminder(1L, 1L);
        }

        @Test
        @DisplayName("cancelReminder委托给ManageService")
        void shouldDelegateCancelReminder() {
            doNothing().when(manageService).cancelReminder(1L, 1L);

            seckillService.cancelReminder(1L, 1L);

            verify(manageService).cancelReminder(1L, 1L);
        }

        @Test
        @DisplayName("preloadStock委托给ManageService")
        void shouldDelegatePreloadStock() {
            doNothing().when(manageService).preloadStock();

            seckillService.preloadStock();

            verify(manageService).preloadStock();
        }

        @Test
        @DisplayName("pushReminders委托给ManageService")
        void shouldDelegatePushReminders() {
            doNothing().when(manageService).pushReminders();

            seckillService.pushReminders();

            verify(manageService).pushReminders();
        }

        @Test
        @DisplayName("updateActivityStatus委托给ManageService")
        void shouldDelegateUpdateActivityStatus() {
            doNothing().when(manageService).updateActivityStatus();

            seckillService.updateActivityStatus();

            verify(manageService).updateActivityStatus();
        }

        @Test
        @DisplayName("getActivityPage委托给ManageService")
        void shouldDelegateGetActivityPage() {
            Page<SeckillActivityVO> page = new Page<>(1, 10);
            when(manageService.getActivityPage(null, 1, 10)).thenReturn(page);

            IPage<SeckillActivityVO> result = seckillService.getActivityPage(null, 1, 10);

            assertNotNull(result);
            verify(manageService).getActivityPage(null, 1, 10);
        }
    }

    @Nested
    @DisplayName("joinSeckill测试")
    class JoinSeckillTests {

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L, "127.0.0.1", null, null));

            assertEquals(5001, exception.getCode());
        }

        @Test
        @DisplayName("成功参与秒杀")
        void shouldJoinSeckillSuccessfully() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(coreService.executeSeckill(1L, 1L, testActivity, "127.0.0.1")).thenReturn(1L);

            Long result = seckillService.joinSeckill(1L, 1L, "127.0.0.1", null, null);

            assertNotNull(result);
            verify(coreService).executeSeckill(1L, 1L, testActivity, "127.0.0.1");
        }
    }

    @Nested
    @DisplayName("验证码测试")
    class CaptchaTests {

        @Test
        @DisplayName("needCaptcha委托给AntiFraudService")
        void shouldDelegateNeedCaptcha() {
            when(antiFraudService.needCaptcha(1L, 1L)).thenReturn(true);

            boolean result = seckillService.needCaptcha(1L, 1L);

            assertTrue(result);
            verify(antiFraudService).needCaptcha(1L, 1L);
        }

        @Test
        @DisplayName("generateCaptcha委托给AntiFraudService")
        void shouldDelegateGenerateCaptcha() {
            when(antiFraudService.generateCaptchaKey(1L, 1L)).thenReturn("captcha-key");

            String result = seckillService.generateCaptcha(1L, 1L);

            assertEquals("captcha-key", result);
            verify(antiFraudService).generateCaptchaKey(1L, 1L);
        }
    }
}
