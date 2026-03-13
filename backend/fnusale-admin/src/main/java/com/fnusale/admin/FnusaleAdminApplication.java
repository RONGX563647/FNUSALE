package com.fnusale.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.admin", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.admin.mapper")
public class FnusaleAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleAdminApplication.class, args);
    }
}