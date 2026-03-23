package com.fnusale.user;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 独立 OSS 上传测试程序
 * 不依赖 Spring Boot，直接使用阿里云 OSS SDK 测试上传功能
 *
 * 运行方式（在 IDE 中直接运行 main 方法）:
 * 1. 确保 ACCESS_KEY_ID 和 ACCESS_KEY_SECRET 已正确配置
 * 2. 直接运行 main 方法
 */
public class OssStandaloneTest {

    // ========== OSS 配置（与 application.yml 一致）==========
    private static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com";
    private static final String BUCKET_NAME = "rongxpicture";
    // ========== OSS 配置 ==========
    // 使用环境变量: ALIYUN_ACCESS_KEY_ID, ALIYUN_ACCESS_KEY_SECRET
    private static final String ACCESS_KEY_ID = System.getenv().getOrDefault("ALIYUN_ACCESS_KEY_ID", "");
    private static final String ACCESS_KEY_SECRET = System.getenv().getOrDefault("ALIYUN_ACCESS_KEY_SECRET", "");
    private static final String DOMAIN = "";             // 自定义域名（可选）

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("FNUSALE OSS 上传测试");
        System.out.println("========================================");
        System.out.println("Endpoint: " + ENDPOINT);
        System.out.println("Bucket: " + BUCKET_NAME);
        System.out.println("========================================\n");

        // 检查配置
        if (ACCESS_KEY_ID.isEmpty() || ACCESS_KEY_SECRET.isEmpty()) {
            System.out.println("❌ 请先配置 ACCESS_KEY_ID 和 ACCESS_KEY_SECRET！");
            System.out.println("\n获取方式：");
            System.out.println("1. 登录阿里云控制台: https://oss.console.aliyun.com");
            System.out.println("2. 进入 AccessKey 管理");
            System.out.println("3. 创建 AccessKey 并获取 ID 和 Secret");
            return;
        }

        OSS ossClient = null;
        try {
            // 创建 OSS 客户端
            System.out.println("📧 正在连接 OSS 服务...");
            ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
            System.out.println("✅ OSS 客户端创建成功！\n");

            // 测试 1: 检查 Bucket 是否存在
            System.out.println("📋 测试 1: 检查 Bucket 是否存在");
            boolean exists = ossClient.doesBucketExist(BUCKET_NAME);
            if (exists) {
                System.out.println("   ✅ Bucket '" + BUCKET_NAME + "' 存在\n");
            } else {
                System.out.println("   ❌ Bucket '" + BUCKET_NAME + "' 不存在！");
                System.out.println("   请先在阿里云 OSS 控制台创建该 Bucket");
                return;
            }

            // 测试 2: 上传文本文件
            System.out.println("📋 测试 2: 上传测试文本文件");
            String testContent = "FNUSALE OSS 测试文件 - " + java.time.LocalDateTime.now();
            String textObjectName = uploadTextFile(ossClient, testContent);
            System.out.println("   ✅ 文本文件上传成功！");
            System.out.println("   文件路径: " + textObjectName);
            System.out.println("   访问 URL: " + getFileUrl(textObjectName) + "\n");

            // 测试 3: 上传模拟图片文件
            System.out.println("📋 测试 3: 上传模拟图片文件");
            String imageObjectName = uploadSimulatedImage(ossClient);
            System.out.println("   ✅ 模拟图片上传成功！");
            System.out.println("   文件路径: " + imageObjectName);
            System.out.println("   访问 URL: " + getFileUrl(imageObjectName) + "\n");

            // 测试 4: 下载文件验证
            System.out.println("📋 测试 4: 下载文件验证");
            String downloadedContent = downloadFile(ossClient, textObjectName);
            System.out.println("   ✅ 文件下载成功！");
            System.out.println("   内容: " + downloadedContent + "\n");

            // 测试 5: 删除测试文件
            System.out.println("📋 测试 5: 删除测试文件");
            ossClient.deleteObject(BUCKET_NAME, textObjectName);
            ossClient.deleteObject(BUCKET_NAME, imageObjectName);
            System.out.println("   ✅ 测试文件已清理\n");

            System.out.println("========================================");
            System.out.println("✅ 所有 OSS 测试通过！");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("❌ OSS 测试失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传文本文件
     */
    private static String uploadTextFile(OSS ossClient, String content) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String objectName = "test/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + ".txt";

        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, inputStream);
        ossClient.putObject(putObjectRequest);

        return objectName;
    }

    /**
     * 上传模拟图片文件（创建一个简单的 PNG 图片）
     */
    private static String uploadSimulatedImage(OSS ossClient) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String objectName = "test/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + ".png";

        // 创建一个最小的有效 PNG 图片（1x1 透明像素）
        // PNG 文件头 + IHDR + IDAT + IEND
        byte[] minimalPng = new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,  // PNG signature
            0x08, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x01,        // IHDR chunk
            0x00, 0x00, 0x00, 0x00                                  // Minimal data
        };

        InputStream inputStream = new ByteArrayInputStream(minimalPng);
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, inputStream);
        com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
        metadata.setContentType("image/png");
        putObjectRequest.setMetadata(metadata);
        ossClient.putObject(putObjectRequest);

        return objectName;
    }

    /**
     * 下载文件内容
     */
    private static String downloadFile(OSS ossClient, String objectName) throws Exception {
        OSSObject ossObject = ossClient.getObject(BUCKET_NAME, objectName);
        try (InputStream inputStream = ossObject.getObjectContent()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取文件访问 URL
     */
    private static String getFileUrl(String objectName) {
        if (DOMAIN != null && !DOMAIN.isEmpty()) {
            return String.format("%s/%s", DOMAIN, objectName);
        }
        return String.format("https://%s.%s/%s", BUCKET_NAME, ENDPOINT, objectName);
    }
}