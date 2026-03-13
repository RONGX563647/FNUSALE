package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 */
@Getter
@AllArgsConstructor
public enum CouponType {

    FULL_REDUCE("FULL_REDUCE", "满减券"),
    DIRECT_REDUCE("DIRECT_REDUCE", "直降券"),
    CATEGORY("CATEGORY", "品类券");

    private final String code;
    private final String desc;
}