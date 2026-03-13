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

    @Schema(description = "支付方式")
    private String payType;

    @Schema(description = "支付状态")
    private String payStatus;

    @Schema(description = "订单状态")
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