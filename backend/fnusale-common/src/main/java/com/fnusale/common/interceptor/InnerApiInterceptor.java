package com.fnusale.common.interceptor;

import com.fnusale.common.annotation.InnerApi;
import com.fnusale.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 内部API拦截器
 * 验证内部服务调用凭证，防止外部恶意调用
 */
@Slf4j
public class InnerApiInterceptor implements HandlerInterceptor {

    private static final String INNER_TOKEN_HEADER = "X-Inner-Token";
    private static final String INNER_TOKEN_VALUE = "fnusale-inner-api-secret-2024";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        InnerApi innerApi = handlerMethod.getMethodAnnotation(InnerApi.class);
        
        if (innerApi == null) {
            innerApi = handlerMethod.getBeanType().getAnnotation(InnerApi.class);
        }

        if (innerApi != null && innerApi.value()) {
            String token = request.getHeader(INNER_TOKEN_HEADER);
            
            if (!Objects.equals(INNER_TOKEN_VALUE, token)) {
                log.warn("内部API访问被拒绝: uri={}, remoteAddr={}", 
                        request.getRequestURI(), request.getRemoteAddr());
                throw new BusinessException(403, "无权访问内部接口");
            }
            
            log.debug("内部API访问验证通过: uri={}", request.getRequestURI());
        }

        return true;
    }
}
