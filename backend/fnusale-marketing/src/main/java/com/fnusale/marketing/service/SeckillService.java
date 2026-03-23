package com.fnusale.marketing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.common.dto.marketing.SeckillActivityDTO;
import com.fnusale.common.vo.marketing.SeckillActivityVO;
import com.fnusale.common.vo.marketing.SeckillResultVO;
import com.fnusale.common.vo.marketing.TodaySeckillVO;

import java.util.List;

/**
 * 秒杀服务接口
 */
public interface SeckillService {

    /**
     * 获取秒杀活动列表
     *
     * @param userId 当前用户ID（可为null）
     * @return 活动列表
     */
    List<SeckillActivityVO> getSeckillList(Long userId);

    /**
     * 获取秒杀活动详情
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    SeckillActivityVO getActivityDetail(Long activityId);

    /**
     * 获取秒杀商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    Object getSeckillProductDetail(Long productId);

    /**
     * 参与秒杀（带防刷检查）
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @param ip         用户IP
     * @param captchaKey  验证码key（可选）
     * @param captchaCode 验证码（可选）
     * @return 排队号（用于查询秒杀结果）
     */
    Long joinSeckill(Long userId, Long activityId, String ip, String captchaKey, String captchaCode);
    
    /**
     * 检查用户是否需要验证码
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return true=需要验证码
     */
    boolean needCaptcha(Long userId, Long activityId);
    
    /**
     * 生成验证码
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 验证码key
     */
    String generateCaptcha(Long userId, Long activityId);

    /**
     * 获取秒杀结果
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 秒杀结果
     */
    SeckillResultVO getSeckillResult(Long userId, Long activityId);

    /**
     * 创建秒杀活动
     *
     * @param dto 活动DTO
     */
    void createActivity(SeckillActivityDTO dto);

    /**
     * 更新秒杀活动
     *
     * @param activityId 活动ID
     * @param dto        活动DTO
     */
    void updateActivity(Long activityId, SeckillActivityDTO dto);

    /**
     * 删除秒杀活动
     *
     * @param activityId 活动ID
     */
    void deleteActivity(Long activityId);

    /**
     * 分页查询秒杀活动
     *
     * @param status   状态
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<SeckillActivityVO> getActivityPage(String status, Integer pageNum, Integer pageSize);

    /**
     * 获取今日秒杀
     *
     * @param userId 当前用户ID（可为null）
     * @return 今日秒杀时间表
     */
    List<TodaySeckillVO> getTodaySeckills(Long userId);

    /**
     * 获取秒杀时段
     *
     * @return 时段列表
     */
    List<String> getTimeSlots();

    /**
     * 设置秒杀提醒
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     */
    void setReminder(Long userId, Long activityId);

    /**
     * 取消秒杀提醒
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     */
    void cancelReminder(Long userId, Long activityId);

    /**
     * 预热秒杀库存
     */
    void preloadStock();

    /**
     * 推送秒杀提醒
     */
    void pushReminders();

    /**
     * 更新活动状态
     */
    void updateActivityStatus();
}