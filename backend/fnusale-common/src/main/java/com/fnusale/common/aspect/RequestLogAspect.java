package com.fnusale.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.fnusale.common.log.KafkaLogSender;
import com.fnusale.common.log.LogConstants;
import com.fnusale.common.log.LogMessage;
import com.fnusale.common.log.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求日志切面
 * 拦截所有Controller方法，记录请求日志并发送到Kafka
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestLogAspect {

    @Autowired(required = false)
    private KafkaLogSender kafkaLogSender;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Value("${log.request.enabled:true}")
    private boolean logRequestEnabled;

    @Value("${log.request.slow-threshold:1000}")
    private long slowRequestThreshold;

    @Pointcut("execution(* com.fnusale..controller..*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logRequestEnabled) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        String requestUri = request != null ? request.getRequestURI() : "unknown";
        String requestMethod = request != null ? request.getMethod() : "unknown";

        String requestParams = getRequestParams(joinPoint);

        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String responseBody = getResponseBody(result);

            if (kafkaLogSender != null) {
                try {
                    LogMessage logMessage = buildLogMessage(
                            request, response, requestUri, requestMethod,
                            requestParams, responseBody, duration, exception
                    );
                    kafkaLogSender.sendRequestLog(logMessage);
                } catch (Exception e) {
                    log.warn("发送日志到Kafka失败: {}", e.getMessage());
                }
            } else {
                if (exception != null) {
                    log.error("[{}] {} {} - {}ms - ERROR: {}",
                            TraceContext.getTraceId(), requestMethod, requestUri, duration,
                            exception.getMessage());
                } else if (duration > slowRequestThreshold) {
                    log.warn("[{}] {} {} - {}ms - SLOW REQUEST",
                            TraceContext.getTraceId(), requestMethod, requestUri, duration);
                } else {
                    log.debug("[{}] {} {} - {}ms",
                            TraceContext.getTraceId(), requestMethod, requestUri, duration);
                }
            }
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "{}";
            }
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest) {
                    params.put("request", extractRequestParams((HttpServletRequest) arg));
                } else if (arg instanceof HttpServletResponse) {
                    continue;
                } else if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    fileInfo.put("originalFilename", file.getOriginalFilename());
                    fileInfo.put("size", file.getSize());
                    fileInfo.put("contentType", file.getContentType());
                    params.put("file", fileInfo);
                } else {
                    params.put("arg" + i, arg);
                }
            }
            String json = JSON.toJSONString(params);
            if (json.length() > LogConstants.MAX_REQUEST_PARAMS_LENGTH) {
                json = json.substring(0, LogConstants.MAX_REQUEST_PARAMS_LENGTH) + "...";
            }
            return json;
        } catch (Exception e) {
            log.debug("获取请求参数失败: {}", e.getMessage());
            return "{}";
        }
    }

    private Map<String, String> extractRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (isSensitiveParam(paramName)) {
                paramValue = "******";
            }
            params.put(paramName, paramValue);
        }
        return params;
    }

    private boolean isSensitiveParam(String paramName) {
        if (paramName == null) {
            return false;
        }
        String lowerName = paramName.toLowerCase();
        return lowerName.contains("password")
                || lowerName.contains("pwd")
                || lowerName.contains("pass")
                || lowerName.contains("secret")
                || lowerName.contains("token")
                || lowerName.contains("key")
                || lowerName.contains("credential");
    }

    private String getResponseBody(Object result) {
        if (result == null) {
            return null;
        }
        try {
            String json = JSON.toJSONString(result);
            if (json.length() > LogConstants.MAX_RESPONSE_BODY_LENGTH) {
                json = json.substring(0, LogConstants.MAX_RESPONSE_BODY_LENGTH) + "...";
            }
            return json;
        } catch (Exception e) {
            log.debug("序列化响应体失败: {}", e.getMessage());
            return result.toString();
        }
    }

    private LogMessage buildLogMessage(HttpServletRequest request, HttpServletResponse response,
                                        String requestUri, String requestMethod,
                                        String requestParams, String responseBody,
                                        long duration, Throwable exception) {
        LogMessage.LogMessageBuilder builder = LogMessage.builder()
                .traceId(TraceContext.getTraceId())
                .spanId(TraceContext.getSpanId())
                .serviceName(serviceName)
                .level(exception != null ? LogConstants.LEVEL_ERROR : LogConstants.LEVEL_INFO)
                .userId(TraceContext.getUserId())
                .userRole(TraceContext.getUserRole())
                .threadName(Thread.currentThread().getName())
                .timestamp(LocalDateTime.now())
                .requestUri(requestUri)
                .requestMethod(requestMethod)
                .requestParams(requestParams)
                .responseBody(responseBody)
                .clientIp(TraceContext.getClientIp())
                .duration(duration)
                .httpStatus(response != null ? response.getStatus() : null)
                .logType(LogConstants.LOG_TYPE_REQUEST)
                .env(System.getProperty("spring.profiles.active", LogConstants.DEFAULT_ENV));

        if (exception != null) {
            builder.className(exception.getStackTrace().length > 0
                    ? exception.getStackTrace()[0].getClassName() : null);
            builder.methodName(exception.getStackTrace().length > 0
                    ? exception.getStackTrace()[0].getMethodName() : null);
            builder.exception(exception.getClass().getName() + ": " + exception.getMessage());
            String stackTrace = getStackTraceString(exception);
            if (stackTrace.length() > LogConstants.MAX_STACK_TRACE_LENGTH) {
                stackTrace = stackTrace.substring(0, LogConstants.MAX_STACK_TRACE_LENGTH) + "...";
            }
            builder.stackTrace(stackTrace);
        }

        return builder.build();
    }

    private String getStackTraceString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        int maxLines = 50;
        int lineCount = 0;
        for (StackTraceElement element : e.getStackTrace()) {
            if (lineCount >= maxLines) {
                sb.append("\t... ").append(e.getStackTrace().length - maxLines).append(" more\n");
                break;
            }
            sb.append("\tat ").append(element.toString()).append("\n");
            lineCount++;
        }
        return sb.toString();
    }
}
