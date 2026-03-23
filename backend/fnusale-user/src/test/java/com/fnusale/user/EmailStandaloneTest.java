package com.fnusale.user;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 独立邮件发送测试程序
 * 不依赖 Spring Boot，直接使用 Jakarta Mail 测试邮件发送功能
 *
 * 运行方式（在 IDE 中直接运行 main 方法）:
 * 1. 修改 MAIL_TO 为你的测试邮箱
 * 2. 直接运行 main 方法
 */
public class EmailStandaloneTest {

    // ========== 邮件配置 ==========
    // 使用环境变量: MAIL_SMTP_HOST, MAIL_SMTP_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_TO
    private static final String SMTP_HOST = System.getenv().getOrDefault("MAIL_SMTP_HOST", "smtp.yeah.net");
    private static final int SMTP_PORT = Integer.parseInt(System.getenv().getOrDefault("MAIL_SMTP_PORT", "465"));
    private static final String SMTP_USER = System.getenv().getOrDefault("MAIL_USERNAME", "");
    private static final String SMTP_PASSWORD = System.getenv().getOrDefault("MAIL_PASSWORD", "");

    // 测试收件人邮箱
    private static final String MAIL_TO = System.getenv().getOrDefault("MAIL_TO", "");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("FNUSALE 邮件发送测试");
        System.out.println("========================================");
        System.out.println("SMTP 服务器: " + SMTP_HOST + ":" + SMTP_PORT);
        System.out.println("发件人: " + SMTP_USER);
        System.out.println("收件人: " + MAIL_TO);
        System.out.println("========================================\n");

        if (MAIL_TO.equals("your-test-email@example.com")) {
            System.out.println("❌ 请先修改 MAIL_TO 为实际的测试邮箱地址！");
            return;
        }

        // 配置邮件属性
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", String.valueOf(SMTP_PORT));

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        // 开启调试模式
        session.setDebug(true);

        try {
            // 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(MAIL_TO));
            message.setSubject("【FNUSALE】验证码测试邮件");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: #f5f5f5; padding: 30px; border-radius: 8px;">
                        <h2 style="color: #667eea;">🎓 FNUSALE 邮件发送测试</h2>
                        <p>这是一封测试邮件，用于验证邮件服务配置是否正确。</p>
                        <div style="background: #fff; padding: 20px; border-radius: 6px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; color: #667eea; font-weight: bold;">888888</span>
                        </div>
                        <p style="color: #666; font-size: 14px;">如果您收到此邮件，说明邮件服务配置正确！</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复</p>
                    </div>
                </body>
                </html>
                """;

            message.setContent(htmlContent, "text/html; charset=UTF-8");

            System.out.println("📧 正在发送邮件...");

            // 发送邮件
            Transport.send(message);

            System.out.println("\n========================================");
            System.out.println("✅ 邮件发送成功！");
            System.out.println("请检查收件箱: " + MAIL_TO);
            System.out.println("(如果未收到，请检查垃圾邮件文件夹)");
            System.out.println("========================================");

        } catch (MessagingException e) {
            System.err.println("\n========================================");
            System.err.println("❌ 邮件发送失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
        }
    }
}