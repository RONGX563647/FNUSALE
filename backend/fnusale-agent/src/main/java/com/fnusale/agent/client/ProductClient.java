package com.fnusale.agent.client;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.product.ProductQueryDTO;
import com.fnusale.common.vo.product.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "fnusale-product", contextId = "productClient")
public interface ProductClient {

    /**
     * 获取商品详情
     */
    @GetMapping("/product/inner/{productId}")
    Result<ProductVO> getProductById(@PathVariable("productId") Long productId);

    /**
     * 批量获取商品信息
     */
    @PostMapping("/product/inner/batch")
    Result<Map<Long, ProductVO>> getProductsByIds(@RequestBody List<Long> productIds);

    /**
     * 分页查询商品
     */
    @PostMapping("/product/page")
    Result<PageResult<ProductVO>> getPage(@RequestBody ProductQueryDTO dto);

    /**
     * 搜索商品
     */
    @GetMapping("/product/search")
    Result<PageResult<ProductVO>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize);
}