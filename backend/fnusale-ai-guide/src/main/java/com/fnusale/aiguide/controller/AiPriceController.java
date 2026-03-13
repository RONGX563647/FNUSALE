package com.fnusale.aiguide.controller;

import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * AI价格参考控制器
 */
@Tag(name = "AI价格参考", description = "AI智能定价参考相关接口")
@RestController
@RequestMapping("/ai/price")
public class AiPriceController {

    @Operation(summary = "获取价格参考", description = "根据品类和新旧程度获取建议价格区间")
    @GetMapping("/reference")
    public Result<Object> getPriceReference(
            @Parameter(description = "品类ID") @RequestParam Long categoryId,
            @Parameter(description = "新旧程度") @RequestParam String newDegree) {
        // TODO: 实现获取价格参考逻辑
        return Result.success();
    }

    @Operation(summary = "获取商品定价建议", description = "根据商品信息获取智能定价建议")
    @PostMapping("/suggest")
    public Result<Object> suggestPrice(@RequestBody Object dto) {
        // TODO: 实现获取商品定价建议逻辑
        return Result.success();
    }

    @Operation(summary = "更新价格参考数据", description = "更新品类价格参考数据（管理员）")
    @PostMapping("/update")
    public Result<Void> updatePriceReference(@RequestBody Object dto) {
        // TODO: 实现更新价格参考数据逻辑
        return Result.success();
    }

    @Operation(summary = "获取价格趋势", description = "获取指定品类的价格趋势")
    @GetMapping("/trend")
    public Result<Object> getPriceTrend(
            @Parameter(description = "品类ID") @RequestParam Long categoryId) {
        // TODO: 实现获取价格趋势逻辑
        return Result.success();
    }

    @Operation(summary = "比价", description = "与同品类商品进行比价")
    @GetMapping("/compare")
    public Result<Object> comparePrice(
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "价格") @RequestParam BigDecimal price) {
        // TODO: 实现比价逻辑
        return Result.success();
    }
}