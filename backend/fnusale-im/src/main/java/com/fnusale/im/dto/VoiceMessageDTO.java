package com.fnusale.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送语音消息DTO
 */
@Data
@Schema(description = "发送语音消息请求")
public class VoiceMessageDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", required = true)
    private Long sessionId;

    @NotBlank(message = "语音URL不能为空")
    @Schema(description = "语音URL", required = true)
    private String voiceUrl;

    @NotNull(message = "语音时长不能为空")
    @Min(value = 1, message = "语音时长最少1秒")
    @Max(value = 60, message = "语音时长最长60秒")
    @Schema(description = "语音时长（秒）", required = true)
    private Integer duration;
}