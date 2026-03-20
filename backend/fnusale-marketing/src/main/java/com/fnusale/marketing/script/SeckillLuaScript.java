package com.fnusale.marketing.script;

/**
 * 秒杀相关 Redis Lua 脚本
 * 用于保证原子性操作
 */
public class SeckillLuaScript {

    private SeckillLuaScript() {
    }

    /**
     * 原子扣减库存脚本
     * 返回值说明：
     * -1: 库存未预热（key不存在）
     *  0: 库存不足
     *  1: 扣减成功
     */
    public static final String DECREMENT_STOCK_SCRIPT = """
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                return -1
            end
            local remain = tonumber(stock)
            if remain <= 0 then
                return 0
            end
            redis.call('DECR', KEYS[1])
            return 1
            """;

    /**
     * 检查并扣减库存脚本（包含用户购买检查）
     * KEYS[1]: 库存key
     * KEYS[2]: 用户已购买集合key
     * ARGV[1]: 用户ID
     * ARGV[2]: 库存值（用于预热）
     * 返回值说明：
     * -2: 用户已购买
     * -1: 库存未预热
     *  0: 库存不足
     *  1: 扣减成功
     */
    public static final String CHECK_AND_DECREMENT_SCRIPT = """
            -- 检查用户是否已购买
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
                return -2
            end

            -- 检查库存
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                return -1
            end

            local remain = tonumber(stock)
            if remain <= 0 then
                return 0
            end

            -- 扣减库存并标记用户已购买
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 1
            """;

    /**
     * 预热库存脚本（带过期时间）
     * KEYS[1]: 库存key
     * ARGV[1]: 库存值
     * ARGV[2]: 过期时间（秒）
     * 返回值：1 表示设置成功，0 表示已存在
     */
    public static final String WARM_UP_STOCK_SCRIPT = """
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return 0
            end
            redis.call('SET', KEYS[1], ARGV[1])
            redis.call('EXPIRE', KEYS[1], ARGV[2])
            return 1
            """;

    /**
     * 预热并扣减库存脚本（原子操作）
     * 解决预热后扣减间的竞态条件
     * KEYS[1]: 库存key
     * ARGV[1]: 预热库存值（当key不存在时使用）
     * ARGV[2]: 过期时间（秒）
     * 返回值说明：
     *  正数: 扣减成功，返回剩余库存
     *  0: 库存不足（扣减后为0）
     *  -1: 参数错误
     */
    public static final String WARM_UP_AND_DECREMENT_SCRIPT = """
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                -- 库存未预热，使用传入的值初始化
                if ARGV[1] and tonumber(ARGV[1]) > 0 then
                    redis.call('SET', KEYS[1], ARGV[1])
                    if ARGV[2] and tonumber(ARGV[2]) > 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[2])
                    end
                    stock = ARGV[1]
                else
                    return -1
                end
            end

            local remain = tonumber(stock)
            if remain <= 0 then
                return 0
            end

            return redis.call('DECR', KEYS[1])
            """;
}