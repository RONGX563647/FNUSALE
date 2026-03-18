package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 纠纷状态枚举
 */
@Getter
@AllArgsConstructor
public enum DisputeStatus {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    RESOLVED("RESOLVED", "已解决");

    private final String code;
    private final String desc;
}