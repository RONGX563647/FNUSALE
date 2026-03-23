package com.fnusale.im.websocket;

import com.fnusale.common.log.LogConstants;
import com.fnusale.common.log.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 企业级优化：添加TraceId支持，便于日志追踪
 */
@Slf4j
@Component
public class ImWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String traceId = null;
        String clientIp = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            traceId = servletRequest.getServletRequest().getHeader(LogConstants.TRACE_ID_HEADER);
            clientIp = getClientIp(servletRequest);
        }

        if (!TraceContext.isValidTraceId(traceId)) {
            traceId = TraceContext.generateTraceId();
        }

        attributes.put("traceId", traceId);
        attributes.put("clientIp", clientIp);

        TraceContext.init(traceId);
        TraceContext.setClientIp(clientIp);

        log.info("WebSocket握手开始，traceId: {}, URI: {}, clientIp: {}",
                traceId, request.getURI(), clientIp);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        String traceId = TraceContext.getTraceId();

        if (exception != null) {
            log.error("WebSocket握手失败，traceId: {}", traceId, exception);
        } else {
            log.info("WebSocket握手完成，traceId: {}", traceId);
        }

        TraceContext.clear();
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServletServerHttpRequest request) {
        String ip = request.getServletRequest().getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getServletRequest().getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getServletRequest().getHeader("Proxy-Client-IP");
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
}
