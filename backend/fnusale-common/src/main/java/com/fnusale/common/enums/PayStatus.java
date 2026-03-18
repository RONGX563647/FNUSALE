package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PayStatus {

    UNPAID("UNPAID", "未支付"),
    PAID("PAID", "已支付"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String desc;
}