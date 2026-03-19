package com.fnusale.common.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付创建DTO
 */
@Data
@Schema(description = "支付创建请求")
public class PaymentCreateDTO implements Serializable {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "支付方式（WECHAT-微信，ALIPAY-支付宝，CAMPUS_CARD-校园卡）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"WECHAT", "ALIPAY", "CAMPUS_CARD"})
    @NotNull(message = "支付方式不能为空")
    private String payType;

    @Schema(description = "客户端IP地址")
    private String clientIp;

    @Schema(description = "用户代理")
    private String userAgent;
}