package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    DRAFT("DRAFT", "草稿"),
    ON_SHELF("ON_SHELF", "上架"),
    SOLD_OUT("SOLD_OUT", "已成交"),
    OFF_SHELF("OFF_SHELF", "下架"),
    ILLEGAL("ILLEGAL", "违规");

    private final String code;
    private final String desc;
}