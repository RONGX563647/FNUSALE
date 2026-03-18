package com.fnusale.common.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 交易纠纷DTO
 */
@Data
@Schema(description = "交易纠纷请求")
public class TradeDisputeDTO implements Serializable {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "纠纷类型（PRODUCT_NOT_MATCH-商品不符，NO_DELIVERY-未发货，OTHER-其他）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"PRODUCT_NOT_MATCH", "NO_DELIVERY", "OTHER"})
    @NotBlank(message = "纠纷类型不能为空")
    private String disputeType;

    @Schema(description = "举证材料地址（图片/聊天记录，支持多个，逗号分隔）")
    private String evidenceUrl;
}