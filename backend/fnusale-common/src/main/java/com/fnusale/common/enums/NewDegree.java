package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 新旧程度枚举
 */
@Getter
@AllArgsConstructor
public enum NewDegree {

    NEW("NEW", "全新"),
    NEW_90("90_NEW", "9成新"),
    NEW_80("80_NEW", "8成新"),
    NEW_70("70_NEW", "7成新"),
    OLD("OLD", "老旧");

    private final String code;
    private final String desc;
}