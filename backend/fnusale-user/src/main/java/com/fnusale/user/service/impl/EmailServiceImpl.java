package com.fnusale.user.service.impl;

import com.fnusale.user.service.EmailService;
import com.fnusale.common.entity.EmailLog;
import com.fnusale.user.service.EmailLogService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogService emailLogService;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.mail.send.retry.max:3}")
    private int maxRetryAttempts;
    
    @Value("${spring.mail.send.retry.delay:1000}")
    private long retryDelayMs;
    
    @Override
    public void sendVerificationCode(String to, String captcha) {
        int retryCount = 0;
        String errorMessage = null;
        
        while (retryCount < maxRetryAttempts) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject("【FNUSALE】验证码");
                
                String content = buildVerificationEmailContent(captcha);
                helper.setText(content, true);
                
                mailSender.send(message);
                log.info("验证码邮件发送成功 - 收件人：{}", to);
                
                // 记录成功日志
                EmailLog emailLog = EmailLog.builder()
                        .toEmail(to)
                        .subject("【FNUSALE】验证码")
                        .content(content)
                        .sendStatus("SUCCESS")
                        .sendTime(java.time.LocalDateTime.now())
                        .retryCount(retryCount)
                        .build();
                emailLogService.recordEmailLog(emailLog);
                return;
            } catch (MessagingException e) {
                retryCount++;
                errorMessage = e.getMessage();
                if (retryCount >= maxRetryAttempts) {
                    log.error("验证码邮件发送失败（已达最大重试次数） - 收件人：{}, 错误：{}", to, e.getMessage(), e);
                    
                    // 记录失败日志
                    EmailLog emailLog = EmailLog.builder()
                            .toEmail(to)
                            .subject("【FNUSALE】验证码")
                            .content(buildVerificationEmailContent(captcha))
                            .sendStatus("FAILED")
                            .errorMessage(errorMessage)
                            .sendTime(java.time.LocalDateTime.now())
                            .retryCount(retryCount)
                            .build();
                    emailLogService.recordEmailLog(emailLog);
                    
                    throw new RuntimeException("验证码邮件发送失败，请稍后重试", e);
                }
                log.warn("验证码邮件发送失败，准备重试 {}/{} - 收件人：{}, 错误：{}", 
                        retryCount, maxRetryAttempts, to, e.getMessage());
                
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("发送被中断", ie);
                }
            }
        }
    }
    
    @Override
    public void sendWelcomeEmail(String to, String username) {
        int retryCount = 0;
        String errorMessage = null;
        
        while (retryCount < maxRetryAttempts) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject("欢迎加入 FNUSALE！");
                
                String content = buildWelcomeEmailContent(username);
                helper.setText(content, true);
                
                mailSender.send(message);
                log.info("欢迎邮件发送成功 - 收件人：{}", to);
                
                // 记录成功日志
                EmailLog emailLog = EmailLog.builder()
                        .toEmail(to)
                        .subject("欢迎加入 FNUSALE！")
                        .content(content)
                        .sendStatus("SUCCESS")
                        .sendTime(java.time.LocalDateTime.now())
                        .retryCount(retryCount)
                        .build();
                emailLogService.recordEmailLog(emailLog);
                return;
            } catch (MessagingException e) {
                retryCount++;
                errorMessage = e.getMessage();
                if (retryCount >= maxRetryAttempts) {
                    log.error("欢迎邮件发送失败（已达最大重试次数） - 收件人：{}, 错误：{}", to, e.getMessage(), e);
                    
                    // 记录失败日志
                    EmailLog emailLog = EmailLog.builder()
                            .toEmail(to)
                            .subject("欢迎加入 FNUSALE！")
                            .content(buildWelcomeEmailContent(username))
                            .sendStatus("FAILED")
                            .errorMessage(errorMessage)
                            .sendTime(java.time.LocalDateTime.now())
                            .retryCount(retryCount)
                            .build();
                    emailLogService.recordEmailLog(emailLog);
                    
                    throw new RuntimeException("欢迎邮件发送失败，请稍后重试", e);
                }
                log.warn("欢迎邮件发送失败，准备重试 {}/{} - 收件人：{}, 错误：{}", 
                        retryCount, maxRetryAttempts, to, e.getMessage());
                
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("发送被中断", ie);
                }
            }
        }
    }
    
    /**
     * 构建验证码邮件内容
     */
    private String buildVerificationEmailContent(String captcha) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Microsoft YaHei', Arial, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 30px;
                        text-align: center;
                        color: #ffffff;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .content p {
                        color: #333333;
                        line-height: 1.8;
                        margin: 0 0 20px 0;
                    }
                    .captcha-box {
                        background-color: #f8f9fa;
                        border: 2px dashed #667eea;
                        border-radius: 6px;
                        padding: 20px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .captcha {
                        font-size: 36px;
                        font-weight: bold;
                        color: #667eea;
                        letter-spacing: 8px;
                        display: inline-block;
                        padding: 10px 30px;
                        background-color: #ffffff;
                        border-radius: 4px;
                    }
                    .tips {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        font-size: 14px;
                        color: #856404;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6c757d;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 FNUSALE 校园二手交易平台</h1>
                    </div>
                    <div class="content">
                        <p>尊敬的用户：</p>
                        <p>您好！您正在使用邮箱验证码进行身份验证。请使用以下验证码完成操作：</p>
                        
                        <div class="captcha-box">
                            <span class="captcha">%s</span>
                        </div>
                        
                        <div class="tips">
                            <strong>⚠️ 重要提示：</strong><br>
                            • 验证码有效期为 5 分钟<br>
                            • 请勿将验证码泄露给他人<br>
                            • 如非本人操作，请忽略此邮件
                        </div>
                        
                        <p>感谢您使用 FNUSALE 平台！</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿直接回复</p>
                        <p>&copy; 2024 FNUSALE. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(captcha);
    }
    
    /**
     * 构建欢迎邮件内容
     */
    private String buildWelcomeEmailContent(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Microsoft YaHei', Arial, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 30px;
                        text-align: center;
                        color: #ffffff;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .content p {
                        color: #333333;
                        line-height: 1.8;
                        margin: 0 0 20px 0;
                    }
                    .welcome-box {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        border-radius: 6px;
                        padding: 20px;
                        text-align: center;
                        margin: 30px 0;
                        color: #ffffff;
                    }
                    .welcome-box h2 {
                        margin: 0 0 10px 0;
                        font-size: 28px;
                    }
                    .features {
                        background-color: #f8f9fa;
                        border-radius: 6px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .features ul {
                        list-style: none;
                        padding: 0;
                        margin: 0;
                    }
                    .features li {
                        padding: 10px 0;
                        color: #333333;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    .features li:last-child {
                        border-bottom: none;
                    }
                    .features li:before {
                        content: "✓ ";
                        color: #667eea;
                        font-weight: bold;
                        margin-right: 8px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6c757d;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 FNUSALE 校园二手交易平台</h1>
                    </div>
                    <div class="content">
                        <div class="welcome-box">
                            <h2>欢迎加入，%s！</h2>
                            <p>我们很高兴成为您校园生活的一部分</p>
                        </div>
                        
                        <p>亲爱的同学：</p>
                        <p>感谢您注册 FNUSALE 校园二手交易平台！我们致力于为您提供安全、便捷的校园二手交易服务。</p>
                        
                        <div class="features">
                            <h3 style="margin-top: 0; color: #667eea;">平台特色：</h3>
                            <ul>
                                <li>实名认证，交易更安全</li>
                                <li>校园配送，取货更方便</li>
                                <li>信用评分，建立信任体系</li>
                                <li>AI 智能客服，24 小时在线服务</li>
                                <li>丰富的营销活动，优惠不断</li>
                            </ul>
                        </div>
                        
                        <p>现在您可以：</p>
                        <ul>
                            <li>浏览和购买心仪的二手商品</li>
                            <li>发布闲置物品，让资源循环利用</li>
                            <li>参与校园社区互动</li>
                            <li>享受专属学生优惠</li>
                        </ul>
                        
                        <p>祝您在 FNUSALE 平台有愉快的体验！</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿直接回复</p>
                        <p>&copy; 2024 FNUSALE. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);
    }
}
