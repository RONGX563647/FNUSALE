package com.fnusale.marketing.script;

/**
 * 秒杀相关 Redis Lua 脚本
 * 用于保证原子性操作
 *
 * v4 版本脚本清单：
 * - WARM_UP_STOCK_SCRIPT: 库存预热
 * - SECKILL_ATOMIC_SCRIPT: 秒杀原子操作（核心）
 * - SECKILL_ROLLBACK_SCRIPT: 原子回滚
 * - SECKILL_CLEANUP_SCRIPT: 活动结束清理
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
     * ARGV[3]: 过期时间（秒）
     * 返回值说明：
     * -2: 用户已购买
     * -1: 库存未预热
     *  0: 库存不足
     * 正数: 扣减成功，返回剩余库存数
     */
    public static final String CHECK_AND_DECREMENT_SCRIPT = """
            -- 检查用户是否已购买
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
                return -2
            end

            -- 检查库存
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                -- 库存未预热，使用传入的值初始化
                if ARGV[2] and tonumber(ARGV[2]) > 0 then
                    redis.call('SET', KEYS[1], ARGV[2])
                    if ARGV[3] and tonumber(ARGV[3]) > 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[3])
                    end
                    stock = ARGV[2]
                else
                    return -1
                end
            end

            local remain = tonumber(stock)
            if remain <= 0 then
                return 0
            end

            -- 扣减库存并标记用户已购买
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return remain - 1
            """;

    /**
     * 预热库存脚本（带过期时间，v4优化：使用SET EX原子操作）
     * KEYS[1]: 库存key
     * ARGV[1]: 库存值
     * ARGV[2]: 过期时间（秒）
     * 返回值：1 表示设置成功，0 表示已存在
     */
    public static final String WARM_UP_STOCK_SCRIPT = """
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return 0
            end
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
            return 1
            """;

    /**
     * 秒杀原子操作脚本（v3核心脚本）
     * 将检查用户购买资格、预热库存、扣减库存、标记用户已购买合并为一个原子操作
     *
     * KEYS[1]: 库存key
     * KEYS[2]: 用户已购买集合key
     * ARGV[1]: 用户ID
     * ARGV[2]: 预热库存值（当key不存在时使用）
     * ARGV[3]: 过期时间（秒）
     *
     * 返回值说明：
     *  >= 0: 秒杀成功，返回排队号（扣减后的剩余库存，0表示抢到最后一件）
     *  -1: 参数错误
     *  -2: 用户已购买
     *  -3: 活动未预热且预热值无效
     *  -4: 库存不足
     *
     * 重要：返回值 >= 0 都表示成功，不要将 0 误判为库存不足！
     */
    public static final String SECKILL_ATOMIC_SCRIPT = """
            -- 1. 检查用户是否已购买
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
                return -2
            end

            -- 2. 获取或初始化库存（使用SET EX原子操作，v4优化）
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                local preloadStock = tonumber(ARGV[2])
                if not preloadStock or preloadStock <= 0 then
                    return -3
                end
                local expireTime = tonumber(ARGV[3])
                if expireTime and expireTime > 0 then
                    redis.call('SET', KEYS[1], preloadStock, 'EX', expireTime)
                else
                    redis.call('SET', KEYS[1], preloadStock)
                end
                stock = preloadStock
            end

            -- 3. 检查并扣减库存
            local remain = tonumber(stock)
            if remain <= 0 then
                return -4
            end

            -- 4. 原子操作：扣减库存 + 标记用户已购买
            local queueNumber = redis.call('DECR', KEYS[1])
            if queueNumber < 0 then
                -- 极端情况：并发导致库存变负，回滚
                redis.call('INCR', KEYS[1])
                return -4
            end

            -- 5. 标记用户已购买并设置过期时间
            redis.call('SADD', KEYS[2], ARGV[1])
            local expireTime = tonumber(ARGV[3])
            if expireTime and expireTime > 0 then
                redis.call('EXPIRE', KEYS[2], expireTime)
            end

            -- 返回排队号（>= 0 表示成功）
            return queueNumber
            """;

    /**
     * 秒杀回滚脚本（v4新增）
     * 用于订单创建失败时原子性回滚Redis库存和用户购买标记
     *
     * KEYS[1]: 库存key
     * KEYS[2]: 用户已购买集合key
     * ARGV[1]: 用户ID
     *
     * 返回值说明：
     *  1: 回滚成功
     *  0: 用户未在购买集合中（无需回滚）
     */
    public static final String SECKILL_ROLLBACK_SCRIPT = """
            -- 1. 检查用户是否在购买集合中
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 0 then
                return 0
            end

            -- 2. 原子操作：移除购买标记 + 恢复库存
            redis.call('SREM', KEYS[2], ARGV[1])
            redis.call('INCR', KEYS[1])

            return 1
            """;

    /**
     * 清理活动Redis数据脚本（v4新增）
     * 用于活动结束后清理Redis中的库存和用户购买标记
     *
     * KEYS[1]: 库存key
     * KEYS[2]: 用户已购买集合key
     *
     * 返回值: 删除的key数量
     */
    public static final String SECKILL_CLEANUP_SCRIPT = """
            local count = 0
            if redis.call('EXISTS', KEYS[1]) == 1 then
                redis.call('DEL', KEYS[1])
                count = count + 1
            end
            if redis.call('EXISTS', KEYS[2]) == 1 then
                redis.call('DEL', KEYS[2])
                count = count + 1
            end
            return count
            """;

    /**
     * IP限流脚本（v4优化：使用SET EX原子操作）
     * 用于原子性地检查和记录IP访问频率
     *
     * KEYS[1]: IP限流key
     * ARGV[1]: 限流阈值
     * ARGV[2]: 过期时间（秒）
     *
     * 返回值说明：
     *  1: 允许访问
     *  0: 已达到限流阈值
     */
    public static final String IP_RATE_LIMIT_SCRIPT = """
            local current = redis.call('GET', KEYS[1])
            if current == false then
                redis.call('SET', KEYS[1], 1, 'EX', ARGV[2])
                return 1
            end
            if tonumber(current) >= tonumber(ARGV[1]) then
                return 0
            end
            redis.call('INCR', KEYS[1])
            return 1
            """;
}