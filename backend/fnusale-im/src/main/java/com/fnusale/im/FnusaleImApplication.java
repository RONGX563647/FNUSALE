package com.fnusale.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IM服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.im", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.im.mapper")
public class FnusaleImApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleImApplication.class, args);
    }
}