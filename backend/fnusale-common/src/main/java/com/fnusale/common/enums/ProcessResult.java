package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 纠纷处理结果枚举
 */
@Getter
@AllArgsConstructor
public enum ProcessResult {

    BUYER_WIN("BUYER_WIN", "买家胜诉"),
    SELLER_WIN("SELLER_WIN", "卖家胜诉"),
    NEGOTIATE("NEGOTIATE", "协商解决");

    private final String code;
    private final String desc;
}