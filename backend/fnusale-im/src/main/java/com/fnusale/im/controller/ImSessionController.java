package com.fnusale.im.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.common.vo.im.SessionVO;
import com.fnusale.im.dto.SessionCreateDTO;
import com.fnusale.im.service.ImSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天会话控制器
 */
@Tag(name = "聊天会话管理", description = "聊天会话的创建、查询等接口")
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class ImSessionController {

    private final ImSessionService sessionService;

    @Operation(summary = "获取会话列表", description = "获取当前用户的所有聊天会话")
    @GetMapping("/list")
    public Result<List<SessionVO>> getSessionList() {
        return Result.success(sessionService.getSessionList());
    }

    @Operation(summary = "获取会话详情", description = "根据会话ID获取详细信息")
    @GetMapping("/{sessionId}")
    public Result<SessionVO> getSessionById(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        return Result.success(sessionService.getSessionById(sessionId));
    }

    @Operation(summary = "创建会话", description = "与指定用户创建新会话（基于商品）")
    @PostMapping("/create")
    public Result<Map<String, Object>> createSession(
            @Valid @RequestBody SessionCreateDTO dto) {
        Long sessionId = sessionService.createSession(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        return Result.success("创建成功", result);
    }

    @Operation(summary = "获取或创建会话", description = "获取与指定用户的会话，不存在则创建")
    @GetMapping("/get-or-create")
    public Result<Long> getOrCreateSession(
            @Parameter(description = "对方用户ID") @RequestParam Long targetUserId,
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        SessionCreateDTO dto = new SessionCreateDTO();
        dto.setTargetUserId(targetUserId);
        dto.setProductId(productId);
        return Result.success(sessionService.getOrCreateSession(dto));
    }

    @Operation(summary = "删除会话", description = "删除指定会话")
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        return Result.success(sessionService.getUnreadCount());
    }

    @Operation(summary = "标记会话已读", description = "将会话标记为已读")
    @PutMapping("/{sessionId}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        sessionService.markAsRead(sessionId);
        return Result.success("操作成功", null);
    }

    @Operation(summary = "获取会话消息列表", description = "分页获取会话的消息记录")
    @GetMapping("/{sessionId}/messages")
    public Result<PageResult<MessageVO>> getMessages(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(sessionService.getMessages(sessionId, pageNum, pageSize));
    }

    @Operation(summary = "置顶会话", description = "将指定会话置顶")
    @PutMapping("/{sessionId}/pin")
    public Result<Void> pinSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        sessionService.pinSession(sessionId);
        return Result.success("置顶成功", null);
    }

    @Operation(summary = "取消置顶", description = "取消会话置顶")
    @DeleteMapping("/{sessionId}/pin")
    public Result<Void> unpinSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        sessionService.unpinSession(sessionId);
        return Result.success("取消置顶成功", null);
    }
}