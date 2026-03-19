package com.fnusale.trade.client;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.product.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "fnusale-product", path = "/product")
public interface ProductClient {

    /**
     * 根据ID获取商品信息
     */
    @GetMapping("/inner/{productId}")
    Result<ProductVO> getProductById(@PathVariable("productId") Long productId);

    /**
     * 更新商品状态
     */
    @PutMapping("/inner/{productId}/status")
    Result<Void> updateProductStatus(
            @PathVariable("productId") Long productId,
            @RequestParam("status") String status);

    /**
     * 检查商品是否可购买
     */
    @GetMapping("/inner/{productId}/purchasable")
    Result<Boolean> checkPurchasable(@PathVariable("productId") Long productId);
}