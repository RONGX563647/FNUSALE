package com.fnusale.agent.controller;

import com.fnusale.agent.dto.ChatRequest;
import com.fnusale.agent.dto.ChatResponse;
import com.fnusale.agent.service.AgentChatService;
import com.fnusale.common.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agent对话控制器
 *
 * 提供对话式购物助手API：
 * - POST /agent/chat - 对话接口
 * - DELETE /agent/session/{sessionId} - 清除会话
 */
@Tag(name = "智能购物助手", description = "对话式购物助手相关接口")
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentChatService agentChatService;

    @Operation(summary = "对话接口", description = "与智能购物助手进行对话，支持多轮对话和意图理解")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        // TODO: 实现对话逻辑
        // 1. 获取当前用户ID
        // 2. 调用agentChatService.chat()
        // 3. 返回响应
        return Result.success();
    }

    @Operation(summary = "清除会话", description = "清除指定会话的上下文信息")
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        agentChatService.clearSession(sessionId);
        return Result.success();
    }
}