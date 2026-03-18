package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 纠纷类型枚举
 */
@Getter
@AllArgsConstructor
public enum DisputeType {

    PRODUCT_NOT_MATCH("PRODUCT_NOT_MATCH", "商品不符"),
    NO_DELIVERY("NO_DELIVERY", "未发货"),
    PRODUCT_DAMAGED("PRODUCT_DAMAGED", "商品损坏"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}