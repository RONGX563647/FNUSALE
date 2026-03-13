package com.fnusale.common.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 */
public class MaskUtils {

    private MaskUtils() {
    }

    /**
     * 手机号脱敏
     * 138****8000
     */
    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 学号/工号脱敏
     * 仅显示后4位
     */
    public static String maskStudentId(String studentId) {
        if (StrUtil.isBlank(studentId) || studentId.length() < 4) {
            return studentId;
        }
        return "****" + studentId.substring(studentId.length() - 4);
    }

    /**
     * 邮箱脱敏
     * t***@example.com
     */
    public static String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 姓名脱敏
     * 张*
     */
    public static String maskName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.substring(2);
    }
}