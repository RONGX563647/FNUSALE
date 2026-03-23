package com.fnusale.marketing.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 秒杀服务监控指标
 * 
 * v4新增：
 * - 添加Prometheus监控指标
 * - 监控秒杀请求、库存、成功率等关键指标
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillMetrics {

    private final MeterRegistry meterRegistry;
    private final StringRedisTemplate redisTemplate;

    private final AtomicLong remainingStock = new AtomicLong(0);
    
    private Counter seckillRequestCounter;
    private Counter seckillSuccessCounter;
    private Counter seckillFailCounter;
    private Timer seckillLatencyTimer;
    private Counter stockInsufficientCounter;
    private Counter duplicatePurchaseCounter;

    @jakarta.annotation.PostConstruct
    public void init() {
        seckillRequestCounter = Counter.builder("seckill_request_total")
            .description("秒杀请求总数")
            .tag("result", "all")
            .register(meterRegistry);

        seckillSuccessCounter = Counter.builder("seckill_success_total")
            .description("秒杀成功总数")
            .tag("result", "success")
            .register(meterRegistry);

        seckillFailCounter = Counter.builder("seckill_fail_total")
            .description("秒杀失败总数")
            .tag("result", "fail")
            .register(meterRegistry);

        stockInsufficientCounter = Counter.builder("seckill_stock_insufficient_total")
            .description("库存不足次数")
            .register(meterRegistry);

        duplicatePurchaseCounter = Counter.builder("seckill_duplicate_purchase_total")
            .description("重复购买次数")
            .register(meterRegistry);

        seckillLatencyTimer = Timer.builder("seckill_latency")
            .description("秒杀请求延迟")
            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(meterRegistry);

        Gauge.builder("seckill_remaining_stock", remainingStock, AtomicLong::get)
            .description("剩余库存量")
            .register(meterRegistry);
    }

    public void recordSeckillRequest(boolean success, long latencyMs) {
        seckillRequestCounter.increment();
        seckillLatencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
        
        if (success) {
            seckillSuccessCounter.increment();
        } else {
            seckillFailCounter.increment();
        }
    }

    public void recordStockInsufficient() {
        stockInsufficientCounter.increment();
    }

    public void recordDuplicatePurchase() {
        duplicatePurchaseCounter.increment();
    }

    public void updateRemainingStock(long stock) {
        remainingStock.set(stock);
    }

    public void recordSeckillFail(String reason) {
        seckillFailCounter.increment();
        switch (reason) {
            case "STOCK_INSUFFICIENT":
                stockInsufficientCounter.increment();
                break;
            case "DUPLICATE_PURCHASE":
                duplicatePurchaseCounter.increment();
                break;
        }
    }
}