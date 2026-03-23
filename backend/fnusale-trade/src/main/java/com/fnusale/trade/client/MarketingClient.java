package com.fnusale.trade.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.entity.Coupon;
import com.fnusale.common.entity.UserCoupon;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 营销服务Feign客户端
 */
@FeignClient(name = "fnusale-marketing", path = "/marketing")
public interface MarketingClient {

    /**
     * 获取优惠券信息
     */
    @GetMapping("/inner/coupon/{couponId}")
    Result<Coupon> getCouponById(@PathVariable("couponId") Long couponId);

    /**
     * 获取用户优惠券信息
     */
    @GetMapping("/inner/user-coupon/{userCouponId}")
    Result<UserCoupon> getUserCouponById(@PathVariable("userCouponId") Long userCouponId);

    /**
     * 验证优惠券是否可用
     */
    @GetMapping("/inner/coupon/validate")
    Result<Boolean> validateCoupon(
            @RequestParam("userId") Long userId,
            @RequestParam("couponId") Long couponId,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("price") BigDecimal price);

    /**
     * 使用优惠券
     */
    @PostMapping("/inner/user-coupon/{userCouponId}/use")
    Result<Void> useCoupon(
            @PathVariable("userCouponId") Long userCouponId,
            @RequestParam("orderId") Long orderId);

    /**
     * 扣减秒杀活动库存（数据库）
     */
    @PostMapping("/inner/seckill/{activityId}/deduct-stock")
    Result<Boolean> deductSeckillStock(@PathVariable("activityId") Long activityId);
    
    /**
     * 重置本地消息状态为待重试（消费者处理失败时调用）
     */
    @PostMapping("/inner/seckill/message/{messageId}/reset")
    Result<Void> resetMessageStatus(@PathVariable("messageId") String messageId);
}