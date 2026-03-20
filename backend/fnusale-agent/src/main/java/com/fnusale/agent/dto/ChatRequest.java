package com.fnusale.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对话请求DTO
 */
@Data
@Schema(description = "对话请求")
public class ChatRequest {

    @Schema(description = "用户消息", example = "我想买一个二手耳机，预算200左右")
    private String message;

    @Schema(description = "会话ID，用于多轮对话", example = "session_123")
    private String sessionId;

    @Schema(description = "用户ID", hidden = true)
    private Long userId;
}