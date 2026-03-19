package com.fnusale.product.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.product.ProductPublishDTO;
import com.fnusale.common.dto.product.ProductQueryDTO;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.product.service.ProductService;
import com.fnusale.product.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@Tag(name = "商品管理", description = "商品发布、查询、上下架等接口")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserBehaviorService userBehaviorService;

    @Operation(summary = "发布商品", description = "发布新商品")
    @PostMapping
    public Result<Long> publish(@Valid @RequestBody ProductPublishDTO dto) {
        Long productId = productService.publish(dto);
        return Result.success(productId);
    }

    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody ProductPublishDTO dto) {
        productService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品", description = "删除商品（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        ProductVO product = productService.getById(id);
        return Result.success(product);
    }

    @Operation(summary = "分页查询商品", description = "分页查询商品列表，支持多条件筛选")
    @PostMapping("/page")
    public Result<PageResult<ProductVO>> getPage(@RequestBody ProductQueryDTO dto) {
        PageResult<ProductVO> result = productService.getPage(dto);
        return Result.success(result);
    }

    @Operation(summary = "搜索商品", description = "关键词搜索商品")
    @GetMapping("/search")
    public Result<PageResult<ProductVO>> search(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.search(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "上架商品", description = "将商品上架")
    @PutMapping("/{id}/on-shelf")
    public Result<Void> onShelf(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        productService.onShelf(id);
        return Result.success();
    }

    @Operation(summary = "下架商品", description = "将商品下架")
    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        productService.offShelf(id);
        return Result.success();
    }

    @Operation(summary = "AI识别品类", description = "上传图片识别商品品类")
    @PostMapping("/ai-category")
    public Result<Object> recognizeCategory(
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {
        Object result = productService.recognizeCategory(imageUrl);
        return Result.success(result);
    }

    @Operation(summary = "获取推荐商品", description = "获取个性化推荐商品列表")
    @GetMapping("/recommend")
    public Result<PageResult<ProductVO>> getRecommend(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.getRecommend(pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "收藏商品", description = "收藏指定商品")
    @PostMapping("/{id}/favorite")
    public Result<Void> addFavorite(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        userBehaviorService.addFavorite(id);
        return Result.success();
    }

    @Operation(summary = "取消收藏", description = "取消收藏指定商品")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        userBehaviorService.removeFavorite(id);
        return Result.success();
    }

    @Operation(summary = "点赞商品", description = "点赞指定商品")
    @PostMapping("/{id}/like")
    public Result<Void> addLike(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        userBehaviorService.addLike(id);
        return Result.success();
    }

    @Operation(summary = "取消点赞", description = "取消点赞指定商品")
    @DeleteMapping("/{id}/like")
    public Result<Void> removeLike(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        userBehaviorService.removeLike(id);
        return Result.success();
    }

    @Operation(summary = "获取附近商品", description = "根据定位获取附近商品列表")
    @GetMapping("/nearby")
    public Result<PageResult<ProductVO>> getNearby(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "距离范围(米)") @RequestParam(defaultValue = "1000") Integer distance,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.getNearby(longitude, latitude, distance, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "保存草稿", description = "保存商品草稿")
    @PostMapping("/draft")
    public Result<Long> saveDraft(@RequestBody ProductPublishDTO dto) {
        Long productId = productService.saveDraft(dto);
        return Result.success(productId);
    }

    @Operation(summary = "获取草稿列表", description = "获取当前用户的草稿列表")
    @GetMapping("/draft/list")
    public Result<PageResult<ProductVO>> getDraftList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.getDraftList(pageNum, pageSize);
        return Result.success(result);
    }

    // ==================== 内部接口（供其他服务调用） ====================

    @Operation(summary = "[内部]根据ID获取商品信息", description = "供其他服务调用，获取商品详细信息")
    @GetMapping("/inner/{productId}")
    public Result<ProductVO> getProductByIdInner(
            @Parameter(description = "商品ID", required = true) @PathVariable Long productId) {
        ProductVO product = productService.getById(productId);
        return Result.success(product);
    }

    @Operation(summary = "[内部]批量获取商品信息", description = "供其他服务调用，批量获取商品信息")
    @PostMapping("/inner/batch")
    public Result<Map<Long, ProductVO>> getProductsByIdsInner(@RequestBody List<Long> productIds) {
        Map<Long, ProductVO> result = productService.getProductsByIds(productIds);
        return Result.success(result);
    }
}