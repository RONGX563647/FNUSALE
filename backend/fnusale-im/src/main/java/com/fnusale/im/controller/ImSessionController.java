package com.fnusale.im.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 */
@Tag(name = "聊天会话管理", description = "聊天会话的创建、查询等接口")
@RestController
@RequestMapping("/session")
public class ImSessionController {

    @Operation(summary = "获取会话列表", description = "获取当前用户的所有聊天会话")
    @GetMapping("/list")
    public Result<List<Object>> getSessionList() {
        // TODO: 实现获取会话列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取会话详情", description = "根据会话ID获取详细信息")
    @GetMapping("/{sessionId}")
    public Result<Object> getSessionById(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现获取会话详情逻辑
        return Result.success();
    }

    @Operation(summary = "创建会话", description = "与指定用户创建新会话（基于商品）")
    @PostMapping("/create")
    public Result<Long> createSession(
            @Parameter(description = "对方用户ID") @RequestParam Long targetUserId,
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        // TODO: 实现创建会话逻辑
        return Result.success();
    }

    @Operation(summary = "获取或创建会话", description = "获取与指定用户的会话，不存在则创建")
    @GetMapping("/get-or-create")
    public Result<Long> getOrCreateSession(
            @Parameter(description = "对方用户ID") @RequestParam Long targetUserId,
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        // TODO: 实现获取或创建会话逻辑
        return Result.success();
    }

    @Operation(summary = "删除会话", description = "删除指定会话")
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现删除会话逻辑
        return Result.success();
    }

    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        // TODO: 实现获取未读消息数逻辑
        return Result.success();
    }

    @Operation(summary = "标记会话已读", description = "将会话标记为已读")
    @PutMapping("/{sessionId}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现标记会话已读逻辑
        return Result.success();
    }

    @Operation(summary = "获取会话消息列表", description = "分页获取会话的消息记录")
    @GetMapping("/{sessionId}/messages")
    public Result<PageResult<Object>> getMessages(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        // TODO: 实现获取会话消息列表逻辑
        return Result.success();
    }

    @Operation(summary = "置顶会话", description = "将指定会话置顶")
    @PutMapping("/{sessionId}/pin")
    public Result<Void> pinSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现置顶会话逻辑
        return Result.success();
    }

    @Operation(summary = "取消置顶", description = "取消会话置顶")
    @DeleteMapping("/{sessionId}/pin")
    public Result<Void> unpinSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        // TODO: 实现取消置顶逻辑
        return Result.success();
    }
}