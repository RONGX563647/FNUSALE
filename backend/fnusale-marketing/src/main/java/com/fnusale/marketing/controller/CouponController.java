package com.fnusale.marketing.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.marketing.CouponDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券控制器
 */
@Tag(name = "优惠券管理", description = "优惠券领取、使用、管理等接口")
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Operation(summary = "获取可领取优惠券列表", description = "获取当前可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Object>> getAvailableCoupons() {
        // TODO: 实现获取可领取优惠券列表逻辑
        return Result.success();
    }

    @Operation(summary = "领取优惠券", description = "领取指定优惠券")
    @PostMapping("/{couponId}/receive")
    public Result<Void> receiveCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        // TODO: 实现领取优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的优惠券列表", description = "获取当前用户的优惠券列表")
    @GetMapping("/my")
    public Result<List<Object>> getMyCoupons(
            @Parameter(description = "状态(UNUSED/USED/EXPIRED)") @RequestParam(required = false) String status) {
        // TODO: 实现获取我的优惠券列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取可用优惠券", description = "获取指定商品可用的优惠券")
    @GetMapping("/usable")
    public Result<List<Object>> getUsableCoupons(
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "商品价格") @RequestParam java.math.BigDecimal price) {
        // TODO: 实现获取可用优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "获取优惠券详情", description = "获取优惠券详细信息")
    @GetMapping("/{couponId}")
    public Result<Object> getCouponDetail(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        // TODO: 实现获取优惠券详情逻辑
        return Result.success();
    }

    @Operation(summary = "新增优惠券", description = "创建新的优惠券（管理员）")
    @PostMapping
    public Result<Void> createCoupon(@Valid @RequestBody CouponDTO dto) {
        // TODO: 实现新增优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "更新优惠券", description = "更新优惠券信息（管理员）")
    @PutMapping("/{couponId}")
    public Result<Void> updateCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponDTO dto) {
        // TODO: 实现更新优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "删除优惠券", description = "删除优惠券（管理员）")
    @DeleteMapping("/{couponId}")
    public Result<Void> deleteCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        // TODO: 实现删除优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "启用/禁用优惠券", description = "切换优惠券启用状态（管理员）")
    @PutMapping("/{couponId}/status")
    public Result<Void> updateCouponStatus(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Parameter(description = "启用状态(0-禁用,1-启用)") @RequestParam Integer status) {
        // TODO: 实现启用/禁用优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "分页查询优惠券", description = "分页查询优惠券列表（管理员）")
    @GetMapping("/page")
    public Result<PageResult<Object>> getCouponPage(
            @Parameter(description = "优惠券名称") @RequestParam(required = false) String name,
            @Parameter(description = "优惠券类型") @RequestParam(required = false) String type,
            @Parameter(description = "启用状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现分页查询优惠券逻辑
        return Result.success();
    }

    @Operation(summary = "发放优惠券", description = "向指定用户发放优惠券（管理员）")
    @PostMapping("/{couponId}/grant")
    public Result<Void> grantCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Parameter(description = "用户ID列表") @RequestBody List<Long> userIds) {
        // TODO: 实现发放优惠券逻辑
        return Result.success();
    }
}