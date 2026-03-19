package com.fnusale.common.util;

import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.exception.BusinessException;

/**
 * 用户校验工具类
 * 提供用户相关数据的校验方法
 */
public class UserValidator {

    private UserValidator() {
    }

    /**
     * 手机号正则表达式
     */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    private static final String EMAIL_REGEX = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";

    /**
     * 校验用户名长度
     *
     * @param username 用户名
     * @throws BusinessException 如果用户名长度不符合要求
     */
    public static void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH ||
            username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new BusinessException("用户名长度应在" + UserConstants.USERNAME_MIN_LENGTH + "-" +
                UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
        }
    }

    /**
     * 校验密码长度
     *
     * @param password 密码
     * @throws BusinessException 如果密码长度不符合要求
     */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return;
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH ||
            password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException("密码长度应在" + UserConstants.PASSWORD_MIN_LENGTH + "-" +
                UserConstants.PASSWORD_MAX_LENGTH + "位之间");
        }
    }

    /**
     * 校验手机号格式
     *
     * @param phone 手机号
     * @throws BusinessException 如果手机号为空或格式不正确
     */
    public static void validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!phone.matches(PHONE_REGEX)) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    /**
     * 校验手机号格式（允许为空）
     *
     * @param phone 手机号
     * @throws BusinessException 如果手机号格式不正确
     */
    public static void validatePhoneIfPresent(String phone) {
        if (phone == null || phone.isEmpty()) {
            return;
        }
        if (!phone.matches(PHONE_REGEX)) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱
     * @throws BusinessException 如果邮箱为空或格式不正确
     */
    public static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    /**
     * 校验邮箱格式（允许为空）
     *
     * @param email 邮箱
     * @throws BusinessException 如果邮箱格式不正确
     */
    public static void validateEmailIfPresent(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    /**
     * 校验定位权限值
     *
     * @param permission 定位权限
     * @throws BusinessException 如果权限值不正确
     */
    public static void validateLocationPermission(String permission) {
        if (!"ALLOW".equals(permission) && !"DENY".equals(permission)) {
            throw new BusinessException("定位权限状态不正确");
        }
    }

    /**
     * 判断是否为手机号格式
     *
     * @param account 账号
     * @return 是否为手机号
     */
    public static boolean isPhone(String account) {
        return account != null && account.matches(PHONE_REGEX);
    }

    /**
     * 判断是否为邮箱格式
     *
     * @param account 账号
     * @return 是否为邮箱
     */
    public static boolean isEmail(String account) {
        return account != null && account.matches(EMAIL_REGEX);
    }
}