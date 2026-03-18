package com.fnusale.common.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单创建DTO
 */
@Data
@Schema(description = "订单创建请求")
public class OrderCreateDTO implements Serializable {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "优惠券ID（不使用优惠券时可不传）")
    private Long couponId;

    @Schema(description = "自提点ID")
    private Long pickPointId;

    @Schema(description = "支付方式（WECHAT-微信，ALIPAY-支付宝，CAMPUS_CARD-校园卡）", allowableValues = {"WECHAT", "ALIPAY", "CAMPUS_CARD"})
    private String payType;
}