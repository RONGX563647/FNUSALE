package com.fnusale.marketing.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.trade.OrderVO;
import com.fnusale.marketing.client.OrderClient;
import com.fnusale.marketing.client.ProductClient;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.mapper.SeckillReminderMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fnusale.marketing.metrics.SeckillMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀查询服务（v4优化：解耦分离）
 * 
 * 职责：
 * 1. 活动列表查询
 * 2. 活动详情查询
 * 3. 商品详情查询
 * 4. 秒杀结果查询
 * 
 * 依赖：7个（符合单一职责原则）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillQueryService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillReminderMapper reminderMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final RBloomFilter<Long> seckillProductBloomFilter;
    private final SeckillMetrics seckillMetrics;

    private static final String SECKILL_TIME_KEY_PREFIX = "seckill:time:";
    private static final int RESULT_TIMEOUT_SECONDS = 300;
    private static final ProductVO EMPTY_PRODUCT = new ProductVO();

    private Cache<Long, ProductVO> productCache;

    @jakarta.annotation.PostConstruct
    public void init() {
        productCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }

    /**
     * 获取秒杀活动列表
     */
    public List<SeckillActivityVO> getSeckillList(Long userId) {
        String cacheKey = MarketingConstants.SECKILL_TODAY_KEY;
        List<SeckillActivityVO> voList;

        try {
            String cachedList = redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null) {
                ObjectMapper mapper = new ObjectMapper();
                voList = mapper.readValue(cachedList,
                        mapper.getTypeFactory().constructCollectionType(List.class, SeckillActivityVO.class));
            } else {
                List<SeckillActivity> activities = activityMapper.selectActiveActivities();
                voList = activities.stream()
                        .map(this::convertToVO)
                        .toList();

                if (!voList.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    redisTemplate.opsForValue().set(cacheKey,
                            mapper.writeValueAsString(voList),
                            Duration.ofMinutes(5));
                }
            }
        } catch (Exception e) {
            log.warn("读取活动列表缓存失败，从数据库查询", e);
            List<SeckillActivity> activities = activityMapper.selectActiveActivities();
            voList = activities.stream()
                    .map(this::convertToVO)
                    .toList();
        }

        fillRemindedStatus(userId, voList);
        return voList;
    }

    /**
     * 获取活动详情
     */
    public SeckillActivityVO getActivityDetail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }
        return convertToVO(activity);
    }

    /**
     * 获取秒杀商品详情
     */
    public Object getSeckillProductDetail(Long productId) {
        try {
            var result = productClient.getSeckillProductById(productId);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
            throw new BusinessException(500, "商品不存在");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用商品服务获取秒杀商品详情失败: productId={}", productId, e);
            throw new BusinessException(500, "获取商品详情失败，请稍后重试");
        }
    }

    /**
     * 获取秒杀结果
     */
    public SeckillResultVO getSeckillResult(Long userId, Long activityId) {
        String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
        Boolean hasBought = redisTemplate.opsForSet().isMember(boughtKey, userId.toString());

        if (Boolean.TRUE.equals(hasBought)) {
            String timeKey = SECKILL_TIME_KEY_PREFIX + activityId + ":" + userId;
            String timeStr = redisTemplate.opsForValue().get(timeKey);
            
            if (timeStr != null) {
                long seckillTime = Long.parseLong(timeStr);
                long elapsedSeconds = (System.currentTimeMillis() - seckillTime) / 1000;
                
                if (elapsedSeconds > RESULT_TIMEOUT_SECONDS) {
                    log.warn("秒杀结果查询超时: userId={}, activityId={}, elapsedSeconds={}", 
                            userId, activityId, elapsedSeconds);
                    return SeckillResultVO.fail("订单创建超时，请联系客服处理");
                }
            }

            try {
                var result = orderClient.getSeckillOrderStatus(userId, activityId);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    OrderVO order = result.getData();
                    return SeckillResultVO.builder()
                            .success(true)
                            .orderId(order.getId())
                            .orderNo(order.getOrderNo())
                            .orderStatus(order.getOrderStatus())
                            .message("秒杀成功")
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            } catch (Exception e) {
                log.error("查询秒杀订单状态失败: userId={}, activityId={}", userId, activityId, e);
            }
            return SeckillResultVO.pending(null, activityId);
        }

        return SeckillResultVO.fail("您未参与该秒杀活动");
    }

    /**
     * 获取今日秒杀
     */
    public List<TodaySeckillVO> getTodaySeckills(Long userId) {
        List<SeckillActivity> activities = activityMapper.selectTodayActivities();

        if (activities.isEmpty()) {
            return new ArrayList<>();
        }

        List<SeckillActivityVO> voList = activities.stream()
                .map(this::convertToVO)
                .toList();

        fillRemindedStatus(userId, voList);

        Map<String, List<SeckillActivityVO>> timeSlotMap = new HashMap<>();
        Map<Long, SeckillActivity> activityMap = new HashMap<>();
        for (SeckillActivity activity : activities) {
            activityMap.put(activity.getId(), activity);
        }

        for (SeckillActivityVO vo : voList) {
            SeckillActivity activity = activityMap.get(vo.getId());
            String timeSlot = activity != null
                    ? activity.getStartTime().toLocalTime().toString().substring(0, 5)
                    : "00:00";
            timeSlotMap.computeIfAbsent(timeSlot, k -> new ArrayList<>()).add(vo);
        }

        List<TodaySeckillVO> result = new ArrayList<>();
        timeSlotMap.forEach((timeSlot, activityList) -> {
            result.add(TodaySeckillVO.builder()
                    .timeSlot(timeSlot)
                    .activities(activityList)
                    .build());
        });

        result.sort(Comparator.comparing(TodaySeckillVO::getTimeSlot));
        return result;
    }

    /**
     * 获取时间段列表
     */
    public List<String> getTimeSlots() {
        return activityMapper.selectTimeSlots();
    }

    /**
     * 批量填充提醒状态
     */
    private void fillRemindedStatus(Long userId, List<SeckillActivityVO> voList) {
        if (userId == null || voList.isEmpty()) {
            voList.forEach(vo -> vo.setReminded(false));
            return;
        }

        List<Long> activityIds = voList.stream()
                .map(SeckillActivityVO::getId)
                .toList();
        Set<Long> remindedIds = reminderMapper.selectRemindedActivityIds(userId, activityIds);
        voList.forEach(vo -> vo.setReminded(remindedIds.contains(vo.getId())));
    }

    /**
     * 获取商品信息（带布隆过滤器防护）
     */
    private ProductVO getProductWithCache(Long productId) {
        if (!seckillProductBloomFilter.contains(productId)) {
            log.debug("布隆过滤器过滤: productId={}", productId);
            return null;
        }

        ProductVO product = productCache.get(productId, key -> {
            try {
                var result = productClient.getProductById(key);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    return result.getData();
                }
                return EMPTY_PRODUCT;
            } catch (Exception e) {
                log.warn("获取商品信息失败: productId={}", key);
                return EMPTY_PRODUCT;
            }
        });

        return EMPTY_PRODUCT.equals(product) ? null : product;
    }

    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        org.springframework.beans.BeanUtils.copyProperties(activity, vo);

        Long productId = activity.getProductId();
        ProductVO product = getProductWithCache(productId);

        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductImage(product.getMainImageUrl());
            vo.setOriginalPrice(product.getPrice());
        }

        return vo;
    }
}
