package com.fnusale.common.config;

import com.fnusale.common.log.LogConstants;
import com.fnusale.common.log.TraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 * 配置Feign请求拦截器，传递TraceId到下游服务
 */
@Configuration
public class FeignTraceConfig {

    /**
     * Feign请求拦截器
     * 在Feign调用时传递TraceId、SpanId、用户信息等
     */
    @Bean
    public RequestInterceptor feignTraceInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String traceId = TraceContext.getTraceId();
                if (traceId != null && !traceId.isEmpty()) {
                    template.header(LogConstants.TRACE_ID_HEADER, traceId);
                }

                String spanId = TraceContext.getSpanId();
                if (spanId != null && !spanId.isEmpty()) {
                    template.header(LogConstants.SPAN_ID_HEADER, spanId);
                }

                Long userId = TraceContext.getUserId();
                if (userId != null) {
                    template.header(LogConstants.USER_ID_HEADER, userId.toString());
                }

                String userRole = TraceContext.getUserRole();
                if (userRole != null && !userRole.isEmpty()) {
                    template.header(LogConstants.USER_ROLE_HEADER, userRole);
                }

                String clientIp = TraceContext.getClientIp();
                if (clientIp != null && !clientIp.isEmpty()) {
                    template.header("X-Client-Ip", clientIp);
                }
            }
        };
    }
}
