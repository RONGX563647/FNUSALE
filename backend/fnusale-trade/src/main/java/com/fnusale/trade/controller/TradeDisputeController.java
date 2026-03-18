package com.fnusale.trade.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.TradeDisputeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 交易纠纷控制器
 */
@Tag(name = "交易纠纷管理", description = "交易纠纷申请、处理等接口")
@RestController
@RequestMapping("/dispute")
public class TradeDisputeController {

    @Operation(summary = "申请纠纷", description = "发起交易纠纷")
    @PostMapping
    public Result<Void> createDispute(@Valid @RequestBody TradeDisputeDTO dto) {
        // TODO: 实现申请纠纷逻辑
        return Result.success();
    }

    @Operation(summary = "获取纠纷详情", description = "获取纠纷详细信息")
    @GetMapping("/{disputeId}")
    public Result<Object> getDisputeById(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId) {
        // TODO: 实现获取纠纷详情逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的纠纷列表", description = "获取当前用户相关的纠纷列表")
    @GetMapping("/my")
    public Result<PageResult<Object>> getMyDisputes(
            @Parameter(description = "纠纷状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取我的纠纷列表逻辑
        return Result.success();
    }

    @Operation(summary = "撤销纠纷", description = "撤销已提交的纠纷申请")
    @DeleteMapping("/{disputeId}")
    public Result<Void> cancelDispute(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId) {
        // TODO: 实现撤销纠纷逻辑
        return Result.success();
    }

    @Operation(summary = "补充证据", description = "补充纠纷证据材料")
    @PostMapping("/{disputeId}/evidence")
    public Result<Void> addEvidence(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId,
            @Parameter(description = "证据材料URL") @RequestParam String evidenceUrl) {
        // TODO: 实现补充证据逻辑
        return Result.success();
    }

    @Operation(summary = "获取纠纷处理记录", description = "获取纠纷的处理过程记录")
    @GetMapping("/{disputeId}/records")
    public Result<Object> getDisputeRecords(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId) {
        // TODO: 实现获取纠纷处理记录逻辑
        return Result.success();
    }
}