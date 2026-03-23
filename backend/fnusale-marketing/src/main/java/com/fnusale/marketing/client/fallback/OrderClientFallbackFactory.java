package com.fnusale.marketing.client.fallback;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.OrderCreateDTO;
import com.fnusale.common.vo.trade.OrderVO;
import com.fnusale.marketing.client.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 订单服务 Feign 降级工厂
 * 当订单服务不可用时提供降级响应
 */
@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        log.warn("订单服务降级触发: {}", cause.getMessage());
        
        return new OrderClient() {
            @Override
            public Result<Long> createSeckillOrder(OrderCreateDTO dto) {
                log.warn("订单服务降级: createSeckillOrder, dto={}", dto);
                return Result.failed("订单服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<OrderVO> getOrderById(Long orderId) {
                log.warn("订单服务降级: getOrderById, orderId={}", orderId);
                return Result.failed("订单服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<OrderVO> getSeckillOrderStatus(Long userId, Long activityId) {
                log.warn("订单服务降级: getSeckillOrderStatus, userId={}, activityId={}", userId, activityId);
                return Result.failed("订单服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Void> cancelSeckillOrder(Long orderId) {
                log.warn("订单服务降级: cancelSeckillOrder, orderId={}", orderId);
                return Result.failed("订单服务暂时不可用，请稍后重试");
            }
        };
    }
}
