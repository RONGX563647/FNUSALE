package com.fnusale.trade.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 交易纠纷事件
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DisputeEvent extends OrderEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 纠纷类型
     */
    private String disputeType;

    /**
     * 发起者ID
     */
    private Long initiatorId;

    /**
     * 被投诉者ID
     */
    private Long accusedId;

    /**
     * 处理结果
     */
    private String result;
}