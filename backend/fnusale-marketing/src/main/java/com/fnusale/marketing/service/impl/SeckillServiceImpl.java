package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.entity.SeckillReminder;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.mapper.SeckillReminderMapper;
import com.fnusale.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final StringRedisTemplate redisTemplate;

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
        // TODO: 调用商品服务获取商品详情
        // 这里需要通过Feign调用product-service
        throw new BusinessException(500, "功能开发中");
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

        // TODO: 发送MQ消息异步创建订单
        // 这里应该发送消息到RocketMQ，由消费者创建订单
        // 临时返回一个模拟订单ID
        Long orderId = System.currentTimeMillis();

        log.info("用户 {} 参与秒杀活动 {} 成功，订单ID: {}", userId, activityId, orderId);
        return orderId;
    }

    @Override
    public SeckillResultVO getSeckillResult(Long userId, Long activityId) {
        // 检查是否已参与
        String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;
        Boolean hasBought = redisTemplate.opsForSet().isMember(boughtKey, userId.toString());

        if (Boolean.TRUE.equals(hasBought)) {
            // TODO: 查询订单状态
            // 这里应该查询订单服务获取真实的订单信息
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
            TodaySeckillVO vo = new TodaySeckillVO();
            vo.setTimeSlot(timeSlot);
            vo.setActivities(activityList);
            result.add(vo);
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
        SeckillReminder reminder = new SeckillReminder();
        reminder.setUserId(userId);
        reminder.setActivityId(activityId);
        // 提醒时间为活动开始前5分钟
        reminder.setRemindTime(activity.getStartTime().minusMinutes(MarketingConstants.SECKILL_REMINDER_MINUTES));
        reminder.setIsReminded(0);
        reminder.setCreateTime(LocalDateTime.now());
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
                // TODO: 通过IM服务推送提醒消息
                // 这里应该调用IM服务发送消息
                log.info("推送秒杀提醒: 活动 {}, 用户数 {}", activity.getId(), userIds.size());

                // 更新提醒状态
                reminderMapper.updateReminded(activity.getId());
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
        // TODO: 查询商品信息填充商品名称、图片、原价
        return vo;
    }
}