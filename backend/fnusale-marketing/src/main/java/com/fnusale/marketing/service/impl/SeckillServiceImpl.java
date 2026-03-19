package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.constant.LocalMessageStatus;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.entity.LocalMessage;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.entity.SeckillReminder;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.common.event.SeckillWarmUpEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.trade.OrderVO;
import com.fnusale.marketing.client.OrderClient;
import com.fnusale.marketing.client.ProductClient;
import com.fnusale.marketing.mapper.LocalMessageMapper;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.mapper.SeckillReminderMapper;
import com.fnusale.marketing.service.MarketingEventPublisher;
import com.fnusale.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillReminderMapper reminderMapper;
    private final LocalMessageMapper localMessageMapper;
    private final StringRedisTemplate redisTemplate;
    private final MarketingEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    @Override
    public List<SeckillActivityVO> getSeckillList(Long userId) {
        List<SeckillActivity> activities = activityMapper.selectActiveActivities();
        List<SeckillActivityVO> voList = new ArrayList<>();
        for (SeckillActivity activity : activities) {
            SeckillActivityVO vo = convertToVO(activity);
            // 检查是否已设置提醒
            if (userId != null) {
                int count = reminderMapper.countByUserAndActivity(userId, activity.getId());
                vo.setReminded(count > 0);
            } else {
                vo.setReminded(false);
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public SeckillActivityVO getActivityDetail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }
        return convertToVO(activity);
    }

    @Override
    public Object getSeckillProductDetail(Long productId) {
        try {
            var result = productClient.getSeckillProductById(productId);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
            throw new BusinessException(500, "商品不存在");
        } catch (Exception e) {
            log.error("调用商品服务获取秒杀商品详情失败: productId={}", productId, e);
            throw new BusinessException(500, "获取商品详情失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long joinSeckill(Long userId, Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        // 检查活动状态
        if (MarketingConstants.SECKILL_STATUS_NOT_START.equals(activity.getActivityStatus())) {
            throw new BusinessException(5002, "秒杀活动尚未开始");
        }
        if (MarketingConstants.SECKILL_STATUS_END.equals(activity.getActivityStatus())) {
            throw new BusinessException(5003, "秒杀活动已结束");
        }

        // 检查用户是否已参与
        String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
        Boolean hasBought = redisTemplate.opsForSet().isMember(boughtKey, userId.toString());
        if (Boolean.TRUE.equals(hasBought)) {
            throw new BusinessException(5005, "您已参与过该秒杀");
        }

        // Redis预扣库存
        String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);

        // 如果Redis中没有库存，从DB加载
        if (stockStr == null) {
            redisTemplate.opsForValue().set(stockKey, activity.getRemainStock().toString());
            stockStr = activity.getRemainStock().toString();
        }

        long remainStock = Long.parseLong(stockStr);
        if (remainStock <= 0) {
            throw new BusinessException(5004, "秒杀库存不足");
        }

        // Redis原子扣减库存
        Long newStock = redisTemplate.opsForValue().decrement(stockKey);
        if (newStock == null || newStock < 0) {
            // 回滚
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException(5004, "秒杀库存不足");
        }

        // 标记用户已购买
        redisTemplate.opsForSet().add(boughtKey, userId.toString());
        // 设置过期时间为活动结束时间
        long expireSeconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
        if (expireSeconds > 0) {
            redisTemplate.expire(boughtKey, expireSeconds, TimeUnit.SECONDS);
        }

        // 生成事件ID
        String eventId = UUID.randomUUID().toString();

        // 创建秒杀订单事件
        SeckillOrderEvent orderEvent = SeckillOrderEvent.builder()
                .userId(userId)
                .activityId(activityId)
                .productId(activity.getProductId())
                .seckillPrice(activity.getSeckillPrice())
                .quantity(1)
                .eventId(eventId)
                .seckillTime(LocalDateTime.now())
                .build();

        // ========== 本地消息表确保最终一致性 ==========
        try {
            // 1. 将消息内容序列化
            String messageContent = objectMapper.writeValueAsString(orderEvent);

            // 2. 保存到本地消息表（同一事务内）
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(eventId)
                    .messageType(LocalMessageStatus.MessageType.SECKILL_ORDER)
                    .topic(RocketMQConstants.SECKILL_ORDER_TOPIC)
                    .tag(RocketMQConstants.SECKILL_ORDER_TAG_CREATE)
                    .messageContent(messageContent)
                    .status(LocalMessageStatus.PENDING)
                    .retryCount(0)
                    .maxRetryCount(LocalMessageStatus.DEFAULT_MAX_RETRY_COUNT)
                    .nextRetryTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            localMessageMapper.insert(localMessage);

            // 3. 尝试发送MQ消息（如果失败，定时任务会重试）
            boolean sent = eventPublisher.sendSync(
                    RocketMQConstants.SECKILL_ORDER_TOPIC,
                    RocketMQConstants.SECKILL_ORDER_TAG_CREATE,
                    orderEvent
            );

            if (sent) {
                // 发送成功，更新消息状态
                localMessageMapper.updateToSent(localMessage.getId());
                log.info("秒杀订单消息发送成功: eventId={}", eventId);
            } else {
                // 发送失败，消息保持PENDING状态，由定时任务重试
                log.warn("秒杀订单消息发送失败，将由定时任务重试: eventId={}", eventId);
            }
        } catch (Exception e) {
            log.error("秒杀订单处理异常: userId={}, activityId={}", userId, activityId, e);
            // 注意：此处不抛出异常，因为Redis库存已扣减，用户已获得秒杀资格
            // 消息将由定时任务重试发送
        }

        log.info("用户 {} 参与秒杀活动 {} 成功，订单创建中", userId, activityId);
        return null; // 订单正在创建中，用户可轮询查询结果
    }

    @Override
    public SeckillResultVO getSeckillResult(Long userId, Long activityId) {
        // 检查是否已参与
        String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
        Boolean hasBought = redisTemplate.opsForSet().isMember(boughtKey, userId.toString());

        if (Boolean.TRUE.equals(hasBought)) {
            try {
                // 调用订单服务查询秒杀订单状态
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
            // 默认返回成功（订单正在创建中）
            return SeckillResultVO.success(System.currentTimeMillis());
        }

        return SeckillResultVO.fail("您未参与该秒杀活动");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createActivity(SeckillActivityDTO dto) {
        validateActivityDTO(dto);

        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setRemainStock(dto.getTotalStock());
        activity.setActivityStatus(MarketingConstants.SECKILL_STATUS_NOT_START);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        activityMapper.insert(activity);
        log.info("创建秒杀活动 {} 成功", activity.getId());

        // 发送延迟预热消息（活动开始前 5 分钟预热）
        scheduleWarmUp(activity);
    }

    /**
     * 调度秒杀预热
     * 在活动开始前 5 分钟预热库存到 Redis
     */
    private void scheduleWarmUp(SeckillActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warmUpTime = activity.getStartTime().minusMinutes(5);

        // 计算延迟时间（秒）
        long delaySeconds = Duration.between(now, warmUpTime).getSeconds();
        if (delaySeconds <= 0) {
            // 如果预热时间已过或即将开始，立即预热
            log.info("活动即将开始，立即预热, activityId: {}", activity.getId());
            doWarmUp(activity);
            return;
        }

        // RocketMQ 延迟级别对应时间：
        // 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        // 级别: 1  2   3   4  5  6  7  8  9 10 11 12 13  14  15  16 17 18
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

    /**
     * 计算延迟级别
     */
    private int calculateDelayLevel(long delaySeconds) {
        // 根据延迟秒数选择合适的延迟级别
        if (delaySeconds <= 1) return 1;        // 1s
        if (delaySeconds <= 5) return 2;        // 5s
        if (delaySeconds <= 10) return 3;       // 10s
        if (delaySeconds <= 30) return 4;       // 30s
        if (delaySeconds <= 60) return 5;       // 1m
        if (delaySeconds <= 120) return 6;      // 2m
        if (delaySeconds <= 180) return 7;      // 3m
        if (delaySeconds <= 240) return 8;      // 4m
        if (delaySeconds <= 300) return 9;      // 5m
        if (delaySeconds <= 360) return 10;     // 6m
        if (delaySeconds <= 420) return 11;     // 7m
        if (delaySeconds <= 480) return 12;     // 8m
        if (delaySeconds <= 540) return 13;     // 9m
        if (delaySeconds <= 600) return 14;     // 10m
        if (delaySeconds <= 1200) return 15;    // 20m
        if (delaySeconds <= 1800) return 16;    // 30m
        if (delaySeconds <= 3600) return 17;    // 1h
        return 18;                               // 2h
    }

    /**
     * 立即执行预热
     */
    private void doWarmUp(SeckillActivity activity) {
        String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activity.getId();
        redisTemplate.opsForValue().set(stockKey, activity.getTotalStock().toString());
        log.info("秒杀库存预热完成, activityId: {}, stock: {}", activity.getId(), activity.getTotalStock());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long activityId, SeckillActivityDTO dto) {
        SeckillActivity existing = activityMapper.selectById(activityId);
        if (existing == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        // 只有未开始的活动可以修改
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        // 只有未开始的活动可以删除
        if (!MarketingConstants.SECKILL_STATUS_NOT_START.equals(activity.getActivityStatus())) {
            throw new BusinessException(400, "只能删除未开始的活动");
        }

        activityMapper.deleteById(activityId);
        log.info("删除秒杀活动 {} 成功", activityId);
    }

    @Override
    public IPage<SeckillActivityVO> getActivityPage(String status, Integer pageNum, Integer pageSize) {
        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> activityPage = activityMapper.selectActivityPage(page, status);

        return activityPage.convert(this::convertToVO);
    }

    @Override
    public List<TodaySeckillVO> getTodaySeckills(Long userId) {
        List<SeckillActivity> activities = activityMapper.selectTodayActivities();

        // 按时间段分组
        Map<String, List<SeckillActivityVO>> timeSlotMap = new HashMap<>();
        for (SeckillActivity activity : activities) {
            String timeSlot = activity.getStartTime().toLocalTime().toString().substring(0, 5);
            SeckillActivityVO vo = convertToVO(activity);
            if (userId != null) {
                int count = reminderMapper.countByUserAndActivity(userId, activity.getId());
                vo.setReminded(count > 0);
            }
            timeSlotMap.computeIfAbsent(timeSlot, k -> new ArrayList<>()).add(vo);
        }

        List<TodaySeckillVO> result = new ArrayList<>();
        timeSlotMap.forEach((timeSlot, activityList) -> {
            result.add(TodaySeckillVO.builder()
                    .timeSlot(timeSlot)
                    .activities(activityList)
                    .build());
        });

        result.sort((a, b) -> a.getTimeSlot().compareTo(b.getTimeSlot()));
        return result;
    }

    @Override
    public List<String> getTimeSlots() {
        return activityMapper.selectTimeSlots();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setReminder(Long userId, Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }

        // 只有未开始的活动可以设置提醒
        if (!MarketingConstants.SECKILL_STATUS_NOT_START.equals(activity.getActivityStatus())) {
            throw new BusinessException(400, "只能对未开始的活动设置提醒");
        }

        // 检查是否已设置
        int count = reminderMapper.countByUserAndActivity(userId, activityId);
        if (count > 0) {
            throw new BusinessException(400, "您已设置过提醒");
        }

        // 创建提醒记录
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReminder(Long userId, Long activityId) {
        int rows = reminderMapper.deleteByUserAndActivity(userId, activityId);
        if (rows == 0) {
            throw new BusinessException(400, "未设置该活动的提醒");
        }
        log.info("用户 {} 取消秒杀提醒 {} 成功", userId, activityId);
    }

    @Override
    public void preloadStock() {
        List<SeckillActivity> activities = activityMapper.selectStartingSoon();
        for (SeckillActivity activity : activities) {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activity.getId();
            redisTemplate.opsForValue().set(stockKey, activity.getRemainStock().toString());
            log.info("预热秒杀活动 {} 库存: {}", activity.getId(), activity.getRemainStock());
        }
    }

    @Override
    public void pushReminders() {
        List<SeckillActivity> activities = activityMapper.selectStartingIn5Minutes();
        for (SeckillActivity activity : activities) {
            // 获取需要提醒的用户
            List<Long> userIds = reminderMapper.selectUserIdsByActivity(activity.getId());
            if (!userIds.isEmpty()) {
                // 查询商品名称
                String productName = null;
                try {
                    var result = productClient.getProductById(activity.getProductId());
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        productName = result.getData().getProductName();
                    }
                } catch (Exception e) {
                    log.warn("获取商品名称失败: productId={}", activity.getProductId());
                }

                // 发送 MQ 消息异步推送提醒
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

                // 更新提醒状态
                reminderMapper.updateReminded(activity.getId());

                log.info("秒杀提醒消息已发送: 活动 {}, 用户数 {}", activity.getId(), userIds.size());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus() {
        // 更新未开始 -> 进行中
        int toOngoing = activityMapper.updateToOngoing();
        if (toOngoing > 0) {
            log.info("更新 {} 个秒杀活动状态为进行中", toOngoing);
        }

        // 更新进行中 -> 已结束
        int toEnded = activityMapper.updateToEnded();
        if (toEnded > 0) {
            log.info("更新 {} 个秒杀活动状态为已结束", toEnded);
        }
    }

    /**
     * 校验活动DTO
     */
    private void validateActivityDTO(SeckillActivityDTO dto) {
        // 活动时长不超过2小时
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            long hours = Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
            if (hours > MarketingConstants.SECKILL_MAX_DURATION_HOURS) {
                throw new BusinessException(400, "活动时长不能超过2小时");
            }
        }

        // 秒杀库存不超过50件
        if (dto.getTotalStock() != null && dto.getTotalStock() > 50) {
            throw new BusinessException(400, "秒杀库存不超过50件");
        }

        // 开始时间必须大于当前时间
        if (dto.getStartTime() != null && dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "开始时间必须大于当前时间");
        }
    }

    /**
     * 转换为VO
     */
    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        BeanUtils.copyProperties(activity, vo);

        // 查询商品信息填充商品名称、图片、原价
        try {
            var result = productClient.getProductById(activity.getProductId());
            if (result != null && result.isSuccess() && result.getData() != null) {
                ProductVO product = result.getData();
                vo.setProductName(product.getProductName());
                vo.setProductImage(product.getMainImageUrl());
                vo.setOriginalPrice(product.getPrice());
            }
        } catch (Exception e) {
            log.warn("获取秒杀商品信息失败: productId={}", activity.getProductId());
        }

        return vo;
    }
}