package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量审核DTO
 */
@Data
@Schema(description = "批量审核请求")
public class BatchAuditDTO implements Serializable {

    @Schema(description = "商品ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> productIds;
}