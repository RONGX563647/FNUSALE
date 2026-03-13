package com.fnusale.aiguide;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI导购服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.aiguide", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.aiguide.mapper")
public class FnusaleAiGuideApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleAiGuideApplication.class, args);
    }
}