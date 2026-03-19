package com.fnusale.user.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fnusale.common.common.Result;
import com.fnusale.common.enums.ResultCode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Sentinel 配置
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        log.info("Sentinel 配置初始化完成");
    }
}
