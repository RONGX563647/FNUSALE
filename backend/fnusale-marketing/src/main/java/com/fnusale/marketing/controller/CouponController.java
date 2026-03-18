package com.fnusale.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.marketing.CouponDTO;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.marketing.CouponVO;
import com.fnusale.common.vo.marketing.UserCouponVO;
import com.fnusale.marketing.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 */
@Tag(name = "优惠券管理", description = "优惠券领取、使用、管理等接口")
@RestController
@RequestMapping("/marketing/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "获取可领取优惠券列表", description = "获取当前可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<CouponVO>> getAvailableCoupons() {
        Long userId = UserContext.getCurrentUserId();
        List<CouponVO> coupons = couponService.getAvailableCoupons(userId);
        return Result.success(coupons);
    }

    @Operation(summary = "领取优惠券", description = "领取指定优惠券")
    @PostMapping("/{couponId}/receive")
    public Result<Void> receiveCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        Long userId = UserContext.getUserIdOrThrow();
        couponService.receiveCoupon(userId, couponId);
        return Result.success("领取成功", null);
    }

    @Operation(summary = "获取我的优惠券列表", description = "获取当前用户的优惠券列表")
    @GetMapping("/my")
    public Result<List<UserCouponVO>> getMyCoupons(
            @Parameter(description = "状态(UNUSED/USED/EXPIRED)") @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserIdOrThrow();
        List<UserCouponVO> coupons = couponService.getMyCoupons(userId, status);
        return Result.success(coupons);
    }

    @Operation(summary = "获取可用优惠券", description = "获取指定商品可用的优惠券")
    @GetMapping("/usable")
    public Result<List<UserCouponVO>> getUsableCoupons(
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "商品价格") @RequestParam BigDecimal price) {
        Long userId = UserContext.getUserIdOrThrow();
        List<UserCouponVO> coupons = couponService.getUsableCoupons(userId, productId, price);
        return Result.success(coupons);
    }

    @Operation(summary = "获取优惠券详情", description = "获取优惠券详细信息")
    @GetMapping("/{couponId}")
    public Result<CouponVO> getCouponDetail(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        CouponVO coupon = couponService.getCouponDetail(couponId);
        return Result.success(coupon);
    }

    @Operation(summary = "新增优惠券", description = "创建新的优惠券（管理员）")
    @PostMapping
    public Result<Void> createCoupon(@Valid @RequestBody CouponDTO dto) {
        couponService.createCoupon(dto);
        return Result.success("创建成功", null);
    }

    @Operation(summary = "更新优惠券", description = "更新优惠券信息（管理员）")
    @PutMapping("/{couponId}")
    public Result<Void> updateCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponDTO dto) {
        couponService.updateCoupon(couponId, dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除优惠券", description = "删除优惠券（管理员）")
    @DeleteMapping("/{couponId}")
    public Result<Void> deleteCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "启用/禁用优惠券", description = "切换优惠券启用状态（管理员）")
    @PutMapping("/{couponId}/status")
    public Result<Void> updateCouponStatus(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Parameter(description = "启用状态(0-禁用,1-启用)") @RequestParam Integer status) {
        couponService.updateCouponStatus(couponId, status);
        return Result.success("操作成功", null);
    }

    @Operation(summary = "分页查询优惠券", description = "分页查询优惠券列表（管理员）")
    @GetMapping("/page")
    public Result<PageResult<CouponVO>> getCouponPage(
            @Parameter(description = "优惠券名称") @RequestParam(required = false) String name,
            @Parameter(description = "优惠券类型") @RequestParam(required = false) String type,
            @Parameter(description = "启用状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CouponVO> page = couponService.getCouponPage(name, type, status, pageNum, pageSize);
        PageResult<CouponVO> result = PageResult.of(pageNum, pageSize, page.getTotal(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "发放优惠券", description = "向指定用户发放优惠券（管理员）")
    @PostMapping("/{couponId}/grant")
    public Result<Void> grantCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Parameter(description = "用户ID列表") @RequestBody List<Long> userIds) {
        couponService.grantCoupon(couponId, userIds);
        return Result.success("发放成功", null);
    }
}