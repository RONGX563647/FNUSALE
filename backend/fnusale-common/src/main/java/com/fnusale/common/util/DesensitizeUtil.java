package com.fnusale.common.util;

/**
 * 数据脱敏工具类
 */
public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 手机号脱敏
     * 13800138000 -> 138****8000
     */
    public static String phone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     * zhangsan@campus.edu.cn -> zhan****@campus.edu.cn
     */
    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        int showLength = Math.min(4, atIndex);
        String prefix = email.substring(0, showLength);
        String suffix = email.substring(atIndex);
        return prefix + "****" + suffix;
    }

    /**
     * 学号/工号脱敏
     * 2021001001 -> ****0001
     */
    public static String studentTeacherId(String id) {
        if (id == null || id.length() <= 4) {
            return id;
        }
        return "****" + id.substring(id.length() - 4);
    }

    /**
     * 姓名脱敏
     * 张三 -> 张*
     * 张三丰 -> 张*丰
     * 欧阳修 -> 欧**
     */
    public static String name(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 身份证号脱敏
     * 110101199001011234 -> 110101********1234
     */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 地址脱敏
     * 北京市海淀区xx路xx号 -> 北京市海淀区***
     */
    public static String address(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "***";
    }
}