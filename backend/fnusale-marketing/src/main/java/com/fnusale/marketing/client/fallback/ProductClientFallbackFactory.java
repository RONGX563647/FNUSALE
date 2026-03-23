package com.fnusale.marketing.client.fallback;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.marketing.client.ProductClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 商品服务 Feign 降级工厂
 * 当商品服务不可用时提供降级响应
 */
@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.warn("商品服务降级触发: {}", cause.getMessage());
        
        return new ProductClient() {
            @Override
            public Result<ProductVO> getProductById(Long productId) {
                log.warn("商品服务降级: getProductById, productId={}", productId);
                return Result.failed("商品服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<ProductVO> getSeckillProductById(Long productId) {
                log.warn("商品服务降级: getSeckillProductById, productId={}", productId);
                return Result.failed("商品服务暂时不可用，请稍后重试");
            }
        };
    }
}
