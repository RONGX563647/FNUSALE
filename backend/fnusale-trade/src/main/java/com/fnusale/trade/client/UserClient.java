package com.fnusale.trade.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.user.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "fnusale-user", path = "/user")
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
     * 获取用户信誉分
     */
    @GetMapping("/inner/{userId}/credit-score")
    Result<Integer> getCreditScore(@PathVariable("userId") Long userId);

    /**
     * 更新用户评分统计
     */
    @PostMapping("/inner/{userId}/update-rating")
    Result<Void> updateRating(
            @PathVariable("userId") Long userId,
            @RequestParam("score") Integer score);
}