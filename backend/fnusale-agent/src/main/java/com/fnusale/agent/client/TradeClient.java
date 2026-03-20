package com.fnusale.agent.client;

import com.fnusale.common.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 交易服务Feign客户端
 */
@FeignClient(name = "fnusale-trade", contextId = "tradeClient")
public interface TradeClient {

    /**
     * 获取卖家交易统计
     */
    @GetMapping("/order/inner/seller/{sellerId}/stats")
    Result<SellerStats> getSellerStats(@PathVariable("sellerId") Long sellerId);

    /**
     * 卖家统计信息
     */
    record SellerStats(
            Long sellerId,
            Integer totalOrders,
            Integer successOrders,
            Double successRate,
            Double avgRating
    ) {}
}