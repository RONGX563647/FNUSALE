package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.service.SeckillAntiFraudService;
import com.fnusale.marketing.service.SeckillService;
import com.fnusale.marketing.service.core.SeckillCoreService;
import com.fnusale.marketing.service.core.SeckillManageService;
import com.fnusale.marketing.service.core.SeckillQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀服务门面实现（v4优化：解耦重构）
 *
 * 设计原则：
 * 1. 门面模式：统一入口，委托给专门的服务类
 * 2. 单一职责：每个服务类只负责一个领域
 * 3. 低耦合：依赖数从13降低到4
 *
 * 委托服务：
 * - SeckillCoreService: 秒杀核心流程
 * - SeckillQueryService: 查询服务
 * - SeckillManageService: 管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final SeckillCoreService coreService;
    private final SeckillQueryService queryService;
    private final SeckillManageService manageService;
    private final SeckillAntiFraudService antiFraudService;
    private final SeckillActivityMapper activityMapper;

    @Override
    public List<SeckillActivityVO> getSeckillList(Long userId) {
        return queryService.getSeckillList(userId);
    }

    @Override
    public SeckillActivityVO getActivityDetail(Long activityId) {
        return queryService.getActivityDetail(activityId);
    }

    @Override
    public Object getSeckillProductDetail(Long productId) {
        return queryService.getSeckillProductDetail(productId);
    }

    @Override
    public Long joinSeckill(Long userId, Long activityId, String ip, String captchaKey, String captchaCode) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "秒杀活动不存在");
        }
        return coreService.executeSeckill(userId, activityId, activity, ip);
    }

    @Override
    public boolean needCaptcha(Long userId, Long activityId) {
        return antiFraudService.needCaptcha(userId, activityId);
    }

    @Override
    public String generateCaptcha(Long userId, Long activityId) {
        return antiFraudService.generateCaptchaKey(userId, activityId);
    }

    @Override
    public SeckillResultVO getSeckillResult(Long userId, Long activityId) {
        return queryService.getSeckillResult(userId, activityId);
    }

    @Override
    public void createActivity(SeckillActivityDTO dto) {
        manageService.createActivity(dto);
    }

    @Override
    public void updateActivity(Long activityId, SeckillActivityDTO dto) {
        manageService.updateActivity(activityId, dto);
    }

    @Override
    public void deleteActivity(Long activityId) {
        manageService.deleteActivity(activityId);
    }

    @Override
    public IPage<SeckillActivityVO> getActivityPage(String status, Integer pageNum, Integer pageSize) {
        return manageService.getActivityPage(status, pageNum, pageSize);
    }

    @Override
    public List<TodaySeckillVO> getTodaySeckills(Long userId) {
        return queryService.getTodaySeckills(userId);
    }

    @Override
    public List<String> getTimeSlots() {
        return queryService.getTimeSlots();
    }

    @Override
    public void setReminder(Long userId, Long activityId) {
        manageService.setReminder(userId, activityId);
    }

    @Override
    public void cancelReminder(Long userId, Long activityId) {
        manageService.cancelReminder(userId, activityId);
    }

    @Override
    public void preloadStock() {
        manageService.preloadStock();
    }

    @Override
    public void pushReminders() {
        manageService.pushReminders();
    }

    @Override
    public void updateActivityStatus() {
        manageService.updateActivityStatus();
    }
}