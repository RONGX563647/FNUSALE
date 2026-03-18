package com.fnusale.admin.controller;

import com.fnusale.admin.service.AdminUserService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.admin.UserQueryDTO;
import com.fnusale.common.vo.admin.UserDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户审核、管理等接口（管理员）")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @GetMapping("/page")
    public Result<PageResult<UserDetailVO>> getUserPage(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "认证状态") @RequestParam(required = false) String authStatus,
            @Parameter(description = "身份类型") @RequestParam(required = false) String identityType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        UserQueryDTO query = new UserQueryDTO();
        query.setUsername(username);
        query.setAuthStatus(authStatus);
        query.setIdentityType(identityType);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(adminUserService.getUserPage(query));
    }

    @Operation(summary = "获取用户详情", description = "获取用户详细信息")
    @GetMapping("/{userId}")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    @Operation(summary = "获取待审核认证列表", description = "获取待审核的校园认证列表")
    @GetMapping("/auth/pending")
    public Result<PageResult<UserDetailVO>> getPendingAuthList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminUserService.getPendingAuthList(pageNum, pageSize));
    }

    @Operation(summary = "审核通过认证", description = "审核通过用户认证")
    @PutMapping("/auth/{userId}/pass")
    public Result<Void> authPass(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long adminId = 1L;
        adminUserService.authPass(userId, adminId);
        return Result.success();
    }

    @Operation(summary = "审核驳回认证", description = "驳回用户认证并填写原因")
    @PutMapping("/auth/{userId}/reject")
    public Result<Void> authReject(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "驳回原因") @RequestParam String reason) {
        Long adminId = 1L;
        adminUserService.authReject(userId, adminId, reason);
        return Result.success();
    }

    @Operation(summary = "封禁用户", description = "封禁指定用户")
    @PutMapping("/{userId}/ban")
    public Result<Void> banUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "封禁原因") @RequestParam String reason) {
        Long adminId = 1L;
        adminUserService.banUser(userId, adminId, reason);
        return Result.success();
    }

    @Operation(summary = "解封用户", description = "解封指定用户")
    @PutMapping("/{userId}/unban")
    public Result<Void> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long adminId = 1L;
        adminUserService.unbanUser(userId, adminId);
        return Result.success();
    }

    @Operation(summary = "调整信誉分", description = "调整用户信誉分")
    @PutMapping("/{userId}/credit")
    public Result<Map<String, Integer>> adjustCredit(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "调整分数(正数加分,负数减分)") @RequestParam Integer score,
            @Parameter(description = "调整原因") @RequestParam String reason) {
        Long adminId = 1L;
        Integer newScore = adminUserService.adjustCredit(userId, score, reason, adminId);
        Map<String, Integer> result = new HashMap<>();
        result.put("newScore", newScore);
        return Result.success(result);
    }

    @Operation(summary = "获取用户认证记录", description = "获取用户认证审核记录")
    @GetMapping("/auth/{userId}/records")
    public Result<UserDetailVO> getAuthRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }
}