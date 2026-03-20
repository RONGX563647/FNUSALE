package com.fnusale.agent.controller;

import com.fnusale.agent.dto.AnalysisResult;
import com.fnusale.agent.service.ProductAnalyzeService;
import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agent分析控制器
 *
 * 提供商品分析API：
 * - GET /agent/analyze/{productId} - 商品分析
 * - GET /agent/bargain/{productId} - 议价建议
 */
@Tag(name = "购买分析", description = "商品分析、议价建议相关接口")
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentAnalyzeController {

    private final ProductAnalyzeService productAnalyzeService;

    @Operation(summary = "商品分析", description = "分析商品的价格、卖家信誉、风险等，提供购买建议")
    @GetMapping("/analyze/{productId}")
    public Result<AnalysisResult> analyze(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        // TODO: 实现分析逻辑
        // 1. 获取当前用户ID
        // 2. 调用productAnalyzeService.analyze()
        // 3. 返回分析结果
        return Result.success();
    }

    @Operation(summary = "议价建议", description = "获取商品的议价建议，包括价格区间和话术")
    @GetMapping("/bargain/{productId}")
    public Result<AnalysisResult.BargainSuggestion> getBargainSuggestion(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        AnalysisResult.BargainSuggestion suggestion = productAnalyzeService.getBargainSuggestion(productId);
        return Result.success(suggestion);
    }
}