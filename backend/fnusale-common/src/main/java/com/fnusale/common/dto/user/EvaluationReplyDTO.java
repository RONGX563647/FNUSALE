package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 卖家回复DTO
 */
@Data
@Schema(description = "卖家回复请求")
public class EvaluationReplyDTO implements Serializable {

    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回复内容不能为空")
    private String replyContent;
}