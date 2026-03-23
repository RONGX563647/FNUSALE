package com.fnusale.marketing.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 * 用于防止缓存穿透
 *
 * 布隆过滤器特点：
 * 1. 判断元素"可能存在"或"一定不存在"
 * 2. 有一定的误判率（可以配置）
 * 3. 空间效率高，查询速度快
 *
 * 应用场景：
 * - 秒杀商品ID过滤：防止恶意请求不存在的商品ID穿透到数据库
 */
@Configuration
public class BloomFilterConfig {

    /**
     * 秒杀商品ID布隆过滤器
     * 预期插入量：10000
     * 误判率：0.01%（万分之一）
     */
    @Bean
    public RBloomFilter<Long> seckillProductBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("seckill:product:bloom");
        // 初始化：预期元素数量10000，误判率0.0001
        bloomFilter.tryInit(10000, 0.0001);
        return bloomFilter;
    }
}