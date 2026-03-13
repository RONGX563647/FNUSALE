package com.fnusale.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fnusale.product", "com.fnusale.common.config"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.fnusale.product.mapper")
public class FnusaleProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleProductApplication.class, args);
    }
}