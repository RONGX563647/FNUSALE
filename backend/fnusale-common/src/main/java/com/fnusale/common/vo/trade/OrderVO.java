package com.fnusale.common.vo.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO
 */
@Data
@Schema(description = "订单信息")
public class OrderVO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品主图")
    private String productImage;

    @Schema(description = "商品原价")
    private BigDecimal productPrice;

    @Schema(description = "优惠券抵扣金额")
    private BigDecimal couponDeductAmount;

    @Schema(description = "实付金额")
    private BigDecimal actualPayAmount;

    @Schema(description = "自提点名称")
    private String pickPointName;

    @Schema(description = "支付方式（WECHAT-微信，ALIPAY-支付宝，CAMPUS_CARD-校园卡）", allowableValues = {"WECHAT", "ALIPAY", "CAMPUS_CARD"})
    private String payType;

    @Schema(description = "支付状态（UNPAID-未支付，PAID-已支付，REFUNDED-已退款）", allowableValues = {"UNPAID", "PAID", "REFUNDED"})
    private String payStatus;

    @Schema(description = "订单状态（UNPAID-待付款，WAIT_PICK-待自提，SUCCESS-已成交，CANCEL-已取消）", allowableValues = {"UNPAID", "WAIT_PICK", "SUCCESS", "CANCEL"})
    private String orderStatus;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家用户名")
    private String sellerName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "成交时间")
    private LocalDateTime successTime;
}