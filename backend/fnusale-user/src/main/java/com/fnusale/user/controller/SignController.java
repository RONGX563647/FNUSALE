package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.SignDTO;
import com.fnusale.common.enums.ResultCode;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.user.SignRecordVO;
import com.fnusale.common.vo.user.SignResultVO;
import com.fnusale.common.vo.user.SignStatusVO;
import com.fnusale.common.vo.user.UserPointsVO;
import com.fnusale.user.service.UserPointsService;
import com.fnusale.user.service.UserSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签到控制器
 * 提供用户签到、签到记录、补签等接口
 */
@Tag(name = "签到管理", description = "用户签到、签到记录、补签等接口")
@RestController
@RequestMapping("/user/sign")
@RequiredArgsConstructor
public class SignController {

    private final UserSignService userSignService;
    private final UserPointsService userPointsService;

    @Operation(summary = "每日签到", description = "用户每日签到一次，获得基础积分和连续签到奖励")
    @PostMapping
    public Result<SignResultVO> sign() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        SignResultVO result = userSignService.sign(userId);
        return Result.success("签到成功", result);
    }

    @Operation(summary = "查询签到状态", description = "查询今日是否已签到，返回连续签到天数、累计签到天数等信息")
    @GetMapping("/status")
    public Result<SignStatusVO> getSignStatus() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        SignStatusVO status = userSignService.getSignStatus(userId);
        return Result.success(status);
    }

    @Operation(summary = "签到统计", description = "获取签到统计信息，包括连续签到天数、累计签到天数、下次奖励等")
    @GetMapping("/statistics")
    public Result<SignStatusVO> getSignStatistics() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        SignStatusVO status = userSignService.getSignStatistics(userId);
        return Result.success(status);
    }

    @Operation(summary = "签到记录", description = "获取签到历史记录，按时间倒序排列")
    @GetMapping("/records")
    public Result<PageResult<SignRecordVO>> getSignRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        PageResult<SignRecordVO> result = userSignService.getSignRecords(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "签到日历", description = "获取指定月份的签到日历，返回已签到的日期列表")
    @GetMapping("/calendar/{month}")
    public Result<List<String>> getSignCalendar(
            @Parameter(description = "月份，格式：yyyy-MM", required = true) @PathVariable String month) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        List<String> calendar = userSignService.getSignCalendar(userId, month);
        return Result.success(calendar);
    }

    @Operation(summary = "补签", description = "消耗积分补签遗漏的签到，仅支持补签前7天内的签到，每月最多3次")
    @PostMapping("/repair")
    public Result<SignResultVO> repairSign(
            @Parameter(description = "补签请求", required = true) @Valid @RequestBody SignDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        SignResultVO result = userSignService.repairSign(userId, dto);
        return Result.success("补签成功", result);
    }

    @Operation(summary = "获取我的积分", description = "获取当前用户积分信息")
    @GetMapping("/points")
    public Result<UserPointsVO> getMyPoints() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        UserPointsVO points = userPointsService.getUserPoints(userId);
        return Result.success(points);
    }

    @Operation(summary = "获取积分变动记录", description = "获取积分变动历史记录")
    @GetMapping("/points/logs")
    public Result<PageResult<Object>> getPointsLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        PageResult<Object> result = userPointsService.getPointsLogs(userId, pageNum, pageSize);
        return Result.success(result);
    }
}
