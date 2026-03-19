package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.EvaluationAppendDTO;
import com.fnusale.common.dto.user.EvaluationReplyDTO;
import com.fnusale.common.dto.user.EvaluationReportDTO;
import com.fnusale.common.dto.user.UserEvaluationDTO;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.user.UserEvaluationVO;
import com.fnusale.common.vo.user.UserRatingVO;
import com.fnusale.user.service.UserEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户评价控制器
 * 提供用户评价、评价统计、评价标签等接口
 */
@Tag(name = "用户评价管理", description = "用户评价、评价统计、评价标签等接口")
@RestController
@RequestMapping("/user/evaluation")
@RequiredArgsConstructor
public class UserEvaluationController {

    private final UserEvaluationService userEvaluationService;

    @Operation(summary = "提交评价", description = "交易完成后对对方进行评价，评价时间限制为订单完成后7天内")
    @PostMapping
    public Result<Void> submitEvaluation(
            @Parameter(description = "评价请求", required = true) @Valid @RequestBody UserEvaluationDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        userEvaluationService.submitEvaluation(userId, dto);
        return Result.success("评价成功", null);
    }

    @Operation(summary = "追加评价", description = "在评价后30天内可追加1次评价内容")
    @PostMapping("/{id}/append")
    public Result<Void> appendEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id,
            @Parameter(description = "追加评价请求", required = true) @Valid @RequestBody EvaluationAppendDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        userEvaluationService.appendEvaluation(userId, id, dto);
        return Result.success("追加成功", null);
    }

    @Operation(summary = "卖家回复", description = "卖家对收到的评价进行回复，每个评价只能回复1次")
    @PostMapping("/{id}/reply")
    public Result<Void> replyEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id,
            @Parameter(description = "回复请求", required = true) @Valid @RequestBody EvaluationReplyDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        userEvaluationService.replyEvaluation(userId, id, dto);
        return Result.success("回复成功", null);
    }

    @Operation(summary = "获取用户评价列表", description = "获取用户收到的评价列表，按时间倒序排列")
    @GetMapping("/user/{userId}")
    public Result<PageResult<UserEvaluationVO>> getUserEvaluations(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserEvaluationVO> result = userEvaluationService.getUserEvaluations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我的评价", description = "获取当前用户发出的评价列表")
    @GetMapping("/my")
    public Result<PageResult<UserEvaluationVO>> getMyEvaluations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        PageResult<UserEvaluationVO> result = userEvaluationService.getMyEvaluations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取评价统计", description = "获取用户评价统计数据，包括综合评分、好评率、各星级数量等")
    @GetMapping("/rating/{userId}")
    public Result<UserRatingVO> getUserRating(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        UserRatingVO rating = userEvaluationService.getUserRating(userId);
        return Result.success(rating);
    }

    @Operation(summary = "获取评价标签统计", description = "获取用户收到的评价标签统计，按出现次数排序")
    @GetMapping("/tags/{userId}")
    public Result<List<Map<String, Object>>> getUserTags(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        List<Map<String, Object>> tags = userEvaluationService.getUserTags(userId);
        return Result.success(tags);
    }

    @Operation(summary = "举报评价", description = "举报恶意/虚假评价，管理员将进行审核处理")
    @PostMapping("/{id}/report")
    public Result<Void> reportEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id,
            @Parameter(description = "举报请求", required = true) @Valid @RequestBody EvaluationReportDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        userEvaluationService.reportEvaluation(userId, id, dto);
        return Result.success("举报成功，我们将尽快处理", null);
    }
}