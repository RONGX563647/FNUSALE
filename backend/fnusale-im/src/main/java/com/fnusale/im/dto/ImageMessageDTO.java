package com.fnusale.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送图片消息DTO
 */
@Data
@Schema(description = "发送图片消息请求")
public class ImageMessageDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", required = true)
    private Long sessionId;

    @NotBlank(message = "图片URL不能为空")
    @Schema(description = "图片URL", required = true)
    private String imageUrl;
}