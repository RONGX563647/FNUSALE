package com.fnusale.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 智能购物助手服务启动类
 *
 * 功能：
 * - 对话式购物助手：意图理解、多轮对话、商品筛选
 * - 购买分析建议：比价分析、卖家信誉评估、风险提醒
 * - 议价辅助：价格区间建议、议价策略、话术生成
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fnusale.agent.client")
public class FnusaleAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnusaleAgentApplication.class, args);
    }
}