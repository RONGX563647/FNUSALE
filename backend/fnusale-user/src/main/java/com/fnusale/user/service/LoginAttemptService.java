package com.fnusale.user.service;

import com.fnusale.common.constant.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * 用于限制登录失败次数，防止暴力破解
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 登录尝试 Key 前缀
     */
    private static final String LOGIN_ATTEMPTS_KEY_PREFIX = "login:attempts:";

    /**
     * 记录登录失败
     *
     * @param account 账号（手机号或邮箱）
     */
    public void recordLoginAttempt(String account) {
        String key = LOGIN_ATTEMPTS_KEY_PREFIX + account;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, UserConstants.LOGIN_LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
        log.warn("登录失败，账号：{}, 失败次数：{}", account, attempts);
    }

    /**
     * 清除登录失败记录
     *
     * @param account 账号
     */
    public void clearLoginAttempts(String account) {
        String key = LOGIN_ATTEMPTS_KEY_PREFIX + account;
        redisTemplate.delete(key);
        log.debug("清除登录失败记录，账号：{}", account);
    }

    /**
     * 检查账号是否被锁定
     *
     * @param account 账号
     * @return true-已锁定，false-未锁定
     */
    public boolean isLocked(String account) {
        String key = LOGIN_ATTEMPTS_KEY_PREFIX + account;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return false;
        }
        try {
            Long attempts = Long.parseLong(attemptsStr);
            return attempts >= UserConstants.MAX_LOGIN_ATTEMPTS;
        } catch (NumberFormatException e) {
            log.error("登录次数格式错误：{}", attemptsStr);
            return false;
        }
    }

    /**
     * 获取剩余登录尝试次数
     *
     * @param account 账号
     * @return 剩余次数
     */
    public Long getRemainingAttempts(String account) {
        String key = LOGIN_ATTEMPTS_KEY_PREFIX + account;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return (long) UserConstants.MAX_LOGIN_ATTEMPTS;
        }
        try {
            Long attempts = Long.parseLong(attemptsStr);
            return Math.max(0, (long) UserConstants.MAX_LOGIN_ATTEMPTS - attempts);
        } catch (NumberFormatException e) {
            log.error("登录次数格式错误：{}", attemptsStr);
            return (long) UserConstants.MAX_LOGIN_ATTEMPTS;
        }
    }

    /**
     * 获取当前失败次数
     *
     * @param account 账号
     * @return 失败次数
     */
    public Long getLoginAttempts(String account) {
        String key = LOGIN_ATTEMPTS_KEY_PREFIX + account;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return 0L;
        }
        try {
            return Long.parseLong(attemptsStr);
        } catch (NumberFormatException e) {
            log.error("登录次数格式错误：{}", attemptsStr);
            return 0L;
        }
    }
}
