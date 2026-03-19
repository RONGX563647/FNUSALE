package com.fnusale.user.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param captcha 验证码
     */
    void sendVerificationCode(String to, String captcha);
    
    /**
     * 发送欢迎邮件
     * @param to 收件人邮箱
     * @param username 用户名
     */
    void sendWelcomeEmail(String to, String username);
}
