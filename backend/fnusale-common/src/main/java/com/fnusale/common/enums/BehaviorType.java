package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户行为类型枚举
 */
@Getter
@AllArgsConstructor
public enum BehaviorType {

    BROWSE("BROWSE", "浏览"),
    COLLECT("COLLECT", "收藏"),
    LIKE("LIKE", "点赞");

    private final String code;
    private final String desc;
}