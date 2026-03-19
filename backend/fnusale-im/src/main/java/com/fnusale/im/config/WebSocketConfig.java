package com.fnusale.im.config;

import com.fnusale.im.websocket.ImWebSocketHandler;
import com.fnusale.im.websocket.ImWebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ImWebSocketHandler webSocketHandler;
    private final ImWebSocketInterceptor webSocketInterceptor;

    public WebSocketConfig(ImWebSocketHandler webSocketHandler, ImWebSocketInterceptor webSocketInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.webSocketInterceptor = webSocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/im")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}