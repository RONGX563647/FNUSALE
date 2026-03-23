package com.fnusale.gateway.config;

import com.fnusale.gateway.filter.TraceIdGatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类
 */
@Configuration
public class GatewayConfig {

    /**
     * TraceId网关过滤器
     */
    @Bean
    public TraceIdGatewayFilter traceIdGatewayFilter() {
        return new TraceIdGatewayFilter();
    }
}
