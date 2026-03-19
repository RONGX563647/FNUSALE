package com.fnusale.im.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.product.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "fnusale-product", path = "/product", fallback = ProductClientFallback.class)
public interface ProductClient {

    /**
     * 根据ID获取商品信息
     */
    @GetMapping("/inner/{productId}")
    Result<ProductVO> getProductById(@PathVariable("productId") Long productId);

    /**
     * 批量获取商品信息
     */
    @PostMapping("/inner/batch")
    Result<Map<Long, ProductVO>> getProductsByIds(@RequestBody List<Long> productIds);
}