package com.fnusale.admin.controller;

import com.fnusale.admin.service.AdminStatisticsService;
import com.fnusale.common.common.Result;
import com.fnusale.common.vo.admin.CategoryStatisticsVO;
import com.fnusale.common.vo.admin.TodayStatisticsVO;
import com.fnusale.common.vo.admin.TrendDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计控制器
 */
@Tag(name = "数据统计", description = "运营数据统计、报表等接口（管理员）")
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @Operation(summary = "获取今日数据概览", description = "获取今日运营数据概览")
    @GetMapping("/today")
    public Result<TodayStatisticsVO> getTodayStatistics() {
        return Result.success(adminStatisticsService.getTodayStatistics());
    }

    @Operation(summary = "获取商品发布趋势", description = "获取商品发布数量趋势")
    @GetMapping("/product/trend")
    public Result<TrendDataVO> getProductTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(adminStatisticsService.getProductTrend(days));
    }

    @Operation(summary = "获取成交趋势", description = "获取成交数量和金额趋势")
    @GetMapping("/order/trend")
    public Result<TrendDataVO> getOrderTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(adminStatisticsService.getOrderTrend(days));
    }

    @Operation(summary = "获取热门品类统计", description = "获取热门品类统计数据")
    @GetMapping("/category/hot")
    public Result<List<CategoryStatisticsVO>> getHotCategoryStatistics() {
        return Result.success(adminStatisticsService.getHotCategoryStatistics());
    }

    @Operation(summary = "获取用户增长趋势", description = "获取用户注册增长趋势")
    @GetMapping("/user/growth")
    public Result<TrendDataVO> getUserGrowthTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(adminStatisticsService.getUserGrowthTrend(days));
    }

    @Operation(summary = "导出统计报表", description = "导出指定日期范围的统计报表")
    @GetMapping("/export")
    public Result<String> exportStatistics(
            @Parameter(description = "开始日期") @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam LocalDate endDate) {
        String url = adminStatisticsService.exportStatistics(startDate, endDate);
        return Result.success(url);
    }

    @Operation(summary = "获取日期范围统计", description = "获取指定日期范围的统计数据")
    @GetMapping("/range")
    public Result<Map<String, Object>> getRangeStatistics(
            @Parameter(description = "开始日期") @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam LocalDate endDate) {
        return Result.success(adminStatisticsService.getRangeStatistics(startDate, endDate));
    }

    @Operation(summary = "获取秒杀统计", description = "获取秒杀活动统计数据")
    @GetMapping("/seckill")
    public Result<Map<String, Object>> getSeckillStatistics() {
        return Result.success(adminStatisticsService.getSeckillStatistics());
    }

    @Operation(summary = "获取优惠券统计", description = "获取优惠券统计数据")
    @GetMapping("/coupon")
    public Result<Map<String, Object>> getCouponStatistics() {
        return Result.success(adminStatisticsService.getCouponStatistics());
    }

    @Operation(summary = "获取AI识别准确率统计", description = "获取AI识别准确率统计数据")
    @GetMapping("/ai/accuracy")
    public Result<Map<String, Object>> getAiAccuracyStatistics() {
        return Result.success(adminStatisticsService.getAiAccuracyStatistics());
    }

    @Operation(summary = "获取用户活跃度统计", description = "获取用户活跃度统计数据")
    @GetMapping("/user/activity")
    public Result<Map<String, Object>> getUserActivityStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(adminStatisticsService.getUserActivityStatistics(days));
    }
}