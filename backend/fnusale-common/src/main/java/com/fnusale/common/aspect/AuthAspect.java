package com.fnusale.common.aspect;

import com.fnusale.common.annotation.RequireAdmin;
import com.fnusale.common.annotation.RequireLogin;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面
 */
@Slf4j
@Aspect
@Component
public class AuthAspect {

    /**
     * 校验管理员权限
     */
    @Before("@within(requireAdmin) || @annotation(requireAdmin)")
    public void checkAdmin(RequireAdmin requireAdmin) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            log.warn("管理员接口未登录访问");
            throw new BusinessException(401, "请先登录");
        }

        if (!UserContext.isAdmin()) {
            log.warn("非管理员用户访问管理员接口, userId={}", userId);
            throw new BusinessException(403, "无权限访问");
        }

        log.debug("管理员权限校验通过, userId={}", userId);
    }

    /**
     * 校验用户登录
     */
    @Before("@within(requireLogin) || @annotation(requireLogin)")
    public void checkLogin(RequireLogin requireLogin) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            log.warn("接口未登录访问");
            throw new BusinessException(401, "请先登录");
        }

        log.debug("登录校验通过, userId={}", userId);
    }
}