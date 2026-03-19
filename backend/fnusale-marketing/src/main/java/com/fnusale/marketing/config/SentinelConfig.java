package com.fnusale.marketing.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

/**
 * Sentinel 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentinelConfig {

    private final ObjectMapper objectMapper;

    /**
     * 自定义限流异常处理
     */
    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        return (HttpServletRequest request, HttpServletResponse response, BlockException e) -> {
            log.warn("Sentinel 限流触发: uri={}, exception={}", request.getRequestURI(), e.getClass().getSimpleName());

            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Result<?> result;
            if (e instanceof FlowException) {
                result = Result.failed(429, "系统繁忙，请稍后再试");
            } else if (e instanceof DegradeException) {
                result = Result.failed(503, "服务降级中，请稍后再试");
            } else if (e instanceof ParamFlowException) {
                result = Result.failed(429, "访问频率过高，请稍后再试");
            } else {
                result = Result.failed(429, "系统繁忙，请稍后再试");
            }

            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
}