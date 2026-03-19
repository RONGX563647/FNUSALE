package com.fnusale.im.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.product.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 商品服务Feign降级处理
 */
@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public Result<ProductVO> getProductById(Long productId) {
        log.warn("商品服务不可用，getProductById降级处理，productId: {}", productId);
        return Result.failed("商品服务暂时不可用");
    }

    @Override
    public Result<Map<Long, ProductVO>> getProductsByIds(List<Long> productIds) {
        log.warn("商品服务不可用，getProductsByIds降级处理，productIds: {}", productIds);
        return Result.success(Collections.emptyMap());
    }
}