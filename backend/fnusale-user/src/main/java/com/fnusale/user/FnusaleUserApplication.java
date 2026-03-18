package com.fnusale.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.user", "com.fnusale.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.user.mapper")
public class FnusaleUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleUserApplication.class, args);
    }
}