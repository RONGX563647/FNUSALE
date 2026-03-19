package com.fnusale.common.annotation;

import java.lang.annotation.*;

/**
 * API限流注解
 * 用于标记需要进行限流的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 时间窗口内最大请求数
     */
    int maxRequests() default 10;

    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 1;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
