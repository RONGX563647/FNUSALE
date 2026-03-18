package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志模块枚举
 */
@Getter
@AllArgsConstructor
public enum LogModule {

    USER("USER", "用户模块"),
    PRODUCT("PRODUCT", "商品模块"),
    ORDER("ORDER", "订单模块"),
    MARKETING("MARKETING", "营销模块"),
    SYSTEM("SYSTEM", "系统模块");

    private final String code;
    private final String desc;
}