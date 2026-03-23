package com.fnusale.marketing.service.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.entity.SeckillReminder;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.common.event.SeckillWarmUpEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.mapper.SeckillReminderMapper;
import com.fnusale.marketing.script.SeckillLuaScript;
import com.fnusale.marketing.service.MarketingEventPublisher;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.marketing.client.ProductClient;
import com.fnusale.marketing.metrics.SeckillMetrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 秒杀管理服务（v4优化：解耦分离）
 * 
 * 职责：
 * 1. 活动创建、修改、删除
 * 2. 活动预热
 * 3. 提醒管理
 * 4. 定时任务
 * 
 * 依赖：7个（符合单一职责原则）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillManageService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillReminderMapper reminderMapper;
    private final StringRedisTemplate redisTemplate;
    private final MarketingEventPublisher eventPublisher;
    private final ProductClient productClient;
    private final RBloomFilter<Long> seckillProductBloomFilter;
    private final SeckillMetrics seckillMetrics;

    private static final ProductVO EMPTY_PRODUCT = new ProductVO();

    private DefaultRedisScript<Long> warmUpStockScript;
    private DefaultRedisScript<Long> seckillCleanupScript;
    private Cache<Long, ProductVO> productCache;

    @PostConstruct
    public void init() {
        warmUpStockScript = new DefaultRedisScript<>();
        warmUpStockScript.setScriptText(SeckillLuaScript.WARM_UP_STOCK_SCRIPT);
        warmUpStockScript.setResultType(Long.class);

        seckillCleanupScript = new DefaultRedisScript<>();
        seckillCleanupScript.setScriptText(SeckillLuaScript.SECKILL_CLEANUP_SCRIPT);
        seckillCleanupScript.setResultType(Long.class);

        productCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }

    /**
     * 创建秒杀活动
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createActivity(SeckillActivityDTO dto) {
        validateActivityDTO(dto);

        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setRemainStock(dto.getTotalStock());
        activity.setActivityStatus(MarketingConstants.SECKILL_STATUS_NOT_START);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        activityMapper.insert(activity);
        log.info("创建秒杀活动 {} 成功", activity.getId());

        addToBloomFilter(activity.getProductId());
        scheduleWarmUp(activity);

        return activity.getId();
    }

    /**
     * 更新秒杀活动
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long activityId, SeckillActivityDTO dto) {
        SeckillActivity existing = activityMapper.selectById(activityId);
        if (existing == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        if (!MarketingConstants.SECKILL_STATUS_NOT_START.equals(existing.getActivityStatus())) {
            throw new BusinessException(400, "只能修改未开始的活动");
        }

        validateActivityDTO(dto);

        BeanUtils.copyProperties(dto, existing);
        existing.setId(activityId);
        existing.setRemainStock(dto.getTotalStock());
        existing.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(existing);

        log.info("更新秒杀活动 {} 成功", activityId);
    }

    /**
     * 删除秒杀活动
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        if (!MarketingConstants.SECKILL_STATUS_NOT_START.equals(activity.getActivityStatus())) {
            throw new BusinessException(400, "只能删除未开始的活动");
        }

        activityMapper.deleteById(activityId);
        log.info("删除秒杀活动 {} 成功", activityId);
    }

    /**
     * 设置提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public void setReminder(Long userId, Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        if (!MarketingConstants.SECKILL_STATUS_NOT_START.equals(activity.getActivityStatus())) {
            throw new BusinessException(400, "只能对未开始的活动设置提醒");
        }

        int count = reminderMapper.countByUserAndActivity(userId, activityId);
        if (count > 0) {
            throw new BusinessException(400, "您已设置过提醒");
        }

        SeckillReminder reminder = SeckillReminder.builder()
                .userId(userId)
                .activityId(activityId)
                .remindTime(activity.getStartTime().minusMinutes(MarketingConstants.SECKILL_REMINDER_MINUTES))
                .isReminded(0)
                .createTime(LocalDateTime.now())
                .build();
        reminderMapper.insert(reminder);

        log.info("用户 {} 设置秒杀提醒 {} 成功", userId, activityId);
    }

    /**
     * 取消提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelReminder(Long userId, Long activityId) {
        int rows = reminderMapper.deleteByUserAndActivity(userId, activityId);
        if (rows == 0) {
            throw new BusinessException(400, "未设置该活动的提醒");
        }
        log.info("用户 {} 取消秒杀提醒 {} 成功", userId, activityId);
    }

    /**
     * 预热库存
     */
    public void preloadStock() {
        List<SeckillActivity> activities = activityMapper.selectStartingSoon();
        for (SeckillActivity activity : activities) {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activity.getId();

            long expireSeconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
            if (expireSeconds <= 0) {
                expireSeconds = MarketingConstants.SECKILL_STOCK_PRELOAD_MINUTES * 60L;
            }

            redisTemplate.execute(
                    warmUpStockScript,
                    Collections.singletonList(stockKey),
                    activity.getRemainStock().toString(),
                    String.valueOf(expireSeconds)
            );
            log.info("预热秒杀活动 {} 库存: {}", activity.getId(), activity.getRemainStock());
        }
    }

    /**
     * 推送提醒
     */
    public void pushReminders() {
        List<SeckillActivity> activities = activityMapper.selectStartingIn5Minutes();
        for (SeckillActivity activity : activities) {
            List<Long> userIds = reminderMapper.selectUserIdsByActivity(activity.getId());
            if (!userIds.isEmpty()) {
                String productName = null;
                Long productId = activity.getProductId();
                ProductVO product = getProductWithCache(productId);

                if (product != null) {
                    productName = product.getProductName();
                }

                SeckillReminderEvent reminderEvent = SeckillReminderEvent.builder()
                        .activityId(activity.getId())
                        .activityName(activity.getActivityName())
                        .productId(activity.getProductId())
                        .productName(productName)
                        .startTime(activity.getStartTime())
                        .userIds(userIds)
                        .eventId(UUID.randomUUID().toString())
                        .remindTime(LocalDateTime.now())
                        .build();
                eventPublisher.publishSeckillReminderEvent(reminderEvent);

                reminderMapper.updateReminded(activity.getId());

                log.info("秒杀提醒消息已发送: 活动 {}, 用户数 {}", activity.getId(), userIds.size());
            }
        }
    }

    /**
     * 更新活动状态
     */
    public void updateActivityStatus() {
        int toOngoing = activityMapper.updateToOngoing();
        if (toOngoing > 0) {
            log.info("更新 {} 个秒杀活动状态为进行中", toOngoing);
        }

        List<SeckillActivity> endedActivities = activityMapper.selectEndedActivities();
        int toEnded = activityMapper.updateToEnded();
        if (toEnded > 0) {
            log.info("更新 {} 个秒杀活动状态为已结束", toEnded);

            for (SeckillActivity activity : endedActivities) {
                cleanupEndedActivityRedis(activity.getId());
            }
        }
    }

    /**
     * 获取活动分页
     */
    public IPage<SeckillActivityVO> getActivityPage(String status, Integer pageNum, Integer pageSize) {
        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> activityPage = activityMapper.selectActivityPage(page, status);

        return activityPage.convert(this::convertToVO);
    }

    private void scheduleWarmUp(SeckillActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warmUpTime = activity.getStartTime().minusMinutes(5);

        long delaySeconds = Duration.between(now, warmUpTime).getSeconds();
        if (delaySeconds <= 0) {
            log.info("活动即将开始，立即预热, activityId: {}", activity.getId());
            doWarmUp(activity);
            return;
        }

        int delayLevel = calculateDelayLevel(delaySeconds);

        SeckillWarmUpEvent event = SeckillWarmUpEvent.builder()
                .activityId(activity.getId())
                .productId(activity.getProductId())
                .seckillPrice(activity.getSeckillPrice())
                .stock(activity.getTotalStock())
                .startTime(activity.getStartTime())
                .eventId(UUID.randomUUID().toString())
                .build();

        eventPublisher.publishSeckillWarmUpEvent(event, delayLevel);
        log.info("已调度秒杀预热消息, activityId: {}, delayLevel: {}, 预热时间: {}",
                activity.getId(), delayLevel, warmUpTime);
    }

    private void doWarmUp(SeckillActivity activity) {
        String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activity.getId();

        long expireSeconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
        if (expireSeconds <= 0) {
            expireSeconds = MarketingConstants.SECKILL_STOCK_PRELOAD_MINUTES * 60L;
        }

        Long result = redisTemplate.execute(
                warmUpStockScript,
                Collections.singletonList(stockKey),
                activity.getTotalStock().toString(),
                String.valueOf(expireSeconds)
        );

        if (result != null && result == 1) {
            log.info("秒杀库存预热完成: activityId={}, stock={}", activity.getId(), activity.getTotalStock());
        } else {
            log.debug("秒杀库存已存在，跳过预热: activityId={}", activity.getId());
        }
    }

    private void cleanupEndedActivityRedis(Long activityId) {
        try {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
            String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;

            Long result = redisTemplate.execute(
                    seckillCleanupScript,
                    List.of(stockKey, boughtKey)
            );

            if (result != null && result > 0) {
                log.info("清理已结束活动Redis数据: activityId={}, 删除key数量={}", activityId, result);
            }
        } catch (Exception e) {
            log.error("清理活动Redis数据失败: activityId={}", activityId, e);
        }
    }

    private void addToBloomFilter(Long productId) {
        seckillProductBloomFilter.add(productId);
        log.debug("添加商品到布隆过滤器: productId={}", productId);
    }

    private void validateActivityDTO(SeckillActivityDTO dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            long hours = Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
            if (hours > MarketingConstants.SECKILL_MAX_DURATION_HOURS) {
                throw new BusinessException(400, "活动时长不能超过2小时");
            }
        }

        if (dto.getTotalStock() != null && dto.getTotalStock() > 50) {
            throw new BusinessException(400, "秒杀库存不超过50件");
        }

        if (dto.getStartTime() != null && dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "开始时间必须大于当前时间");
        }
    }

    private int calculateDelayLevel(long delaySeconds) {
        if (delaySeconds <= 1) return 1;
        if (delaySeconds <= 5) return 2;
        if (delaySeconds <= 10) return 3;
        if (delaySeconds <= 30) return 4;
        if (delaySeconds <= 60) return 5;
        if (delaySeconds <= 120) return 6;
        if (delaySeconds <= 180) return 7;
        if (delaySeconds <= 240) return 8;
        if (delaySeconds <= 300) return 9;
        if (delaySeconds <= 360) return 10;
        if (delaySeconds <= 420) return 11;
        if (delaySeconds <= 480) return 12;
        if (delaySeconds <= 540) return 13;
        if (delaySeconds <= 600) return 14;
        if (delaySeconds <= 1200) return 15;
        if (delaySeconds <= 1800) return 16;
        if (delaySeconds <= 3600) return 17;
        return 18;
    }

    private ProductVO getProductWithCache(Long productId) {
        if (!seckillProductBloomFilter.contains(productId)) {
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
        BeanUtils.copyProperties(activity, vo);

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
