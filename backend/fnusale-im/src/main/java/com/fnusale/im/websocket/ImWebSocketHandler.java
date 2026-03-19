package com.fnusale.im.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final OnlineUserManager onlineUserManager;

    // 存储会话：sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立，sessionId: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);

        try {
            Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
            String type = (String) msgMap.get("type");

            switch (type) {
                case "ping":
                    handlePing(session);
                    break;
                case "auth":
                    handleAuth(session, msgMap);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendMessage(session, Map.of("type", "error", "message", "消息处理失败"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        // 移除在线用户
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            onlineUserManager.removeOnlineUser(userId);
            log.info("用户下线，userId: {}", userId);
        }

        log.info("WebSocket连接关闭，sessionId: {}, status: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误，sessionId: {}", session.getId(), exception);
    }

    /**
     * 处理心跳消息
     */
    private void handlePing(WebSocketSession session) throws IOException {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            onlineUserManager.refreshUserOnline(userId);
        }
        sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
    }

    /**
     * 处理认证消息
     */
    private void handleAuth(WebSocketSession session, Map<String, Object> msgMap) throws IOException {
        String token = (String) msgMap.get("token");
        if (token == null || token.isEmpty()) {
            sendMessage(session, Map.of("type", "auth_result", "success", false, "message", "token不能为空"));
            return;
        }

        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (!JwtUtil.validateToken(token)) {
            sendMessage(session, Map.of("type", "auth_result", "success", false, "message", "token无效或已过期"));
            return;
        }

        Long userId = JwtUtil.getUserId(token);
        if (userId == null) {
            sendMessage(session, Map.of("type", "auth_result", "success", false, "message", "token解析失败"));
            return;
        }

        // 保存在线状态
        session.getAttributes().put("userId", userId);
        onlineUserManager.addOnlineUser(userId, session.getId());

        log.info("WebSocket认证成功，userId: {}", userId);
        sendMessage(session, Map.of("type", "auth_result", "success", true, "message", "认证成功"));
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, Map<String, Object> message) {
        String wsSessionId = onlineUserManager.getSessionId(userId);
        if (wsSessionId == null) {
            log.debug("用户不在线，userId: {}", userId);
            return;
        }

        WebSocketSession session = sessions.get(wsSessionId);
        if (session == null || !session.isOpen()) {
            log.debug("WebSocket会话不存在或已关闭，userId: {}", userId);
            return;
        }

        try {
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("发送消息失败，userId: {}", userId, e);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }
}