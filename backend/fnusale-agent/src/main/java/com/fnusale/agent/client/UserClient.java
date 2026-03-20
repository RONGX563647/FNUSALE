package com.fnusale.agent.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.user.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "fnusale-user", contextId = "userClient")
public interface UserClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/user/inner/{userId}")
    Result<UserVO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 批量获取用户信息
     */
    @PostMapping("/user/inner/batch")
    Result<Map<Long, UserVO>> getUsersByIds(@RequestBody List<Long> userIds);

    /**
     * 获取用户认证状态
     */
    @GetMapping("/user/inner/{userId}/auth-status")
    Result<String> getAuthStatus(@PathVariable("userId") Long userId);
}