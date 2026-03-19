package com.fnusale.product.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.product.ProductCategoryDTO;
import com.fnusale.common.vo.product.ProductCategoryVO;
import com.fnusale.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品类控制器
 */
@Tag(name = "商品品类管理", description = "商品品类的增删改查接口")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @Operation(summary = "获取品类树", description = "获取完整的品类树形结构")
    @GetMapping("/tree")
    public Result<List<ProductCategoryVO>> getTree() {
        List<ProductCategoryVO> tree = productCategoryService.getTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取一级品类列表", description = "获取所有一级品类")
    @GetMapping("/list")
    public Result<List<ProductCategoryVO>> getList() {
        List<ProductCategoryVO> list = productCategoryService.getList();
        return Result.success(list);
    }

    @Operation(summary = "获取子品类", description = "根据父ID获取子品类列表")
    @GetMapping("/children/{parentId}")
    public Result<List<ProductCategoryVO>> getChildren(
            @Parameter(description = "父品类ID") @PathVariable Long parentId) {
        List<ProductCategoryVO> children = productCategoryService.getChildren(parentId);
        return Result.success(children);
    }

    @Operation(summary = "获取品类详情", description = "根据ID获取品类详细信息")
    @GetMapping("/{id}")
    public Result<ProductCategoryVO> getById(
            @Parameter(description = "品类ID") @PathVariable Long id) {
        ProductCategoryVO category = productCategoryService.getById(id);
        return Result.success(category);
    }

    @Operation(summary = "新增品类", description = "添加新品类（管理员）")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ProductCategoryDTO dto) {
        productCategoryService.add(dto);
        return Result.success();
    }

    @Operation(summary = "更新品类", description = "更新品类信息（管理员）")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "品类ID") @PathVariable Long id,
            @Valid @RequestBody ProductCategoryDTO dto) {
        productCategoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除品类", description = "删除品类（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "品类ID") @PathVariable Long id) {
        productCategoryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用品类", description = "切换品类启用状态（管理员）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "品类ID") @PathVariable Long id,
            @Parameter(description = "启用状态(0-禁用,1-启用)") @RequestParam Integer status) {
        productCategoryService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取热门品类", description = "获取热门品类列表")
    @GetMapping("/hot")
    public Result<List<ProductCategoryVO>> getHotCategories() {
        List<ProductCategoryVO> hotCategories = productCategoryService.getHotCategories();
        return Result.success(hotCategories);
    }
}