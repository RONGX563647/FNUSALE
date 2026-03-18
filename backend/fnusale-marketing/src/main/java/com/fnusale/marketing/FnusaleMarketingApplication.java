package com.fnusale.marketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 营销服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.marketing", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.marketing.mapper")
public class FnusaleMarketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleMarketingApplication.class, args);
    }
}