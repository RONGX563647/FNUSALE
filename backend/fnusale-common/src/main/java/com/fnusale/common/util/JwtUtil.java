package com.fnusale.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {

    /**
     * 密钥（实际生产环境应从配置文件读取）
     */
    private static final String SECRET = "fnusale-campus-secondhand-trading-platform-jwt-secret-key";

    /**
     * 访问令牌过期时间（2小时）
     */
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L;

    /**
     * 刷新令牌过期时间（7天）
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 令牌类型
     */
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     */
    public static String generateAccessToken(Long userId, String username, String identityType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("identityType", identityType);
        claims.put("type", TOKEN_TYPE_ACCESS);
        return createToken(claims, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 生成刷新令牌
     */
    public static String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", TOKEN_TYPE_REFRESH);
        return createToken(claims, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 创建令牌
     */
    private static String createToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析令牌
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("验证JWT令牌失败", e);
            return false;
        }
    }

    /**
     * 判断令牌是否过期
     */
    private static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从令牌中获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从令牌中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从令牌中获取身份类型
     */
    public static String getIdentityType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("identityType") : null;
    }

    /**
     * 从令牌中获取令牌类型
     */
    public static String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("type") : null;
    }

    /**
     * 获取访问令牌过期时间（秒）
     */
    public static long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION / 1000;
    }
}