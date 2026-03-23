package com.fnusale.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步任务配置类
 * 配置专用的日志发送线程池
 * 企业级优化：支持动态配置、监控指标
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @Value("${log.async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${log.async.max-pool-size:16}")
    private int maxPoolSize;

    @Value("${log.async.queue-capacity:10000}")
    private int queueCapacity;

    @Value("${log.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${log.async.await-termination-seconds:30}")
    private int awaitTerminationSeconds;

    private static final AtomicLong rejectCount = new AtomicLong(0);

    /**
     * 日志发送专用线程池
     * 企业级配置：
     * - 核心线程数4（避免频繁创建销毁）
     * - 最大线程数16（应对突发流量）
     * - 队列容量10000（缓冲高峰请求）
     * - CallerRunsPolicy（降级到调用线程执行）
     */
    @Bean("logTaskExecutor")
    public Executor logTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("log-sender-");
        executor.setRejectedExecutionHandler((r, e) -> {
            rejectCount.incrementAndGet();
            log.warn("日志线程池队列已满，降级到调用线程执行. rejectCount={}", rejectCount.get());
            if (!e.isShutdown()) {
                r.run();
            }
        });
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.initialize();

        if (meterRegistry != null) {
            new ExecutorServiceMetrics(
                    executor.getThreadPoolExecutor(),
                    "log.task.executor",
                    Tags.empty()
            ).bindTo(meterRegistry);

            meterRegistry.gauge("log.sender.reject.count", rejectCount);
        }

        log.info("日志发送线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    public static long getRejectCount() {
        return rejectCount.get();
    }

    public static void resetCounters() {
        rejectCount.set(0);
    }
}
