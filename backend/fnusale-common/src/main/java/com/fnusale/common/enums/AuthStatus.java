package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证状态枚举
 */
@Getter
@AllArgsConstructor
public enum AuthStatus {

    UNAUTH("UNAUTH", "未认证"),
    UNDER_REVIEW("UNDER_REVIEW", "审核中"),
    AUTH_SUCCESS("AUTH_SUCCESS", "认证成功"),
    AUTH_FAILED("AUTH_FAILED", "认证失败");

    private final String code;
    private final String desc;
}