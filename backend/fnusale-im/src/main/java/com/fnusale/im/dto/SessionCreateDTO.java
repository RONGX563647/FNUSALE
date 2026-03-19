package com.fnusale.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 创建会话DTO
 */
@Data
@Schema(description = "创建会话请求")
public class SessionCreateDTO {

    @NotNull(message = "对方用户ID不能为空")
    @Schema(description = "对方用户ID（商品发布者）", required = true)
    private Long targetUserId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", required = true)
    private Long productId;
}