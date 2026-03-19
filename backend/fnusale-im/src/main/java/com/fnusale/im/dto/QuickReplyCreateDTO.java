package com.fnusale.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加快捷回复DTO
 */
@Data
@Schema(description = "添加快捷回复请求")
public class QuickReplyCreateDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 50, message = "回复内容不能超过50字")
    @Schema(description = "回复内容", required = true)
    private String content;
}