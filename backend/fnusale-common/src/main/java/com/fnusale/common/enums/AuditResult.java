package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核结果枚举
 */
@Getter
@AllArgsConstructor
public enum AuditResult {

    PASS("PASS", "审核通过"),
    REJECT("REJECT", "审核驳回");

    private final String code;
    private final String desc;
}