package com.fnusale.common.aspect;

import com.fnusale.common.annotation.RateLimit;
import com.fnusale.common.cache.RedisService;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * API限流切面
 * 基于Redis实现滑动窗口限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisService redisService;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Around("@annotation(com.fnusale.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = buildKey(rateLimit, method);

        if (!tryAcquire(key, rateLimit.maxRequests(), rateLimit.windowSeconds())) {
            log.warn("接口限流触发: key={}, maxRequests={}, windowSeconds={}", 
                    key, rateLimit.maxRequests(), rateLimit.windowSeconds());
            throw new BusinessException(429, rateLimit.message());
        }

        return point.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildKey(RateLimit rateLimit, Method method) {
        String prefix = rateLimit.key();
        if (prefix.isEmpty()) {
            prefix = method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        }

        String identifier = getIdentifier();
        return RATE_LIMIT_KEY_PREFIX + prefix + ":" + identifier;
    }

    /**
     * 获取限流标识（优先使用用户ID，其次使用IP）
     */
    private String getIdentifier() {
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            return "user:" + userId;
        }

        HttpServletRequest request = getRequest();
        if (request != null) {
            String ip = getClientIp(request);
            return "ip:" + ip;
        }

        return "anonymous";
    }

    /**
     * 尝试获取访问许可（滑动窗口算法）
     */
    private boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - windowSeconds * 1000L;

        String countKey = key + ":count";
        String timestampKey = key + ":timestamps";

        String currentCount = redisService.get(countKey);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;

        if (count < maxRequests) {
            redisService.set(countKey, String.valueOf(count + 1), windowSeconds, TimeUnit.SECONDS);
            return true;
        }

        return false;
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
