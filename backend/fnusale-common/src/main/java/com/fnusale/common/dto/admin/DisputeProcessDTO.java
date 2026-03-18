package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 纠纷处理DTO
 */
@Data
@Schema(description = "纠纷处理请求")
public class DisputeProcessDTO implements Serializable {

    @Schema(description = "处理结果（BUYER_WIN/SELLER_WIN/NEGOTIATE）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理结果不能为空")
    private String processResult;

    @Schema(description = "处理备注", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理备注不能为空")
    private String processRemark;

    @Schema(description = "买家信誉分变化")
    private Integer buyerCreditChange;

    @Schema(description = "卖家信誉分变化")
    private Integer sellerCreditChange;
}