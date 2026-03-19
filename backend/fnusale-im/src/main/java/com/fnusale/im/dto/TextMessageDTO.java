package com.fnusale.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发送文字消息DTO
 */
@Data
@Schema(description = "发送文字消息请求")
public class TextMessageDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", required = true)
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500字")
    @Schema(description = "消息内容", required = true)
    private String content;
}