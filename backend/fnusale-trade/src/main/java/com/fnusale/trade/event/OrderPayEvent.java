package com.fnusale.trade.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 订单支付事件
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderPayEvent extends OrderEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 优惠券ID（如果使用）
     */
    private Long couponId;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponDeductAmount;
}