package com.fnusale.user.service.impl;

import com.fnusale.common.entity.EmailLog;
import com.fnusale.user.service.EmailLogService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邮件服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailLogService emailLogService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(emailService, "retryDelayMs", 100L);
    }

    @Test
    @DisplayName("发送验证码邮件_成功")
    void sendVerificationCode_success() {
        // Arrange
        String to = "test@example.com";
        String captcha = "123456";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendVerificationCode(to, captcha);

        // Assert
        verify(mailSender).send(mimeMessage);
        verify(emailLogService).recordEmailLog(any(EmailLog.class));
    }

    @Test
    @DisplayName("发送欢迎邮件_成功")
    void sendWelcomeEmail_success() {
        // Arrange
        String to = "test@example.com";
        String username = "测试用户";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendWelcomeEmail(to, username);

        // Assert
        verify(mailSender).send(mimeMessage);
        verify(emailLogService).recordEmailLog(any(EmailLog.class));
    }

    @Test
    @DisplayName("发送验证码邮件_MailSender为空_跳过发送")
    void sendVerificationCode_mailSenderNull_skip() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "mailSender", null);

        // Act - should not throw
        assertDoesNotThrow(() -> emailService.sendVerificationCode("test@example.com", "123456"));

        // Assert - no interaction with mailSender
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("发送欢迎邮件_MailSender为空_跳过发送")
    void sendWelcomeEmail_mailSenderNull_skip() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "mailSender", null);

        // Act - should not throw
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("test@example.com", "测试用户"));

        // Assert - no interaction with mailSender
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("发送验证码邮件_记录正确日志")
    void sendVerificationCode_recordsCorrectLog() {
        // Arrange
        String to = "user@test.com";
        String captcha = "654321";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);

        // Act
        emailService.sendVerificationCode(to, captcha);

        // Assert
        verify(emailLogService).recordEmailLog(logCaptor.capture());
        EmailLog capturedLog = logCaptor.getValue();
        assertEquals(to, capturedLog.getToEmail());
        assertEquals("SUCCESS", capturedLog.getSendStatus());
        assertEquals("【FNUSALE】验证码", capturedLog.getSubject());
        assertTrue(capturedLog.getContent().contains(captcha));
    }

    @Test
    @DisplayName("发送欢迎邮件_记录正确日志")
    void sendWelcomeEmail_recordsCorrectLog() {
        // Arrange
        String to = "user@test.com";
        String username = "张三";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);

        // Act
        emailService.sendWelcomeEmail(to, username);

        // Assert
        verify(emailLogService).recordEmailLog(logCaptor.capture());
        EmailLog capturedLog = logCaptor.getValue();
        assertEquals(to, capturedLog.getToEmail());
        assertEquals("SUCCESS", capturedLog.getSendStatus());
        assertEquals("欢迎加入 FNUSALE！", capturedLog.getSubject());
        assertTrue(capturedLog.getContent().contains(username));
    }

    @Test
    @DisplayName("发送验证码邮件_邮件内容包含验证码")
    void sendVerificationCode_emailContainsCaptcha() {
        // Arrange
        String captcha = "888999";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);

        // Act
        emailService.sendVerificationCode("test@example.com", captcha);

        // Assert
        verify(emailLogService).recordEmailLog(logCaptor.capture());
        String content = logCaptor.getValue().getContent();
        assertTrue(content.contains(captcha));
        assertTrue(content.contains("FNUSALE"));
        assertTrue(content.contains("验证码"));
    }

    @Test
    @DisplayName("发送欢迎邮件_邮件内容包含用户名")
    void sendWelcomeEmail_emailContainsUsername() {
        // Arrange
        String username = "李四";
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);

        // Act
        emailService.sendWelcomeEmail("test@example.com", username);

        // Assert
        verify(emailLogService).recordEmailLog(logCaptor.capture());
        String content = logCaptor.getValue().getContent();
        assertTrue(content.contains(username));
        assertTrue(content.contains("FNUSALE"));
        assertTrue(content.contains("欢迎"));
    }
}