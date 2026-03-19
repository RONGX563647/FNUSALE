package com.fnusale.trade.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 订单完成事件
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderCompleteEvent extends OrderEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 商品名称
     */
    private String productName;
}