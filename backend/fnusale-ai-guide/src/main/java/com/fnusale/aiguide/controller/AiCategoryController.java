package com.fnusale.aiguide.controller;

import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI分类控制器
 */
@Tag(name = "AI拍照分类", description = "AI识别商品品类相关接口")
@RestController
@RequestMapping("/ai/category")
public class AiCategoryController {

    @Operation(summary = "识别商品品类", description = "上传商品图片识别品类")
    @PostMapping("/recognize")
    public Result<Object> recognizeCategory(
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {
        // TODO: 实现识别商品品类逻辑
        return Result.success();
    }

    @Operation(summary = "批量识别品类", description = "批量上传图片识别品类")
    @PostMapping("/batch-recognize")
    public Result<List<Object>> batchRecognize(
            @Parameter(description = "图片URL列表") @RequestBody List<String> imageUrls) {
        // TODO: 实现批量识别品类逻辑
        return Result.success();
    }

    @Operation(summary = "获取识别历史", description = "获取用户的AI识别历史记录")
    @GetMapping("/history")
    public Result<Object> getHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取识别历史逻辑
        return Result.success();
    }
}