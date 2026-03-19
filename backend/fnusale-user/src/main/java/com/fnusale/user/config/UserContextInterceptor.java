package com.fnusale.user.config;

import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从请求头中获取Token并设置当前用户ID
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                if (JwtUtil.validateToken(token)) {
                    Long userId = JwtUtil.getUserId(token);
                    if (userId != null) {
                        UserContext.setCurrentUserId(userId);
                        String identityType = JwtUtil.getIdentityType(token);
                        if (identityType != null) {
                            UserContext.setCurrentUserRole(identityType);
                        }
                    }
                }
            } catch (Exception e) {
                // Token 无效，忽略
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}