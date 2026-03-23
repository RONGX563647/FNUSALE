package com.fnusale.common.config;

import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 用户上下文自动配置
 * 从请求头 Authorization 或 X-User-Id 读取用户信息
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserContextAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/error");
    }

    /**
     * 用户上下文拦截器
     * 支持两种认证方式：
     * 1. Authorization: Bearer <jwt_token> - JWT 认证
     * 2. X-User-Id: <user_id> - 简单用户ID（用于测试环境）
     */
    static class UserContextInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // 1. 尝试从 JWT Token 获取用户信息
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
                } catch (Exception ignored) {
                    // Token 无效，继续尝试其他方式
                }
            }

            // 2. 尝试从 X-User-Id 获取用户信息（测试环境）
            if (!UserContext.isLoggedIn()) {
                String userIdStr = request.getHeader("X-User-Id");
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        Long userId = Long.parseLong(userIdStr);
                        UserContext.setCurrentUserId(userId);
                    } catch (NumberFormatException ignored) {
                    }
                }

                String userRole = request.getHeader("X-User-Role");
                if (userRole != null && !userRole.isEmpty()) {
                    UserContext.setCurrentUserRole(userRole);
                }
            }

            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            UserContext.clear();
        }
    }
}