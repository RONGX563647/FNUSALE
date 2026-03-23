package com.fnusale.gateway.filter;

import com.fnusale.common.log.LogConstants;
import com.fnusale.common.log.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关TraceId传递过滤器
 * 在请求进入网关时生成或获取TraceId，并传递给下游微服务
 * 企业级优化：TraceId格式校验，防止注入攻击
 */
@Slf4j
@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "gatewayStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String traceId = request.getHeaders().getFirst(LogConstants.TRACE_ID_HEADER);
        String spanId = request.getHeaders().getFirst(LogConstants.SPAN_ID_HEADER);

        if (!TraceContext.isValidTraceId(traceId)) {
            traceId = TraceContext.generateTraceId();
        }
        if (!TraceContext.isValidSpanId(spanId)) {
            spanId = TraceContext.generateSpanId();
        }

        String clientIp = getClientIp(request);

        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        final String finalTraceId = traceId;
        final String finalSpanId = spanId;

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(LogConstants.TRACE_ID_HEADER, traceId)
                .header(LogConstants.SPAN_ID_HEADER, spanId)
                .header("X-Client-Ip", clientIp)
                .build();

        log.debug("Gateway TraceId: {}, SpanId: {}, ClientIp: {}, Path: {}",
                traceId, spanId, clientIp, request.getPath());

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = response.getHeaders();
                    headers.add(LogConstants.TRACE_ID_HEADER, finalTraceId);
                    headers.add(LogConstants.SPAN_ID_HEADER, finalSpanId);

                    Long startTime = exchange.getAttribute(START_TIME_ATTR);
                    if (startTime != null) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.debug("Gateway TraceId: {}, Duration: {}ms, Status: {}",
                                finalTraceId, duration, response.getStatusCode());
                    }
                }));
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if (ip != null && ip.contains(":")) {
            ip = ip.substring(0, ip.indexOf(":"));
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
