package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供用户注册、登录、认证、信息管理等接口
 */
@Tag(name = "用户管理", description = "用户注册、登录、认证、信息管理等接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Operation(summary = "用户注册（手机号）", description = "使用手机号注册新用户")
    @PostMapping("/register/phone")
    public Result<Void> registerByPhone(@Valid @RequestBody UserRegisterDTO dto) {
        // TODO: 实现用户注册逻辑
        return Result.success();
    }

    @Operation(summary = "用户注册（邮箱）", description = "使用邮箱注册新用户")
    @PostMapping("/register/email")
    public Result<Void> registerByEmail(@Valid @RequestBody UserRegisterDTO dto) {
        // TODO: 实现用户注册逻辑
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "手机号/邮箱密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        // TODO: 实现用户登录逻辑
        return Result.success();
    }

    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 实现用户登出逻辑
        return Result.success();
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(
            @Parameter(description = "刷新令牌", required = true) @RequestParam String refreshToken) {
        // TODO: 实现刷新Token逻辑
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        // TODO: 实现获取当前用户信息逻辑
        return Result.success();
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的基本信息（用户名、头像、生日等）")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@Valid @RequestBody UserUpdateDTO dto) {
        // TODO: 实现更新用户信息逻辑
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Parameter(description = "旧密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        // TODO: 实现修改密码逻辑
        return Result.success();
    }

    @Operation(summary = "校园身份认证", description = "提交学号/工号和校园卡/学生证进行身份认证")
    @PostMapping("/auth")
    public Result<Void> submitAuth(@Valid @RequestBody UserAuthDTO dto) {
        // TODO: 实现提交认证逻辑
        return Result.success();
    }

    @Operation(summary = "获取认证状态", description = "获取当前用户的认证状态")
    @GetMapping("/auth/status")
    public Result<UserVO> getAuthStatus() {
        // TODO: 实现获取认证状态逻辑
        return Result.success();
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户公开信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        // TODO: 实现获取用户详情逻辑
        return Result.success();
    }

    @Operation(summary = "更新定位权限", description = "更新用户定位授权状态")
    @PutMapping("/location-permission")
    public Result<Void> updateLocationPermission(
            @Parameter(description = "定位权限状态(ALLOW/DENY)", required = true) @RequestParam String permission) {
        // TODO: 实现更新定位权限逻辑
        return Result.success();
    }

    @Operation(summary = "校验定位是否在校园内", description = "校验用户当前定位是否在校园围栏内")
    @GetMapping("/location/verify")
    public Result<Boolean> verifyLocation(
            @Parameter(description = "经度", required = true) @RequestParam String longitude,
            @Parameter(description = "纬度", required = true) @RequestParam String latitude) {
        // TODO: 实现校验定位逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的发布列表", description = "获取当前用户发布的商品列表")
    @GetMapping("/my/products")
    public Result<PageResult<Object>> getMyProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取我的发布列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的订单列表", description = "获取当前用户的订单列表")
    @GetMapping("/my/orders")
    public Result<PageResult<Object>> getMyOrders(
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取我的订单列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取我的收藏列表", description = "获取当前用户收藏的商品列表")
    @GetMapping("/my/favorites")
    public Result<PageResult<Object>> getMyFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取我的收藏列表逻辑
        return Result.success();
    }
}