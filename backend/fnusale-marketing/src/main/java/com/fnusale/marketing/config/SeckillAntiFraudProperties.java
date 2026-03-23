package com.fnusale.marketing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 秒杀防刷配置（v4优化：提取魔法数字到配置文件）
 */
@Data
@Component
@ConfigurationProperties(prefix = "seckill.anti-fraud")
public class SeckillAntiFraudProperties {

    /**
     * IP限流配置
     */
    private IpLimit ipLimit = new IpLimit();

    /**
     * 验证码配置
     */
    private Captcha captcha = new Captcha();

    /**
     * 用户失败配置
     */
    private UserFail userFail = new UserFail();

    @Data
    public static class IpLimit {
        /**
         * IP限流阈值（同一IP在时间窗口内最大请求数）
         */
        private int threshold = 10;

        /**
         * IP限流时间窗口（秒）
         */
        private int windowSeconds = 60;
    }

    @Data
    public static class Captcha {
        /**
         * 需要验证码的失败次数阈值
         */
        private int failThreshold = 3;

        /**
         * 验证码有效期（分钟）
         */
        private int expireMinutes = 5;

        /**
         * 验证码长度
         */
        private int length = 4;
    }

    @Data
    public static class UserFail {
        /**
         * 用户失败记录过期时间（小时）
         */
        private int expireHours = 1;
    }
}
