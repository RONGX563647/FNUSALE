package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.SignDTO;
import com.fnusale.common.vo.user.SignRecordVO;
import com.fnusale.common.vo.user.SignResultVO;
import com.fnusale.common.vo.user.SignStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签到控制器
 * 提供用户签到、签到记录、补签等接口
 */
@Tag(name = "签到管理", description = "用户签到、签到记录、补签等接口")
@RestController
@RequestMapping("/user/sign")
public class SignController {

    @Operation(summary = "每日签到", description = "用户每日签到一次，获得基础积分和连续签到奖励")
    @PostMapping
    public Result<SignResultVO> sign() {
        // TODO: 实现签到逻辑
        return Result.success();
    }

    @Operation(summary = "查询签到状态", description = "查询今日是否已签到，返回连续签到天数、累计签到天数等信息")
    @GetMapping("/status")
    public Result<SignStatusVO> getSignStatus() {
        // TODO: 实现查询签到状态逻辑
        return Result.success();
    }

    @Operation(summary = "签到统计", description = "获取签到统计信息，包括连续签到天数、累计签到天数、下次奖励等")
    @GetMapping("/statistics")
    public Result<SignStatusVO> getSignStatistics() {
        // TODO: 实现签到统计逻辑
        return Result.success();
    }

    @Operation(summary = "签到记录", description = "获取签到历史记录，按时间倒序排列")
    @GetMapping("/records")
    public Result<PageResult<SignRecordVO>> getSignRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取签到记录逻辑
        return Result.success();
    }

    @Operation(summary = "签到日历", description = "获取指定月份的签到日历，返回已签到的日期列表")
    @GetMapping("/calendar/{month}")
    public Result<List<String>> getSignCalendar(
            @Parameter(description = "月份，格式：yyyy-MM", required = true) @PathVariable String month) {
        // TODO: 实现签到日历逻辑
        return Result.success();
    }

    @Operation(summary = "补签", description = "消耗积分补签遗漏的签到，仅支持补签前7天内的签到，每月最多3次")
    @PostMapping("/repair")
    public Result<SignResultVO> repairSign(
            @Parameter(description = "补签请求", required = true) @Valid @RequestBody SignDTO dto) {
        // TODO: 实现补签逻辑
        return Result.success();
    }
}