package com.fnusale.trade.event;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单事件基类
 */
@Data
@SuperBuilder
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
}