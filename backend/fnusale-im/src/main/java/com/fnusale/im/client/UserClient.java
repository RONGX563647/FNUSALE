package com.fnusale.im.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.user.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "fnusale-user", path = "/user", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/inner/{userId}")
    Result<UserVO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 检查用户认证状态
     */
    @GetMapping("/inner/{userId}/auth-status")
    Result<String> getAuthStatus(@PathVariable("userId") Long userId);

    /**
     * 批量获取用户信息
     */
    @PostMapping("/inner/batch")
    Result<Map<Long, UserVO>> getUsersByIds(@RequestBody List<Long> userIds);

    /**
     * 批量获取用户认证状态
     */
    @PostMapping("/inner/auth-status/batch")
    Result<Map<Long, String>> getAuthStatusByIds(@RequestBody List<Long> userIds);
}