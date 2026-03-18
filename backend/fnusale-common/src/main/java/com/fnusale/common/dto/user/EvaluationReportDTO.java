package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 举报评价DTO
 */
@Data
@Schema(description = "举报评价请求")
public class EvaluationReportDTO implements Serializable {

    @Schema(description = "举报原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报原因不能为空")
    private String reportReason;

    @Schema(description = "举报说明")
    private String reportDesc;
}