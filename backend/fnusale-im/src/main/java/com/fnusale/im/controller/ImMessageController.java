package com.fnusale.im.controller;

import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天消息控制器
 */
@Tag(name = "聊天消息管理", description = "发送消息、消息记录等接口")
@RestController
@RequestMapping("/message")
public class ImMessageController {

    @Operation(summary = "发送文字消息", description = "发送文字消息")
    @PostMapping("/text")
    public Result<Void> sendTextMessage(
            @Parameter(description = "会话ID") @RequestParam Long sessionId,
            @Parameter(description = "消息内容") @RequestParam String content) {
        // TODO: 实现发送文字消息逻辑
        return Result.success();
    }

    @Operation(summary = "发送图片消息", description = "发送图片消息")
    @PostMapping("/image")
    public Result<Void> sendImageMessage(
            @Parameter(description = "会话ID") @RequestParam Long sessionId,
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {
        // TODO: 实现发送图片消息逻辑
        return Result.success();
    }

    @Operation(summary = "发送语音消息", description = "发送语音消息")
    @PostMapping("/voice")
    public Result<Void> sendVoiceMessage(
            @Parameter(description = "会话ID") @RequestParam Long sessionId,
            @Parameter(description = "语音URL") @RequestParam String voiceUrl,
            @Parameter(description = "语音时长(秒)") @RequestParam Integer duration) {
        // TODO: 实现发送语音消息逻辑
        return Result.success();
    }

    @Operation(summary = "撤回消息", description = "撤回已发送的消息")
    @DeleteMapping("/{messageId}")
    public Result<Void> recallMessage(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        // TODO: 实现撤回消息逻辑
        return Result.success();
    }

    @Operation(summary = "获取快捷回复列表", description = "获取系统预设的快捷回复模板")
    @GetMapping("/quick-reply/list")
    public Result<Object> getQuickReplyList() {
        // TODO: 实现获取快捷回复列表逻辑
        return Result.success();
    }

    @Operation(summary = "添加快捷回复", description = "添加自定义快捷回复")
    @PostMapping("/quick-reply")
    public Result<Void> addQuickReply(
            @Parameter(description = "回复内容") @RequestParam String content) {
        // TODO: 实现添加快捷回复逻辑
        return Result.success();
    }

    @Operation(summary = "删除快捷回复", description = "删除自定义快捷回复")
    @DeleteMapping("/quick-reply/{id}")
    public Result<Void> deleteQuickReply(
            @Parameter(description = "快捷回复ID") @PathVariable Long id) {
        // TODO: 实现删除快捷回复逻辑
        return Result.success();
    }

    @Operation(summary = "导出聊天记录", description = "导出指定会话的聊天记录")
    @GetMapping("/{sessionId}/export")
    public Result<String> exportChatHistory(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现导出聊天记录逻辑
        return Result.success();
    }

    @Operation(summary = "搜索消息", description = "在会话中搜索消息")
    @GetMapping("/search")
    public Result<Object> searchMessages(
            @Parameter(description = "会话ID") @RequestParam Long sessionId,
            @Parameter(description = "关键词") @RequestParam String keyword) {
        // TODO: 实现搜索消息逻辑
        return Result.success();
    }
}