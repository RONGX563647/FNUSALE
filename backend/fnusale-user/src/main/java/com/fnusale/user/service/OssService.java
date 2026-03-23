package com.fnusale.user.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.fnusale.user.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OssService {

    private OSS ossClient;
    private OssConfig ossConfig;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB
    private static final long MAX_AUTH_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired(required = false)
    public void setOssClient(OSS ossClient) {
        this.ossClient = ossClient;
    }

    @Autowired(required = false)
    public void setOssConfig(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    public String uploadAvatar(MultipartFile file, Long userId) {
        validateOssConfig();
        validateImageFile(file, MAX_AVATAR_SIZE);
        String dir = generateDir("avatar", userId);
        return uploadFile(file, dir);
    }

    public String uploadAuthImage(MultipartFile file, Long userId) {
        validateOssConfig();
        validateImageFile(file, MAX_AUTH_IMAGE_SIZE);
        String dir = generateDir("auth", userId);
        return uploadFile(file, dir);
    }

    private void validateOssConfig() {
        if (ossClient == null || ossConfig == null) {
            throw new RuntimeException("OSS服务未配置，请联系管理员");
        }
    }

    public String uploadFile(MultipartFile file, String dir) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String objectName = dir + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            InputStream inputStream = new ByteArrayInputStream(file.getBytes());
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, inputStream);
            ossClient.putObject(putObjectRequest);

            String fileUrl = getFileUrl(objectName);
            log.info("文件上传成功：{}, URL: {}", objectName, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("文件上传失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName != null) {
                ossClient.deleteObject(ossConfig.getBucketName(), objectName);
                log.info("文件删除成功：{}", objectName);
            }
        } catch (Exception e) {
            log.error("文件删除失败：{}", fileUrl, e);
        }
    }

    private void validateImageFile(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持图片格式：jpg, png, gif, webp");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制：" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    private String generateDir(String type, Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("user/%s/%s/%s", type, userId, date);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private String getFileUrl(String objectName) {
        if (ossConfig.getDomain() != null && !ossConfig.getDomain().isEmpty()) {
            return String.format("%s/%s", ossConfig.getDomain(), objectName);
        }
        return String.format("https://%s.%s/%s",
                ossConfig.getBucketName(),
                ossConfig.getEndpoint(),
                objectName);
    }

    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        String domain = ossConfig.getDomain();
        if (domain != null && !domain.isEmpty() && fileUrl.contains(domain)) {
            return fileUrl.substring(fileUrl.indexOf(domain) + domain.length() + 1);
        }

        String bucketEndpoint = ossConfig.getBucketName() + "." + ossConfig.getEndpoint();
        if (fileUrl.contains(bucketEndpoint)) {
            return fileUrl.substring(fileUrl.indexOf(bucketEndpoint) + bucketEndpoint.length() + 1);
        }

        return null;
    }
}
