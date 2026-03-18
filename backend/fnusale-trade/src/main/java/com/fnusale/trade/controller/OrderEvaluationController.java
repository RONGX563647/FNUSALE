package com.fnusale.trade.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.OrderEvaluationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单评价控制器
 */
@Tag(name = "订单评价管理", description = "订单评价、回复等接口")
@RestController
@RequestMapping("/evaluation")
public class OrderEvaluationController {

    @Operation(summary = "提交评价", description = "买家对订单提交评价")
    @PostMapping
    public Result<Void> submitEvaluation(@Valid @RequestBody OrderEvaluationDTO dto) {
        // TODO: 实现提交评价逻辑
        return Result.success();
    }

    @Operation(summary = "获取订单评价", description = "获取指定订单的评价信息")
    @GetMapping("/order/{orderId}")
    public Result<Object> getByOrderId(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        // TODO: 实现获取订单评价逻辑
        return Result.success();
    }

    @Operation(summary = "获取商品评价列表", description = "获取指定商品的所有评价")
    @GetMapping("/product/{productId}")
    public Result<Object> getByProductId(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取商品评价列表逻辑
        return Result.success();
    }

    @Operation(summary = "卖家回复评价", description = "卖家对评价进行回复")
    @PostMapping("/{evaluationId}/reply")
    public Result<Void> replyEvaluation(
            @Parameter(description = "评价ID") @PathVariable Long evaluationId,
            @Parameter(description = "回复内容") @RequestParam String content) {
        // TODO: 实现卖家回复评价逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的评价列表", description = "获取当前用户发出的评价列表")
    @GetMapping("/my")
    public Result<Object> getMyEvaluations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取我的评价列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取收到的评价", description = "卖家获取收到的评价列表")
    @GetMapping("/received")
    public Result<Object> getReceivedEvaluations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取收到的评价逻辑
        return Result.success();
    }
}