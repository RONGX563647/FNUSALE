package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.CaptchaDTO;
import com.fnusale.common.dto.user.CaptchaLoginDTO;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.service.CaptchaService;
import com.fnusale.user.service.UserService;
import com.fnusale.user.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供用户注册、登录、认证、信息管理等接口
 */
@Tag(name = "用户管理", description = "用户注册、登录、认证、信息管理等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;

    @Operation(summary = "用户注册（手机号）", description = "使用手机号注册新用户")
    @PostMapping("/register/phone")
    public Result<Void> registerByPhone(@Valid @RequestBody UserRegisterDTO dto) {
        userService.registerByPhone(dto);
        return Result.success("注册成功", null);
    }

    @Operation(summary = "用户注册（邮箱）", description = "使用邮箱注册新用户")
    @PostMapping("/register/email")
    public Result<Void> registerByEmail(@Valid @RequestBody UserRegisterDTO dto) {
        userService.registerByEmail(dto);
        return Result.success("注册成功", null);
    }

    @Operation(summary = "用户登录", description = "手机号/邮箱密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "验证码登录", description = "使用手机号/邮箱+验证码登录，用户不存在则自动注册")
    @PostMapping("/login/captcha")
    public Result<LoginVO> loginByCaptcha(@Valid @RequestBody CaptchaLoginDTO dto) {
        LoginVO loginVO = captchaService.loginByCaptcha(dto);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "发送验证码", description = "发送手机短信或邮箱验证码")
    @PostMapping("/captcha/send")
    public Result<Void> sendCaptcha(@Valid @RequestBody CaptchaDTO dto) {
        captchaService.sendCaptcha(dto);
        return Result.success("验证码发送成功", null);
    }

    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功", null);
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(
            @Parameter(description = "刷新令牌", required = true) @RequestParam String refreshToken) {
        LoginVO loginVO = userService.refreshToken(refreshToken);
        return Result.success("刷新成功", loginVO);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        UserVO userVO = userService.getCurrentUserInfo();
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的基本信息（用户名、头像、生日等）")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Parameter(description = "旧密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        userService.updatePassword(oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "校园身份认证", description = "提交学号/工号和校园卡/学生证进行身份认证")
    @PostMapping("/auth")
    public Result<Void> submitAuth(@Valid @RequestBody UserAuthDTO dto) {
        userService.submitAuth(dto);
        return Result.success("认证申请已提交，请等待审核", null);
    }

    @Operation(summary = "获取认证状态", description = "获取当前用户的认证状态")
    @GetMapping("/auth/status")
    public Result<UserVO> getAuthStatus() {
        UserVO userVO = userService.getAuthStatus();
        return Result.success(userVO);
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户公开信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新定位权限", description = "更新用户定位授权状态")
    @PutMapping("/location-permission")
    public Result<Void> updateLocationPermission(
            @Parameter(description = "定位权限状态(ALLOW/DENY)", required = true) @RequestParam String permission) {
        userService.updateLocationPermission(permission);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "校验定位是否在校园内", description = "校验用户当前定位是否在校园围栏内")
    @GetMapping("/location/verify")
    public Result<Boolean> verifyLocation(
            @Parameter(description = "经度", required = true) @RequestParam String longitude,
            @Parameter(description = "纬度", required = true) @RequestParam String latitude) {
        boolean result = userService.verifyLocation(longitude, latitude);
        return Result.success(result);
    }

    @Operation(summary = "获取我的发布列表", description = "获取当前用户发布的商品列表")
    @GetMapping("/my/products")
    public Result<PageResult<Object>> getMyProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 需要从请求头获取当前用户ID
        Long userId = UserServiceImpl.getCurrentUserId();
        PageResult<Object> result = userService.getMyProducts(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我的订单列表", description = "获取当前用户的订单列表")
    @GetMapping("/my/orders")
    public Result<PageResult<Object>> getMyOrders(
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserServiceImpl.getCurrentUserId();
        PageResult<Object> result = userService.getMyOrders(userId, status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我的收藏列表", description = "获取当前用户收藏的商品列表")
    @GetMapping("/my/favorites")
    public Result<PageResult<Object>> getMyFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserServiceImpl.getCurrentUserId();
        PageResult<Object> result = userService.getMyFavorites(userId, pageNum, pageSize);
        return Result.success(result);
    }
}