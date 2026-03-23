package com.fnusale.marketing.service.impl;

import com.fnusale.marketing.config.SeckillAntiFraudProperties;
import com.fnusale.marketing.service.SeckillAntiFraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀防刷服务实现
 * 
 * 防刷策略：
 * 1. IP限流：同一IP在指定时间内请求次数超过阈值则限制
 * 2. 验证码：用户连续失败多次后需要验证码
 * 3. 用户行为分析：异常请求模式检测
 * 
 * v4优化：
 * - 使用Lua脚本实现IP限流的原子操作
 * - 避免increment和expire非原子问题
 * - 提取魔法数字到配置文件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillAntiFraudServiceImpl implements SeckillAntiFraudService {

    private final StringRedisTemplate redisTemplate;
    private final SeckillAntiFraudProperties properties;

    /**
     * IP限流 Key 前缀
     */
    private static final String IP_LIMIT_PREFIX = "seckill:anti:ip:";
    
    /**
     * 验证码 Key 前缀
     */
    private static final String CAPTCHA_PREFIX = "seckill:anti:captcha:";
    
    /**
     * 用户失败次数 Key 前缀
     */
    private static final String USER_FAIL_PREFIX = "seckill:anti:fail:";

    /**
     * IP限流Lua脚本（原子操作）
     */
    private DefaultRedisScript<Long> ipRateLimitScript;

    /**
     * 用户失败计数Lua脚本（原子操作，v4优化）
     */
    private DefaultRedisScript<Long> userFailScript;

    @PostConstruct
    public void init() {
        ipRateLimitScript = new DefaultRedisScript<>();
        ipRateLimitScript.setScriptText("""
            local current = redis.call('GET', KEYS[1])
            if current == false then
                redis.call('SET', KEYS[1], 1, 'EX', ARGV[2])
                return 1
            end
            if tonumber(current) >= tonumber(ARGV[1]) then
                return 0
            end
            redis.call('INCR', KEYS[1])
            return 1
            """);
        ipRateLimitScript.setResultType(Long.class);

        userFailScript = new DefaultRedisScript<>();
        userFailScript.setScriptText("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """);
        userFailScript.setResultType(Long.class);
    }

    @Override
    public boolean isIpLimited(String ip, Long activityId) {
        String key = IP_LIMIT_PREFIX + activityId + ":" + ip;
        String count = redisTemplate.opsForValue().get(key);
        int threshold = properties.getIpLimit().getThreshold();
        if (count != null && Integer.parseInt(count) >= threshold) {
            log.warn("IP被限制: ip={}, activityId={}, count={}", ip, activityId, count);
            return true;
        }
        return false;
    }

    @Override
    public void recordIpAccess(String ip, Long activityId) {
        String key = IP_LIMIT_PREFIX + activityId + ":" + ip;
        int threshold = properties.getIpLimit().getThreshold();
        int windowSeconds = properties.getIpLimit().getWindowSeconds();
        Long result = redisTemplate.execute(
            ipRateLimitScript,
            java.util.Collections.singletonList(key),
            String.valueOf(threshold),
            String.valueOf(windowSeconds)
        );
        log.debug("记录IP访问: ip={}, activityId={}, result={}", ip, activityId, result);
    }

    @Override
    public boolean verifyCaptcha(String captchaKey, String captchaCode) {
        String key = CAPTCHA_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode != null && storedCode.equals(captchaCode)) {
            redisTemplate.delete(key);
            return true;
        }
        log.warn("验证码验证失败: captchaKey={}", captchaKey);
        return false;
    }

    @Override
    public String generateCaptchaKey(Long userId, Long activityId) {
        String captchaKey = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String key = CAPTCHA_PREFIX + captchaKey;
        String captchaCode = generateSimpleCaptcha();
        int expireMinutes = properties.getCaptcha().getExpireMinutes();
        redisTemplate.opsForValue().set(key, captchaCode, expireMinutes, TimeUnit.MINUTES);
        log.debug("生成验证码: captchaKey={}, code={}", captchaKey, captchaCode);  // v4修复：改为debug级别
        return captchaKey;
    }

    @Override
    public boolean needCaptcha(Long userId, Long activityId) {
        String failKey = USER_FAIL_PREFIX + activityId + ":" + userId;
        String failCount = redisTemplate.opsForValue().get(failKey);
        int threshold = properties.getCaptcha().getFailThreshold();
        return failCount != null && Integer.parseInt(failCount) >= threshold;
    }
    
    /**
     * 记录用户秒杀失败（v4优化：使用Lua脚本原子操作）
     */
    public void recordUserFail(Long userId, Long activityId) {
        String failKey = USER_FAIL_PREFIX + activityId + ":" + userId;
        int expireSeconds = properties.getUserFail().getExpireHours() * 3600;
        Long count = redisTemplate.execute(
            userFailScript,
            java.util.Collections.singletonList(failKey),
            String.valueOf(expireSeconds)
        );
        log.info("记录用户失败: userId={}, activityId={}, failCount={}", userId, activityId, count);
    }
    
    /**
     * 清除用户失败记录
     */
    public void clearUserFail(Long userId, Long activityId) {
        String failKey = USER_FAIL_PREFIX + activityId + ":" + userId;
        redisTemplate.delete(failKey);
    }
    
    /**
     * 生成简单验证码（4位数字）
     */
    private String generateSimpleCaptcha() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
    }
}
