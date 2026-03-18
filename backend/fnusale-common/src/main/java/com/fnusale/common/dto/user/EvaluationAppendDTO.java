package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 追加评价DTO
 */
@Data
@Schema(description = "追加评价请求")
public class EvaluationAppendDTO implements Serializable {

    @Schema(description = "追加评价内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "追加评价内容不能为空")
    private String appendContent;

    @Schema(description = "追加评价图片地址")
    private String appendImageUrl;
}