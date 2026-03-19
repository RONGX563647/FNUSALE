package com.fnusale.im.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign降级处理
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public Result<UserVO> getUserById(Long userId) {
        log.warn("用户服务不可用，getUserById降级处理，userId: {}", userId);
        return Result.failed("用户服务暂时不可用");
    }

    @Override
    public Result<String> getAuthStatus(Long userId) {
        log.warn("用户服务不可用，getAuthStatus降级处理，userId: {}", userId);
        return Result.failed("用户服务暂时不可用");
    }

    @Override
    public Result<Map<Long, UserVO>> getUsersByIds(List<Long> userIds) {
        log.warn("用户服务不可用，getUsersByIds降级处理，userIds: {}", userIds);
        return Result.success(Collections.emptyMap());
    }

    @Override
    public Result<Map<Long, String>> getAuthStatusByIds(List<Long> userIds) {
        log.warn("用户服务不可用，getAuthStatusByIds降级处理，userIds: {}", userIds);
        return Result.success(Collections.emptyMap());
    }
}