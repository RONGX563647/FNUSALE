package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum OperateType {

    ADD("ADD", "新增"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    QUERY("QUERY", "查询");

    private final String code;
    private final String desc;
}