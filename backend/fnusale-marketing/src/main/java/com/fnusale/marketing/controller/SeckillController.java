package com.fnusale.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.marketing.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀活动控制器
 */
@Tag(name = "秒杀活动管理", description = "秒杀活动、秒杀商品等接口")
@RestController
@RequestMapping("/marketing/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取秒杀活动列表", description = "获取当前进行中和即将开始的秒杀活动")
    @GetMapping("/list")
    public Result<List<SeckillActivityVO>> getSeckillList() {
        Long userId = UserContext.getCurrentUserId();
        List<SeckillActivityVO> activities = seckillService.getSeckillList(userId);
        return Result.success(activities);
    }

    @Operation(summary = "获取秒杀活动详情", description = "获取秒杀活动详细信息")
    @GetMapping("/{activityId}")
    public Result<SeckillActivityVO> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        SeckillActivityVO activity = seckillService.getActivityDetail(activityId);
        return Result.success(activity);
    }

    @Operation(summary = "获取秒杀商品详情", description = "获取秒杀商品的详细信息")
    @GetMapping("/product/{productId}")
    public Result<Object> getSeckillProductDetail(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Object product = seckillService.getSeckillProductDetail(productId);
        return Result.success(product);
    }

    @Operation(summary = "参与秒杀", description = "参与秒杀抢购")
    @PostMapping("/{activityId}/join")
    public Result<Long> joinSeckill(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        Long userId = UserContext.getUserIdOrThrow();
        Long orderId = seckillService.joinSeckill(userId, activityId);
        return Result.success("秒杀请求已提交", orderId);
    }

    @Operation(summary = "获取秒杀结果", description = "查询秒杀抢购结果")
    @GetMapping("/{activityId}/result")
    public Result<SeckillResultVO> getSeckillResult(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        Long userId = UserContext.getUserIdOrThrow();
        SeckillResultVO result = seckillService.getSeckillResult(userId, activityId);
        return Result.success(result);
    }

    @Operation(summary = "创建秒杀活动", description = "创建新的秒杀活动（管理员）")
    @PostMapping
    public Result<Void> createActivity(@Valid @RequestBody SeckillActivityDTO dto) {
        seckillService.createActivity(dto);
        return Result.success("创建成功", null);
    }

    @Operation(summary = "更新秒杀活动", description = "更新秒杀活动信息（管理员）")
    @PutMapping("/{activityId}")
    public Result<Void> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Valid @RequestBody SeckillActivityDTO dto) {
        seckillService.updateActivity(activityId, dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除秒杀活动", description = "删除秒杀活动（管理员）")
    @DeleteMapping("/{activityId}")
    public Result<Void> deleteActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        seckillService.deleteActivity(activityId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "分页查询秒杀活动", description = "分页查询秒杀活动列表（管理员）")
    @GetMapping("/page")
    public Result<PageResult<SeckillActivityVO>> getActivityPage(
            @Parameter(description = "活动状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<SeckillActivityVO> page = seckillService.getActivityPage(status, pageNum, pageSize);
        PageResult<SeckillActivityVO> result = PageResult.of(pageNum, pageSize, page.getTotal(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取今日秒杀", description = "获取今日的秒杀活动时间表")
    @GetMapping("/today")
    public Result<List<TodaySeckillVO>> getTodaySeckills() {
        Long userId = UserContext.getCurrentUserId();
        List<TodaySeckillVO> activities = seckillService.getTodaySeckills(userId);
        return Result.success(activities);
    }

    @Operation(summary = "获取秒杀时段", description = "获取秒杀活动的时间段列表")
    @GetMapping("/time-slots")
    public Result<List<String>> getTimeSlots() {
        List<String> timeSlots = seckillService.getTimeSlots();
        return Result.success(timeSlots);
    }

    @Operation(summary = "设置秒杀提醒", description = "设置秒杀开始前的提醒")
    @PostMapping("/{activityId}/reminder")
    public Result<Void> setReminder(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        Long userId = UserContext.getUserIdOrThrow();
        seckillService.setReminder(userId, activityId);
        return Result.success("提醒设置成功", null);
    }

    @Operation(summary = "取消秒杀提醒", description = "取消秒杀提醒")
    @DeleteMapping("/{activityId}/reminder")
    public Result<Void> cancelReminder(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        Long userId = UserContext.getUserIdOrThrow();
        seckillService.cancelReminder(userId, activityId);
        return Result.success("取消成功", null);
    }
}