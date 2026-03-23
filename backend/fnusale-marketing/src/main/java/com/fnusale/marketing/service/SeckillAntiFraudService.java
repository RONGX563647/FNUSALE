package com.fnusale.marketing.service;

/**
 * 秒杀防刷服务接口
 */
public interface SeckillAntiFraudService {

    /**
     * 检查IP是否被限制
     *
     * @param ip 用户IP
     * @param activityId 活动ID
     * @return true=被限制，false=正常
     */
    boolean isIpLimited(String ip, Long activityId);

    /**
     * 记录IP访问
     *
     * @param ip 用户IP
     * @param activityId 活动ID
     */
    void recordIpAccess(String ip, Long activityId);

    /**
     * 验证验证码
     *
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return true=验证通过，false=验证失败
     */
    boolean verifyCaptcha(String captchaKey, String captchaCode);

    /**
     * 生成验证码key
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 验证码key
     */
    String generateCaptchaKey(Long userId, Long activityId);

    /**
     * 检查用户是否需要验证码
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return true=需要验证码
     */
    boolean needCaptcha(Long userId, Long activityId);
    
    /**
     * 记录用户秒杀失败
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    void recordUserFail(Long userId, Long activityId);
    
    /**
     * 清除用户失败记录
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    void clearUserFail(Long userId, Long activityId);
}
