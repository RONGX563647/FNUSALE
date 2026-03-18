package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核驳回DTO
 */
@Data
@Schema(description = "审核驳回请求")
public class AuditRejectDTO implements Serializable {

    @Schema(description = "驳回原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "驳回原因不能为空")
    private String reason;
}