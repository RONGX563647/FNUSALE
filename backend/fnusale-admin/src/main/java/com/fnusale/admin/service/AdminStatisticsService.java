package com.fnusale.admin.service;

import com.fnusale.common.vo.admin.CategoryStatisticsVO;
import com.fnusale.common.vo.admin.TodayStatisticsVO;
import com.fnusale.common.vo.admin.TrendDataVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 */
public interface AdminStatisticsService {

    /**
     * 获取今日数据概览
     */
    TodayStatisticsVO getTodayStatistics();

    /**
     * 获取商品发布趋势
     */
    TrendDataVO getProductTrend(Integer days);

    /**
     * 获取成交趋势
     */
    TrendDataVO getOrderTrend(Integer days);

    /**
     * 获取热门品类统计
     */
    List<CategoryStatisticsVO> getHotCategoryStatistics();

    /**
     * 获取用户增长趋势
     */
    TrendDataVO getUserGrowthTrend(Integer days);

    /**
     * 导出统计报表
     */
    String exportStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取日期范围统计数据
     */
    Map<String, Object> getRangeStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取秒杀活动统计
     */
    Map<String, Object> getSeckillStatistics();

    /**
     * 获取优惠券统计
     */
    Map<String, Object> getCouponStatistics();

    /**
     * 获取AI识别准确率统计
     */
    Map<String, Object> getAiAccuracyStatistics();

    /**
     * 获取用户活跃度统计
     */
    Map<String, Object> getUserActivityStatistics(Integer days);
}