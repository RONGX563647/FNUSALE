package com.fnusale.marketing.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.product.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端
 */
@FeignClient(name = "fnusale-product", path = "/product")
public interface ProductClient {

    /**
     * 根据ID获取商品详情
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/{productId}")
    Result<ProductVO> getProductById(@PathVariable Long productId);

    /**
     * 获取秒杀商品详情
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/seckill/{productId}")
    Result<ProductVO> getSeckillProductById(@PathVariable Long productId);
}