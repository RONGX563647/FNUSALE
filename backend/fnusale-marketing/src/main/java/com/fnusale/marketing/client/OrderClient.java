package com.fnusale.marketing.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.OrderCreateDTO;
import com.fnusale.common.vo.trade.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务 Feign 客户端
 */
@FeignClient(name = "fnusale-trade", path = "/order")
public interface OrderClient {

    /**
     * 创建秒杀订单
     *
     * @param dto 订单创建DTO
     * @return 订单ID
     */
    @PostMapping("/seckill")
    Result<Long> createSeckillOrder(@RequestBody OrderCreateDTO dto);

    /**
     * 根据ID获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    Result<OrderVO> getOrderById(@PathVariable Long orderId);

    /**
     * 根据用户和活动查询秒杀订单状态
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 订单信息
     */
    @GetMapping("/seckill/status")
    Result<OrderVO> getSeckillOrderStatus(@RequestParam Long userId, @RequestParam Long activityId);

    /**
     * 取消秒杀订单（库存回滚）
     *
     * @param orderId 订单ID
     * @return 结果
     */
    @PutMapping("/{orderId}/cancel-seckill")
    Result<Void> cancelSeckillOrder(@PathVariable Long orderId);
}