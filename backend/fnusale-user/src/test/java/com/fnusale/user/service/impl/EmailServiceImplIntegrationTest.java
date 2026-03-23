package com.fnusale.user.service.impl;

import com.fnusale.common.entity.EmailLog;
import com.fnusale.user.service.EmailLogService;
import com.fnusale.user.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邮件服务集成测试
 *
 * 运行测试前需要设置环境变量:
 *   TEST_EMAIL_TO=your-email@example.com
 *
 * 运行方式:
 *   mvn test -pl fnusale-user -Dtest=EmailServiceImplIntegrationTest
 */
@SpringBootTest
@ActiveProfiles("dev")
class EmailServiceImplIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @MockBean
    private EmailLogService emailLogService;

    private String testEmailTo;

    @BeforeEach
    void setUp() {
        testEmailTo = System.getenv("TEST_EMAIL_TO");
    }

    @Test
    @DisplayName("检查JavaMailSender是否已配置")
    void checkMailSenderConfigured() {
        if (mailSender == null) {
            System.out.println("⚠️ JavaMailSender 未配置，邮件发送功能将跳过");
        } else {
            System.out.println("✅ JavaMailSender 已配置，可以进行邮件发送测试");
        }
    }

    @Nested
    @DisplayName("验证码邮件发送测试")
    class SendVerificationCodeTests {

        @Test
        @DisplayName("发送验证码邮件_成功")
        @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
        void sendVerificationCode_success() {
            // Arrange
            String captcha = "123456";

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendVerificationCode(testEmailTo, captcha));

            // Verify log was recorded
            verify(emailLogService, times(1)).recordEmailLog(any(EmailLog.class));

            System.out.println("✅ 验证码邮件发送成功，请检查收件箱: " + testEmailTo);
        }

        @Test
        @DisplayName("发送验证码邮件_无效邮箱_抛出异常")
        void sendVerificationCode_invalidEmail_throwsException() {
            String invalidEmail = "invalid-email";
            String captcha = "123456";

            assertThrows(Exception.class, () -> emailService.sendVerificationCode(invalidEmail, captcha));
        }
    }

    @Nested
    @DisplayName("欢迎邮件发送测试")
    class SendWelcomeEmailTests {

        @Test
        @DisplayName("发送欢迎邮件_成功")
        @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
        void sendWelcomeEmail_success() {
            // Arrange
            String username = "测试用户";

            // Act & Assert
            assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testEmailTo, username));

            // Verify log was recorded
            verify(emailLogService, times(1)).recordEmailLog(any(EmailLog.class));

            System.out.println("✅ 欢迎邮件发送成功，请检查收件箱: " + testEmailTo);
        }
    }

    /**
     * 手动测试方法 - 不依赖环境变量
     * 可直接在 IDE 中运行，修改 targetEmail 为你的测试邮箱
     */
    @Test
    @DisplayName("手动测试邮件发送功能")
    void manualTestEmailSending() {
        // 修改此处为你的测试邮箱
        String targetEmail = "your-test-email@example.com";

        // 跳过占位符邮箱
        if (targetEmail.equals("your-test-email@example.com")) {
            System.out.println("⚠️ 请修改 targetEmail 为实际的测试邮箱地址");
            System.out.println("💡 提示: 在 IDE 中直接修改此测试方法的 targetEmail 变量");
            return;
        }

        System.out.println("========================================");
        System.out.println("开始邮件发送测试...");
        System.out.println("目标邮箱: " + targetEmail);
        System.out.println("========================================");

        try {
            // 测试验证码邮件
            System.out.println("\n📧 发送验证码邮件...");
            emailService.sendVerificationCode(targetEmail, "888888");
            System.out.println("✅ 验证码邮件发送成功!");

            // 测试欢迎邮件
            System.out.println("\n📧 发送欢迎邮件...");
            emailService.sendWelcomeEmail(targetEmail, "测试同学");
            System.out.println("✅ 欢迎邮件发送成功!");

            System.out.println("\n========================================");
            System.out.println("所有邮件发送测试完成!");
            System.out.println("请检查收件箱: " + targetEmail);
            System.out.println("========================================");

        } catch (Exception e) {
            System.out.println("\n❌ 邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            fail("邮件发送失败: " + e.getMessage());
        }
    }
}