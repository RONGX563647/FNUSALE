package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 身份类型枚举
 */
@Getter
@AllArgsConstructor
public enum IdentityType {

    STUDENT("STUDENT", "学生"),
    TEACHER("TEACHER", "教职工");

    private final String code;
    private final String desc;
}