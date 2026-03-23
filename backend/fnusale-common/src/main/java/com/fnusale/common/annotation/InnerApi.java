package com.fnusale.common.annotation;

import java.lang.annotation.*;

/**
 * 内部API注解
 * 标记该接口仅允许内部服务调用
 * 需要配合网关或拦截器验证内部调用凭证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InnerApi {
    
    /**
     * 是否验证内部调用凭证
     * 默认需要验证
     */
    boolean value() default true;
}
