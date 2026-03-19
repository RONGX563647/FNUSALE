package com.fnusale.trade.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 订单评价事件
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderEvaluationEvent extends OrderEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long evaluationId;

    /**
     * 评分（1-5）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;
}