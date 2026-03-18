package com.fnusale.admin.service.impl;

import com.fnusale.admin.mapper.OperationStatisticsMapper;
import com.fnusale.admin.mapper.OrderMapper;
import com.fnusale.admin.mapper.ProductMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.common.entity.OperationStatistics;
import com.fnusale.common.vo.admin.TodayStatisticsVO;
import com.fnusale.common.vo.admin.TrendDataVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AdminStatisticsService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OperationStatisticsMapper operationStatisticsMapper;

    @InjectMocks
    private AdminStatisticsServiceImpl adminStatisticsService;

    @Nested
    @DisplayName("今日数据统计测试")
    class GetTodayStatisticsTest {

        @Test
        @DisplayName("获取今日统计数据成功")
        void getTodayStatistics_success() {
            // Arrange
            LocalDate today = LocalDate.now();
            when(userMapper.countTodayNewUsers(today)).thenReturn(10);
            when(userMapper.countPendingAuth()).thenReturn(5);
            when(productMapper.countTodayPublish(today)).thenReturn(20);
            when(productMapper.countPendingAudit()).thenReturn(8);
            when(orderMapper.countTodaySuccess(today)).thenReturn(15);
            when(orderMapper.sumTodayAmount(today)).thenReturn(new BigDecimal("1500.00"));

            // Act
            TodayStatisticsVO result = adminStatisticsService.getTodayStatistics();

            // Assert
            assertNotNull(result);
            assertEquals(10, result.getNewUserCount());
            assertEquals(10, result.getActiveUserCount()); // 暂时等于新增用户数
            assertEquals(20, result.getProductPublishCount());
            assertEquals(15, result.getOrderSuccessCount());
            assertEquals(new BigDecimal("1500.00"), result.getOrderSuccessAmount());
            assertEquals(8, result.getPendingAuditCount());
            assertEquals(5, result.getPendingAuthCount());
        }

        @Test
        @DisplayName("今日统计-无数据时返回0")
        void getTodayStatistics_noData() {
            // Arrange
            LocalDate today = LocalDate.now();
            when(userMapper.countTodayNewUsers(today)).thenReturn(0);
            when(userMapper.countPendingAuth()).thenReturn(0);
            when(productMapper.countTodayPublish(today)).thenReturn(0);
            when(productMapper.countPendingAudit()).thenReturn(0);
            when(orderMapper.countTodaySuccess(today)).thenReturn(0);
            when(orderMapper.sumTodayAmount(today)).thenReturn(BigDecimal.ZERO);

            // Act
            TodayStatisticsVO result = adminStatisticsService.getTodayStatistics();

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getNewUserCount());
            assertEquals(0, result.getProductPublishCount());
            assertEquals(0, result.getOrderSuccessCount());
        }
    }

    @Nested
    @DisplayName("商品发布趋势测试")
    class GetProductTrendTest {

        @Test
        @DisplayName("获取商品发布趋势成功")
        void getProductTrend_success() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);

            OperationStatistics stat = new OperationStatistics();
            stat.setStatDate(today);
            stat.setProductPublishCount(10);

            when(operationStatisticsMapper.selectByDateRange(eq(startDate), eq(today)))
                    .thenReturn(List.of(stat));

            // Act
            TrendDataVO result = adminStatisticsService.getProductTrend(7);

            // Assert
            assertNotNull(result);
            assertEquals(7, result.getXAxis().size());
            assertEquals(2, result.getSeries().size());
            assertEquals("发布数量", result.getSeries().get(0).getName());
            assertEquals("审核通过数量", result.getSeries().get(1).getName());
        }

        @Test
        @DisplayName("商品发布趋势-空数据返回全0")
        void getProductTrend_emptyData() {
            // Arrange
            when(operationStatisticsMapper.selectByDateRange(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            TrendDataVO result = adminStatisticsService.getProductTrend(7);

            // Assert
            assertNotNull(result);
            assertEquals(7, result.getXAxis().size());
            // 所有数据应为0
            assertTrue(result.getSeries().get(0).getData().stream().allMatch(v -> v == 0));
        }
    }

    @Nested
    @DisplayName("成交趋势测试")
    class GetOrderTrendTest {

        @Test
        @DisplayName("获取成交趋势成功")
        void getOrderTrend_success() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);

            OperationStatistics stat = new OperationStatistics();
            stat.setStatDate(today);
            stat.setOrderSuccessCount(5);

            when(operationStatisticsMapper.selectByDateRange(eq(startDate), eq(today)))
                    .thenReturn(List.of(stat));

            // Act
            TrendDataVO result = adminStatisticsService.getOrderTrend(7);

            // Assert
            assertNotNull(result);
            assertEquals(7, result.getXAxis().size());
            assertEquals(1, result.getSeries().size());
            assertEquals("成交数量", result.getSeries().get(0).getName());
        }
    }

    @Nested
    @DisplayName("用户增长趋势测试")
    class GetUserGrowthTrendTest {

        @Test
        @DisplayName("获取用户增长趋势成功")
        void getUserGrowthTrend_success() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);

            OperationStatistics stat = new OperationStatistics();
            stat.setStatDate(today);
            stat.setNewUserCount(3);

            when(operationStatisticsMapper.selectByDateRange(eq(startDate), eq(today)))
                    .thenReturn(List.of(stat));

            // Act
            TrendDataVO result = adminStatisticsService.getUserGrowthTrend(7);

            // Assert
            assertNotNull(result);
            assertEquals(7, result.getXAxis().size());
            assertEquals(1, result.getSeries().size());
            assertEquals("新增用户", result.getSeries().get(0).getName());
        }

        @Test
        @DisplayName("用户增长趋势-null用户数处理为0")
        void getUserGrowthTrend_nullUserCount() {
            // Arrange
            LocalDate today = LocalDate.now();

            OperationStatistics stat = new OperationStatistics();
            stat.setStatDate(today);
            stat.setNewUserCount(null);

            when(operationStatisticsMapper.selectByDateRange(any(), any()))
                    .thenReturn(List.of(stat));

            // Act
            TrendDataVO result = adminStatisticsService.getUserGrowthTrend(7);

            // Assert
            assertNotNull(result);
            // 验证null值被处理为0
            assertEquals(0, result.getSeries().get(0).getData().get(6)); // 最后一个元素是今天
        }
    }

    @Nested
    @DisplayName("导出统计测试")
    class ExportStatisticsTest {

        @Test
        @DisplayName("导出统计-目前返回null(TODO)")
        void exportStatistics_returnsNull() {
            // Act
            String result = adminStatisticsService.exportStatistics(LocalDate.now(), LocalDate.now());

            // Assert
            assertNull(result); // TODO功能，暂返回null
        }
    }
}