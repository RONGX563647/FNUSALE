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
import com.fnusale.marketing.mapper.SeckillReminderMapper;
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
import org.springframework.data.redis.core.SetOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 秒杀服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceImplTest {

    @Mock
    private SeckillActivityMapper activityMapper;

    @Mock
    private SeckillReminderMapper reminderMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private SeckillActivity testActivity;
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

        testActivityDTO = new SeckillActivityDTO();
        testActivityDTO.setActivityName("新秒杀活动");
        testActivityDTO.setProductId(100L);
        testActivityDTO.setSeckillPrice(new BigDecimal("88.00"));
        testActivityDTO.setTotalStock(20);
        testActivityDTO.setStartTime(LocalDateTime.now().plusHours(1));
        testActivityDTO.setEndTime(LocalDateTime.now().plusHours(2));

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Nested
    @DisplayName("获取秒杀活动列表测试")
    class GetSeckillListTests {

        @Test
        @DisplayName("成功获取秒杀活动列表")
        void shouldReturnSeckillList() {
            when(activityMapper.selectActiveActivities()).thenReturn(List.of(testActivity));
            when(reminderMapper.countByUserAndActivity(1L, 1L)).thenReturn(0);

            List<SeckillActivityVO> result = seckillService.getSeckillList(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试秒杀活动", result.get(0).getActivityName());
            assertFalse(result.get(0).getReminded());
        }

        @Test
        @DisplayName("用户已设置提醒的活动应标记")
        void shouldMarkRemindedActivities() {
            when(activityMapper.selectActiveActivities()).thenReturn(List.of(testActivity));
            when(reminderMapper.countByUserAndActivity(1L, 1L)).thenReturn(1);

            List<SeckillActivityVO> result = seckillService.getSeckillList(1L);

            assertTrue(result.get(0).getReminded());
        }

        @Test
        @DisplayName("无用户ID时返回未提醒状态")
        void shouldReturnNotRemindedWhenNoUserId() {
            when(activityMapper.selectActiveActivities()).thenReturn(List.of(testActivity));

            List<SeckillActivityVO> result = seckillService.getSeckillList(null);

            assertFalse(result.get(0).getReminded());
        }

        @Test
        @DisplayName("无活动时返回空列表")
        void shouldReturnEmptyListWhenNoActivities() {
            when(activityMapper.selectActiveActivities()).thenReturn(Collections.emptyList());

            List<SeckillActivityVO> result = seckillService.getSeckillList(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取秒杀活动详情测试")
    class GetActivityDetailTests {

        @Test
        @DisplayName("成功获取活动详情")
        void shouldReturnActivityDetail() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            SeckillActivityVO result = seckillService.getActivityDetail(1L);

            assertNotNull(result);
            assertEquals("测试秒杀活动", result.getActivityName());
        }

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.getActivityDetail(1L));

            assertEquals(5001, exception.getCode());
            assertEquals("秒杀活动不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("参与秒杀测试")
    class JoinSeckillTests {

        @BeforeEach
        void setUpForSeckill() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_ON_GOING);
        }

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5001, exception.getCode());
        }

        @Test
        @DisplayName("活动未开始时抛出异常")
        void shouldThrowExceptionWhenActivityNotStarted() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_NOT_START);
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5002, exception.getCode());
            assertEquals("秒杀活动尚未开始", exception.getMessage());
        }

        @Test
        @DisplayName("活动已结束时抛出异常")
        void shouldThrowExceptionWhenActivityEnded() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_END);
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5003, exception.getCode());
            assertEquals("秒杀活动已结束", exception.getMessage());
        }

        @Test
        @DisplayName("用户已参与过秒杀时抛出异常")
        void shouldThrowExceptionWhenUserAlreadyJoined() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5005, exception.getCode());
            assertEquals("您已参与过该秒杀", exception.getMessage());
        }

        @Test
        @DisplayName("库存不足时抛出异常")
        void shouldThrowExceptionWhenStockInsufficient() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(false);
            when(valueOperations.get(anyString())).thenReturn("0");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5004, exception.getCode());
            assertEquals("秒杀库存不足", exception.getMessage());
        }

        @Test
        @DisplayName("Redis扣减后库存为负时抛出异常")
        void shouldThrowExceptionWhenStockNegativeAfterDecrement() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(false);
            when(valueOperations.get(anyString())).thenReturn("1");
            when(valueOperations.decrement(anyString())).thenReturn(-1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.joinSeckill(1L, 1L));

            assertEquals(5004, exception.getCode());
            verify(valueOperations).increment(anyString());
        }

        @Test
        @DisplayName("成功参与秒杀")
        void shouldJoinSeckillSuccessfully() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(false);
            when(valueOperations.get(anyString())).thenReturn("5");
            when(valueOperations.decrement(anyString())).thenReturn(4L);

            Long orderId = seckillService.joinSeckill(1L, 1L);

            assertNotNull(orderId);
            verify(setOperations).add(anyString(), eq("1"));
        }

        @Test
        @DisplayName("Redis无库存时从DB加载")
        void shouldLoadStockFromDbWhenRedisEmpty() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(false);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(valueOperations.decrement(anyString())).thenReturn(4L);

            Long orderId = seckillService.joinSeckill(1L, 1L);

            assertNotNull(orderId);
            verify(valueOperations).set(anyString(), eq("5"));
        }
    }

    @Nested
    @DisplayName("获取秒杀结果测试")
    class GetSeckillResultTests {

        @Test
        @DisplayName("用户已参与秒杀返回成功结果")
        void shouldReturnSuccessResult() {
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(true);

            SeckillResultVO result = seckillService.getSeckillResult(1L, 1L);

            assertNotNull(result);
            assertTrue(result.getSuccess());
        }

        @Test
        @DisplayName("用户未参与秒杀返回失败结果")
        void shouldReturnFailResult() {
            when(setOperations.isMember(anyString(), eq("1"))).thenReturn(false);

            SeckillResultVO result = seckillService.getSeckillResult(1L, 1L);

            assertNotNull(result);
            assertFalse(result.getSuccess());
            assertEquals("您未参与该秒杀活动", result.getMessage());
        }
    }

    @Nested
    @DisplayName("创建秒杀活动测试")
    class CreateActivityTests {

        @Test
        @DisplayName("成功创建秒杀活动")
        void shouldCreateActivitySuccessfully() {
            when(activityMapper.insert(any(SeckillActivity.class))).thenReturn(1);

            assertDoesNotThrow(() -> seckillService.createActivity(testActivityDTO));

            verify(activityMapper).insert(any(SeckillActivity.class));
        }

        @Test
        @DisplayName("活动时长超过2小时抛出异常")
        void shouldThrowExceptionWhenDurationExceeds2Hours() {
            testActivityDTO.setEndTime(LocalDateTime.now().plusHours(4));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.createActivity(testActivityDTO));

            assertEquals(400, exception.getCode());
            assertEquals("活动时长不能超过2小时", exception.getMessage());
        }

        @Test
        @DisplayName("秒杀库存超过50件抛出异常")
        void shouldThrowExceptionWhenStockExceeds50() {
            testActivityDTO.setTotalStock(100);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.createActivity(testActivityDTO));

            assertEquals(400, exception.getCode());
            assertEquals("秒杀库存不超过50件", exception.getMessage());
        }

        @Test
        @DisplayName("开始时间早于当前时间抛出异常")
        void shouldThrowExceptionWhenStartTimeBeforeNow() {
            testActivityDTO.setStartTime(LocalDateTime.now().minusHours(1));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.createActivity(testActivityDTO));

            assertEquals(400, exception.getCode());
            assertEquals("开始时间必须大于当前时间", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("更新秒杀活动测试")
    class UpdateActivityTests {

        @Test
        @DisplayName("成功更新秒杀活动")
        void shouldUpdateActivitySuccessfully() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(activityMapper.updateById(any(SeckillActivity.class))).thenReturn(1);

            assertDoesNotThrow(() -> seckillService.updateActivity(1L, testActivityDTO));

            verify(activityMapper).updateById(any(SeckillActivity.class));
        }

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.updateActivity(1L, testActivityDTO));

            assertEquals(5001, exception.getCode());
        }

        @Test
        @DisplayName("非未开始状态的活动不可修改")
        void shouldThrowExceptionWhenActivityNotModifiable() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_ON_GOING);
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.updateActivity(1L, testActivityDTO));

            assertEquals(400, exception.getCode());
            assertEquals("只能修改未开始的活动", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("删除秒杀活动测试")
    class DeleteActivityTests {

        @Test
        @DisplayName("成功删除秒杀活动")
        void shouldDeleteActivitySuccessfully() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(activityMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> seckillService.deleteActivity(1L));

            verify(activityMapper).deleteById(1L);
        }

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.deleteActivity(1L));

            assertEquals(5001, exception.getCode());
        }

        @Test
        @DisplayName("非未开始状态的活动不可删除")
        void shouldThrowExceptionWhenActivityNotDeletable() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_END);
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.deleteActivity(1L));

            assertEquals(400, exception.getCode());
            assertEquals("只能删除未开始的活动", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("分页查询秒杀活动测试")
    class GetActivityPageTests {

        @Test
        @DisplayName("成功分页查询活动")
        void shouldReturnActivityPage() {
            Page<SeckillActivity> page = new Page<>(1, 10);
            page.setRecords(List.of(testActivity));
            page.setTotal(1);

            when(activityMapper.selectActivityPage(any(Page.class), any())).thenReturn(page);

            IPage<SeckillActivityVO> result = seckillService.getActivityPage(null, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("今日秒杀测试")
    class GetTodaySeckillsTests {

        @Test
        @DisplayName("成功获取今日秒杀列表")
        void shouldReturnTodaySeckills() {
            when(activityMapper.selectTodayActivities()).thenReturn(List.of(testActivity));
            when(reminderMapper.countByUserAndActivity(anyLong(), anyLong())).thenReturn(0);

            List<TodaySeckillVO> result = seckillService.getTodaySeckills(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("无今日秒杀返回空列表")
        void shouldReturnEmptyListWhenNoTodaySeckills() {
            when(activityMapper.selectTodayActivities()).thenReturn(Collections.emptyList());

            List<TodaySeckillVO> result = seckillService.getTodaySeckills(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("秒杀提醒测试")
    class ReminderTests {

        @Test
        @DisplayName("成功设置提醒")
        void shouldSetReminderSuccessfully() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(reminderMapper.countByUserAndActivity(1L, 1L)).thenReturn(0);
            when(reminderMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> seckillService.setReminder(1L, 1L));

            verify(reminderMapper).insert(any());
        }

        @Test
        @DisplayName("活动不存在时抛出异常")
        void shouldThrowExceptionWhenActivityNotFound() {
            when(activityMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.setReminder(1L, 1L));

            assertEquals(5001, exception.getCode());
        }

        @Test
        @DisplayName("非未开始活动不能设置提醒")
        void shouldThrowExceptionWhenActivityNotNotStart() {
            testActivity.setActivityStatus(MarketingConstants.SECKILL_STATUS_ON_GOING);
            when(activityMapper.selectById(1L)).thenReturn(testActivity);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.setReminder(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("只能对未开始的活动设置提醒", exception.getMessage());
        }

        @Test
        @DisplayName("已设置过提醒抛出异常")
        void shouldThrowExceptionWhenAlreadyReminded() {
            when(activityMapper.selectById(1L)).thenReturn(testActivity);
            when(reminderMapper.countByUserAndActivity(1L, 1L)).thenReturn(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.setReminder(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("您已设置过提醒", exception.getMessage());
        }

        @Test
        @DisplayName("成功取消提醒")
        void shouldCancelReminderSuccessfully() {
            when(reminderMapper.deleteByUserAndActivity(1L, 1L)).thenReturn(1);

            assertDoesNotThrow(() -> seckillService.cancelReminder(1L, 1L));

            verify(reminderMapper).deleteByUserAndActivity(1L, 1L);
        }

        @Test
        @DisplayName("未设置提醒时取消抛出异常")
        void shouldThrowExceptionWhenNoReminderToCancel() {
            when(reminderMapper.deleteByUserAndActivity(1L, 1L)).thenReturn(0);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> seckillService.cancelReminder(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("未设置该活动的提醒", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("预热库存测试")
    class PreloadStockTests {

        @Test
        @DisplayName("成功预热库存")
        void shouldPreloadStockSuccessfully() {
            when(activityMapper.selectStartingSoon()).thenReturn(List.of(testActivity));

            seckillService.preloadStock();

            verify(valueOperations).set(anyString(), eq("5"));
        }

        @Test
        @DisplayName("无即将开始的活动时不执行操作")
        void shouldDoNothingWhenNoStartingSoonActivities() {
            when(activityMapper.selectStartingSoon()).thenReturn(Collections.emptyList());

            seckillService.preloadStock();

            verify(valueOperations, never()).set(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("推送提醒测试")
    class PushRemindersTests {

        @Test
        @DisplayName("成功推送提醒")
        void shouldPushRemindersSuccessfully() {
            when(activityMapper.selectStartingIn5Minutes()).thenReturn(List.of(testActivity));
            when(reminderMapper.selectUserIdsByActivity(1L)).thenReturn(List.of(1L, 2L));
            when(reminderMapper.updateReminded(1L)).thenReturn(1);

            seckillService.pushReminders();

            verify(reminderMapper).updateReminded(1L);
        }

        @Test
        @DisplayName("无用户需要提醒时不更新状态")
        void shouldNotUpdateWhenNoUsersToRemind() {
            when(activityMapper.selectStartingIn5Minutes()).thenReturn(List.of(testActivity));
            when(reminderMapper.selectUserIdsByActivity(1L)).thenReturn(Collections.emptyList());

            seckillService.pushReminders();

            verify(reminderMapper, never()).updateReminded(anyLong());
        }
    }

    @Nested
    @DisplayName("更新活动状态测试")
    class UpdateActivityStatusTests {

        @Test
        @DisplayName("成功更新活动状态")
        void shouldUpdateActivityStatusSuccessfully() {
            when(activityMapper.updateToOngoing()).thenReturn(2);
            when(activityMapper.updateToEnded()).thenReturn(1);

            seckillService.updateActivityStatus();

            verify(activityMapper).updateToOngoing();
            verify(activityMapper).updateToEnded();
        }
    }

    @Nested
    @DisplayName("获取秒杀时段测试")
    class GetTimeSlotsTests {

        @Test
        @DisplayName("成功获取秒杀时段列表")
        void shouldReturnTimeSlots() {
            when(activityMapper.selectTimeSlots()).thenReturn(List.of("10:00", "14:00", "20:00"));

            List<String> result = seckillService.getTimeSlots();

            assertNotNull(result);
            assertEquals(3, result.size());
        }
    }
}