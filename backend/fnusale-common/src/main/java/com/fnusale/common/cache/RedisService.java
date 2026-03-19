package com.fnusale.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 封装 Bitmap、GEO、Hash、Sorted Set 等高级操作
 */
@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // ==================== Bitmap 操作 ====================

    /**
     * 设置 Bitmap 位
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  值 (true=1, false=0)
     * @return 原来的值
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 获取 Bitmap 位
     *
     * @param key    键
     * @param offset 偏移量
     * @return 值
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 统计 Bitmap 中值为 1 的位数
     *
     * @param key 键
     * @return 值为 1 的位数
     */
    public Long bitCount(String key) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.bitCount(key.getBytes()));
    }

    /**
     * 统计 Bitmap 指定范围内值为 1 的位数
     *
     * @param key   键
     * @param start 起始字节
     * @param end   结束字节
     * @return 值为 1 的位数
     */
    public Long bitCount(String key, long start, long end) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.bitCount(key.getBytes(), start, end));
    }

    /**
     * 获取 Bitmap 指定范围的位值
     *
     * @param key     键
     * @param bitSize 位数
     * @param offset  起始偏移量
     * @return 位值列表
     */
    public List<Long> bitFieldGet(String key, int bitSize, long offset) {
        BitFieldSubCommands commands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(bitSize))
                .valueAt(offset);
        List<Long> result = redisTemplate.opsForValue().bitField(key, commands);
        return result != null ? result : new ArrayList<>();
    }

    // ==================== GEO 操作 ====================

    /**
     * 添加地理位置
     *
     * @param key   键
     * @param lng   经度
     * @param lat   纬度
     * @param member 成员名称
     * @return 添加的元素数量
     */
    public Long geoAdd(String key, double lng, double lat, String member) {
        return redisTemplate.opsForGeo().add(key, new Point(lng, lat), member);
    }

    /**
     * 批量添加地理位置
     *
     * @param key     键
     * @param members 成员坐标映射
     * @return 添加的元素数量
     */
    public Long geoAdd(String key, Map<String, Point> members) {
        Map<String, Point> memberMap = new HashMap<>();
        members.forEach((member, point) -> memberMap.put(member, point));
        return redisTemplate.opsForGeo().add(key, memberMap);
    }

    /**
     * 获取地理位置坐标
     *
     * @param key    键
     * @param member 成员名称
     * @return 坐标点
     */
    public Point geoPos(String key, String member) {
        List<Point> points = redisTemplate.opsForGeo().position(key, member);
        return points != null && !points.isEmpty() ? points.get(0) : null;
    }

    /**
     * 计算两个成员之间的距离
     *
     * @param key     键
     * @param member1 成员1
     * @param member2 成员2
     * @return 距离（米）
     */
    public Double geoDist(String key, String member1, String member2) {
        Distance distance = redisTemplate.opsForGeo().distance(key, member1, member2, Metrics.KILOMETERS);
        return distance != null ? distance.getValue() * 1000 : null;
    }

    /**
     * 查询指定半径内的地理位置
     *
     * @param key     键
     * @param lng     中心点经度
     * @param lat     中心点纬度
     * @param radius  半径（米）
     * @return 地理位置结果列表
     */
    public List<GeoResult> geoRadius(String key, double lng, double lat, double radius) {
        Circle circle = new Circle(new Point(lng, lat), new Distance(radius / 1000, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(key, circle, RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .sortAscending());

        if (results == null) {
            return new ArrayList<>();
        }

        List<GeoResult> result = new ArrayList<>();
        results.forEach(r -> {
            GeoResult gr = new GeoResult();
            gr.setMember(r.getContent().getName());
            gr.setDistance(r.getDistance().getValue() * 1000); // 转为米
            result.add(gr);
        });
        return result;
    }

    /**
     * 删除地理位置
     *
     * @param key    键
     * @param member 成员名称
     * @return 删除数量
     */
    public Long geoRemove(String key, String member) {
        return redisTemplate.opsForGeo().remove(key, member);
    }

    // ==================== Hash 操作 ====================

    /**
     * 设置 Hash 字段
     */
    public void hSet(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 批量设置 Hash 字段
     */
    public void hSetAll(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取 Hash 字段
     */
    public String hGet(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取 Hash 所有字段
     */
    public Map<String, String> hGetAll(String key) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k.toString(), v != null ? v.toString() : null));
        return result;
    }

    /**
     * 删除 Hash 字段
     */
    public Long hDel(String key, String... fields) {
        return redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    /**
     * 检查 Hash 字段是否存在
     */
    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ==================== Sorted Set 操作 ====================

    /**
     * 添加到有序集合
     */
    public Boolean zAdd(String key, String member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * 增加有序集合成员分数
     */
    public Double zIncrBy(String key, String member, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, member, delta);
    }

    /**
     * 获取有序集合成员分数
     */
    public Double zScore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    /**
     * 获取有序集合排名（从高到低）
     */
    public Long zRevRank(String key, String member) {
        return redisTemplate.opsForZSet().reverseRank(key, member);
    }

    /**
     * 获取有序集合指定范围成员（从高到低）
     */
    public Set<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 删除有序集合成员
     */
    public Long zRemove(String key, String... members) {
        return redisTemplate.opsForZSet().remove(key, (Object[]) members);
    }

    // ==================== 通用操作 ====================

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration);
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 地理位置查询结果
     */
    public static class GeoResult {
        private String member;
        private Double distance;

        public String getMember() {
            return member;
        }

        public void setMember(String member) {
            this.member = member;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }
    }
}