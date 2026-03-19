package com.fnusale.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图API配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {

    /**
     * 高德地图API Key
     */
    private String key;

    /**
     * API基础URL
     */
    private String baseUrl = "https://restapi.amap.com/v3";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 10000;

    /**
     * 是否启用高德地图API
     */
    private Boolean enabled = true;
}
