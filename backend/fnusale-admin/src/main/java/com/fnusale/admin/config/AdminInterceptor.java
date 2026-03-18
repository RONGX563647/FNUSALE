package com.fnusale.admin.config;

import com.fnusale.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员上下文拦截器
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            
            if (JwtUtil.validateToken(token)) {
                Long adminId = JwtUtil.getUserId(token);
                String tokenType = JwtUtil.getTokenType(token);
                
                if (adminId != null && JwtUtil.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                    AdminContext.setAdminId(adminId);
                    log.debug("管理员认证成功, adminId: {}", adminId);
                    return true;
                }
            }
        }

        String adminIdStr = request.getHeader("X-Admin-Id");
        if (adminIdStr != null && !adminIdStr.isEmpty()) {
            try {
                Long adminId = Long.parseLong(adminIdStr);
                AdminContext.setAdminId(adminId);
            } catch (NumberFormatException e) {
                log.warn("无效的管理员ID: {}", adminIdStr);
            }
        }

        if (AdminContext.getAdminId() == null) {
            AdminContext.setAdminId(1L);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        AdminContext.clear();
    }
}
