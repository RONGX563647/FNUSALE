package com.fnusale.admin.controller;

import com.fnusale.admin.service.AdminAuthService;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.admin.AdminLoginDTO;
import com.fnusale.common.vo.admin.AdminInfoVO;
import com.fnusale.common.vo.admin.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 */
@Tag(name = "管理员认证", description = "管理员登录、登出、获取信息等接口")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录", description = "使用用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Result<AdminLoginVO> login(
            @Valid @RequestBody AdminLoginDTO dto,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        AdminLoginVO loginVO = adminAuthService.login(dto, ip);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "管理员登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        adminAuthService.logout();
        return Result.success("登出成功", null);
    }

    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息")
    @GetMapping("/info")
    public Result<AdminInfoVO> getAdminInfo() {
        AdminInfoVO adminInfo = adminAuthService.getAdminInfo();
        return Result.success(adminInfo);
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<AdminLoginVO> refreshToken(
            @Parameter(description = "刷新令牌", required = true) @RequestParam String refreshToken) {
        AdminLoginVO loginVO = adminAuthService.refreshToken(refreshToken);
        return Result.success("刷新成功", loginVO);
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
