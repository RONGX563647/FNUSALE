package com.fnusale.trade.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 订单退款事件
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderRefundEvent extends OrderEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 支付方式
     */
    private String payType;
}