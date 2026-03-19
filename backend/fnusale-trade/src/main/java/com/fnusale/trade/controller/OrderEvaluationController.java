package com.fnusale.trade.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.OrderEvaluationDTO;
import com.fnusale.common.vo.trade.EvaluationVO;
import com.fnusale.trade.service.OrderEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单评价控制器
 */
@Tag(name = "订单评价管理", description = "订单评价、回复等接口")
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class OrderEvaluationController {

    private final OrderEvaluationService orderEvaluationService;

    @Operation(summary = "提交评价", description = "买家对订单提交评价")
    @PostMapping
    public Result<Void> submitEvaluation(@Valid @RequestBody OrderEvaluationDTO dto) {
        orderEvaluationService.submitEvaluation(dto);
        return Result.success("评价成功", null);
    }

    @Operation(summary = "获取订单评价", description = "获取指定订单的评价信息")
    @GetMapping("/order/{orderId}")
    public Result<EvaluationVO> getByOrderId(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        EvaluationVO evaluation = orderEvaluationService.getByOrderId(orderId);
        return Result.success(evaluation);
    }

    @Operation(summary = "获取商品评价列表", description = "获取指定商品的所有评价")
    @GetMapping("/product/{productId}")
    public Result<PageResult<EvaluationVO>> getByProductId(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<EvaluationVO> result = orderEvaluationService.getByProductId(productId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "卖家回复评价", description = "卖家对评价进行回复")
    @PostMapping("/{evaluationId}/reply")
    public Result<Void> replyEvaluation(
            @Parameter(description = "评价ID") @PathVariable Long evaluationId,
            @Parameter(description = "回复内容") @RequestParam String content) {
        orderEvaluationService.replyEvaluation(evaluationId, content);
        return Result.success("回复成功", null);
    }

    @Operation(summary = "获取我的评价列表", description = "获取当前用户发出的评价列表")
    @GetMapping("/my")
    public Result<PageResult<EvaluationVO>> getMyEvaluations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<EvaluationVO> result = orderEvaluationService.getMyEvaluations(pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取收到的评价", description = "卖家获取收到的评价列表")
    @GetMapping("/received")
    public Result<PageResult<EvaluationVO>> getReceivedEvaluations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<EvaluationVO> result = orderEvaluationService.getReceivedEvaluations(pageNum, pageSize);
        return Result.success(result);
    }
}