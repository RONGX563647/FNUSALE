package com.fnusale.aiguide.controller;

import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI智能客服控制器
 */
@Tag(name = "AI智能客服", description = "AI智能问答相关接口")
@RestController
@RequestMapping("/ai/service")
public class AiServiceController {

    @Operation(summary = "智能问答", description = "发送问题获取AI回答")
    @PostMapping("/ask")
    public Result<Object> askQuestion(
            @Parameter(description = "问题内容") @RequestParam String question) {
        // TODO: 实现智能问答逻辑
        return Result.success();
    }

    @Operation(summary = "获取常见问题列表", description = "获取系统预设的常见问题")
    @GetMapping("/faq/list")
    public Result<List<Object>> getFaqList() {
        // TODO: 实现获取常见问题列表逻辑
        return Result.success();
    }

    @Operation(summary = "搜索问题", description = "搜索相关问题")
    @GetMapping("/search")
    public Result<List<Object>> searchQuestion(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        // TODO: 实现搜索问题逻辑
        return Result.success();
    }

    @Operation(summary = "获取问题分类", description = "获取问题分类列表")
    @GetMapping("/categories")
    public Result<List<Object>> getCategories() {
        // TODO: 实现获取问题分类逻辑
        return Result.success();
    }

    @Operation(summary = "转人工客服", description = "转接人工客服")
    @PostMapping("/transfer")
    public Result<Void> transferToHuman() {
        // TODO: 实现转人工客服逻辑
        return Result.success();
    }

    @Operation(summary = "反馈问答结果", description = "用户对问答结果的反馈")
    @PostMapping("/feedback")
    public Result<Void> feedbackAnswer(
            @Parameter(description = "问答ID") @RequestParam Long answerId,
            @Parameter(description = "是否有帮助") @RequestParam Boolean helpful) {
        // TODO: 实现反馈问答结果逻辑
        return Result.success();
    }

    @Operation(summary = "获取聊天历史", description = "获取用户的客服聊天历史")
    @GetMapping("/history")
    public Result<Object> getChatHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取聊天历史逻辑
        return Result.success();
    }
}