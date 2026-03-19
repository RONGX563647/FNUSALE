package com.fnusale.im.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 在线用户管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineUserManager {

    private final StringRedisTemplate redisTemplate;

    private static final String ONLINE_KEY_PREFIX = "im:online:";
    private static final long ONLINE_EXPIRE_SECONDS = 60;

    // 本地缓存：userId -> sessionId
    private final Map<Long, String> localUserSessionMap = new ConcurrentHashMap<>();

    /**
     * 添加在线用户
     */
    public void addOnlineUser(Long userId, String sessionId) {
        localUserSessionMap.put(userId, sessionId);
        String key = ONLINE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, sessionId, ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("用户上线，userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * 移除在线用户
     */
    public void removeOnlineUser(Long userId) {
        localUserSessionMap.remove(userId);
        String key = ONLINE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("用户下线，userId: {}", userId);
    }

    /**
     * 刷新用户在线状态（心跳续期）
     */
    public void refreshUserOnline(Long userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        redisTemplate.expire(key, ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.debug("刷新用户在线状态，userId: {}", userId);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取用户的WebSocket会话ID
     */
    public String getSessionId(Long userId) {
        return localUserSessionMap.get(userId);
    }
}