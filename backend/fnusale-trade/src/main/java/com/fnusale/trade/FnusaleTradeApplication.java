package com.fnusale.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 交易服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.trade", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.trade.mapper")
public class FnusaleTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleTradeApplication.class, args);
    }
}