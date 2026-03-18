package com.fnusale.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    TEXT("TEXT", "文字"),
    IMAGE("IMAGE", "图片"),
    VOICE("VOICE", "语音");

    private final String code;
    private final String desc;
}