package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fnusale.admin.mapper.OperationStatisticsMapper;
import com.fnusale.admin.mapper.OrderMapper;
import com.fnusale.admin.mapper.ProductMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.AdminStatisticsService;
import com.fnusale.common.entity.OperationStatistics;
import com.fnusale.common.vo.admin.CategoryStatisticsVO;
import com.fnusale.common.vo.admin.TodayStatisticsVO;
import com.fnusale.common.vo.admin.TrendDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OperationStatisticsMapper operationStatisticsMapper;

    @Override
    public TodayStatisticsVO getTodayStatistics() {
        LocalDate today = LocalDate.now();
        TodayStatisticsVO vo = new TodayStatisticsVO();

        vo.setNewUserCount(userMapper.countTodayNewUsers(today));
        vo.setProductPublishCount(productMapper.countTodayPublish(today));
        vo.setOrderSuccessCount(orderMapper.countTodaySuccess(today));
        vo.setOrderSuccessAmount(orderMapper.sumTodayAmount(today));
        vo.setPendingAuditCount(productMapper.countPendingAudit());
        vo.setPendingAuthCount(userMapper.countPendingAuth());

        // 活跃用户数暂时设为新增用户数，实际应从登录日志统计
        vo.setActiveUserCount(vo.getNewUserCount());

        return vo;
    }

    @Override
    public TrendDataVO getProductTrend(Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<OperationStatistics> statistics = operationStatisticsMapper.selectByDateRange(startDate, endDate);

        TrendDataVO vo = new TrendDataVO();
        List<String> xAxis = new ArrayList<>();
        List<Integer> publishData = new ArrayList<>();
        List<Integer> auditPassData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            xAxis.add(date.format(formatter));

            final LocalDate targetDate = date;
            OperationStatistics stat = statistics.stream()
                    .filter(s -> targetDate.equals(s.getStatDate()))
                    .findFirst()
                    .orElse(null);

            if (stat != null) {
                publishData.add(stat.getProductPublishCount() != null ? stat.getProductPublishCount() : 0);
                // 审核通过数暂时用发布数代替
                auditPassData.add(stat.getProductPublishCount() != null ? stat.getProductPublishCount() : 0);
            } else {
                publishData.add(0);
                auditPassData.add(0);
            }
        }

        vo.setXAxis(xAxis);
        List<TrendDataVO.SeriesData> series = new ArrayList<>();

        TrendDataVO.SeriesData publishSeries = new TrendDataVO.SeriesData();
        publishSeries.setName("发布数量");
        publishSeries.setData(publishData);
        series.add(publishSeries);

        TrendDataVO.SeriesData auditSeries = new TrendDataVO.SeriesData();
        auditSeries.setName("审核通过数量");
        auditSeries.setData(auditPassData);
        series.add(auditSeries);

        vo.setSeries(series);
        return vo;
    }

    @Override
    public TrendDataVO getOrderTrend(Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<OperationStatistics> statistics = operationStatisticsMapper.selectByDateRange(startDate, endDate);

        TrendDataVO vo = new TrendDataVO();
        List<String> xAxis = new ArrayList<>();
        List<Integer> orderCountData = new ArrayList<>();
        List<Integer> orderAmountData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            xAxis.add(date.format(formatter));

            final LocalDate targetDate = date;
            OperationStatistics stat = statistics.stream()
                    .filter(s -> targetDate.equals(s.getStatDate()))
                    .findFirst()
                    .orElse(null);

            if (stat != null) {
                orderCountData.add(stat.getOrderSuccessCount() != null ? stat.getOrderSuccessCount() : 0);
                // 金额转换为整数显示
                orderAmountData.add(stat.getOrderSuccessCount() != null ? stat.getOrderSuccessCount() : 0);
            } else {
                orderCountData.add(0);
                orderAmountData.add(0);
            }
        }

        vo.setXAxis(xAxis);
        List<TrendDataVO.SeriesData> series = new ArrayList<>();

        TrendDataVO.SeriesData countSeries = new TrendDataVO.SeriesData();
        countSeries.setName("成交数量");
        countSeries.setData(orderCountData);
        series.add(countSeries);

        vo.setSeries(series);
        return vo;
    }

    @Override
    public List<CategoryStatisticsVO> getHotCategoryStatistics() {
        // TODO: 实现品类统计查询
        // 当前返回模拟数据
        return Collections.emptyList();
    }

    @Override
    public TrendDataVO getUserGrowthTrend(Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<OperationStatistics> statistics = operationStatisticsMapper.selectByDateRange(startDate, endDate);

        TrendDataVO vo = new TrendDataVO();
        List<String> xAxis = new ArrayList<>();
        List<Integer> newUserData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            xAxis.add(date.format(formatter));

            final LocalDate targetDate = date;
            OperationStatistics stat = statistics.stream()
                    .filter(s -> targetDate.equals(s.getStatDate()))
                    .findFirst()
                    .orElse(null);

            if (stat != null && stat.getNewUserCount() != null) {
                newUserData.add(stat.getNewUserCount());
            } else {
                newUserData.add(0);
            }
        }

        vo.setXAxis(xAxis);
        List<TrendDataVO.SeriesData> series = new ArrayList<>();

        TrendDataVO.SeriesData userSeries = new TrendDataVO.SeriesData();
        userSeries.setName("新增用户");
        userSeries.setData(newUserData);
        series.add(userSeries);

        vo.setSeries(series);
        return vo;
    }

    @Override
    public String exportStatistics(LocalDate startDate, LocalDate endDate) {
        // TODO: 实现Excel导出
        log.info("导出统计数据, startDate: {}, endDate: {}", startDate, endDate);
        return null;
    }

    @Override
    public Map<String, Object> getRangeStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        // 计算日期范围内的统计数据
        List<OperationStatistics> statistics = operationStatisticsMapper.selectByDateRange(startDate, endDate);

        int totalNewUsers = 0;
        int totalProducts = 0;
        int totalOrders = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OperationStatistics stat : statistics) {
            if (stat.getNewUserCount() != null) {
                totalNewUsers += stat.getNewUserCount();
            }
            if (stat.getProductPublishCount() != null) {
                totalProducts += stat.getProductPublishCount();
            }
            if (stat.getOrderSuccessCount() != null) {
                totalOrders += stat.getOrderSuccessCount();
            }
        }

        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("newUserCount", totalNewUsers);
        result.put("productPublishCount", totalProducts);
        result.put("orderSuccessCount", totalOrders);
        result.put("orderSuccessAmount", totalAmount);

        return result;
    }

    @Override
    public Map<String, Object> getSeckillStatistics() {
        Map<String, Object> result = new HashMap<>();

        // TODO: 从数据库查询秒杀统计数据
        // 当前返回模拟数据
        result.put("totalActivities", 0);
        result.put("totalParticipants", 0);
        result.put("totalOrders", 0);
        result.put("successRate", 0.0);

        return result;
    }

    @Override
    public Map<String, Object> getCouponStatistics() {
        Map<String, Object> result = new HashMap<>();

        // TODO: 从数据库查询优惠券统计数据
        // 当前返回模拟数据
        result.put("totalCoupons", 0);
        result.put("issuedCount", 0);
        result.put("usedCount", 0);
        result.put("usageRate", 0.0);

        return result;
    }

    @Override
    public Map<String, Object> getAiAccuracyStatistics() {
        Map<String, Object> result = new HashMap<>();

        // TODO: 从数据库查询AI识别准确率数据
        // 当前返回模拟数据
        result.put("totalRecognitions", 0);
        result.put("correctCount", 0);
        result.put("accuracyRate", 0.0);

        return result;
    }

    @Override
    public Map<String, Object> getUserActivityStatistics(Integer days) {
        Map<String, Object> result = new HashMap<>();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<OperationStatistics> statistics = operationStatisticsMapper.selectByDateRange(startDate, endDate);

        int totalActiveUsers = 0;
        int avgActiveUsers = 0;

        for (OperationStatistics stat : statistics) {
            // 活跃用户数暂时用新增用户数替代
            if (stat.getNewUserCount() != null) {
                totalActiveUsers += stat.getNewUserCount();
            }
        }

        if (!statistics.isEmpty()) {
            avgActiveUsers = totalActiveUsers / statistics.size();
        }

        result.put("days", days);
        result.put("totalActiveUsers", totalActiveUsers);
        result.put("avgActiveUsers", avgActiveUsers);

        return result;
    }
}