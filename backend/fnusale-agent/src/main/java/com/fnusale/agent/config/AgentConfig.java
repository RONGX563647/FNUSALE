package com.fnusale.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent配置类
 *
 * 配置通义千问API相关参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class AgentConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model = "qwen-plus";

    /**
     * 最大Token数
     */
    private Integer maxTokens = 2000;

    /**
     * 温度参数（0-1，越高越随机）
     */
    private Double temperature = 0.7;
}