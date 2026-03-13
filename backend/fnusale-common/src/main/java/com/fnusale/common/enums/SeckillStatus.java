package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀活动状态枚举
 */
@Getter
@AllArgsConstructor
public enum SeckillStatus {

    NOT_START("NOT_START", "未开始"),
    ON_GOING("ON_GOING", "进行中"),
    END("END", "已结束");

    private final String code;
    private final String desc;
}