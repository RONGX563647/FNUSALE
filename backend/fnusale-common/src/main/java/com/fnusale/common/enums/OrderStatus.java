package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    UNPAID("UNPAID", "待付款"),
    WAIT_PICK("WAIT_PICK", "待自提"),
    SUCCESS("SUCCESS", "已成交"),
    CANCEL("CANCEL", "已取消");

    private final String code;
    private final String desc;
}