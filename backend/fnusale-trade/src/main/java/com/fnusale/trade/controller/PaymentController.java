package com.fnusale.trade.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.PaymentCreateDTO;
import com.fnusale.trade.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Tag(name = "支付管理", description = "支付、退款等接口")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "发起支付", description = "创建支付订单")
    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(@Valid @RequestBody PaymentCreateDTO dto) {
        Map<String, Object> result = paymentService.createPayment(dto);
        return Result.success("创建成功", result);
    }

    @Operation(summary = "支付回调", description = "支付成功回调接口（第三方支付调用）")
    @PostMapping("/callback/{payType}")
    public String paymentCallback(
            @Parameter(description = "支付方式") @PathVariable String payType,
            @RequestBody String data) {
        paymentService.handlePayCallback(payType, data);
        return "success";
    }

    @Operation(summary = "查询支付状态", description = "查询订单支付状态")
    @GetMapping("/status/{orderId}")
    public Result<Map<String, Object>> queryPaymentStatus(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.queryPayStatus(orderId);
        return Result.success(result);
    }

    @Operation(summary = "申请退款", description = "申请订单退款")
    @PostMapping("/refund")
    public Result<Void> applyRefund(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "退款原因") @RequestParam String reason) {
        paymentService.applyRefund(orderId, reason);
        return Result.success("申请成功", null);
    }

    @Operation(summary = "退款回调", description = "退款成功回调接口")
    @PostMapping("/refund/callback/{payType}")
    public String refundCallback(
            @Parameter(description = "支付方式") @PathVariable String payType,
            @RequestBody String data) {
        paymentService.handleRefundCallback(payType, data);
        return "success";
    }

    @Operation(summary = "查询退款状态", description = "查询退款状态")
    @GetMapping("/refund/status/{orderId}")
    public Result<Map<String, Object>> queryRefundStatus(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.queryRefundStatus(orderId);
        return Result.success(result);
    }

    @Operation(summary = "获取支付方式列表", description = "获取系统支持的支付方式")
    @GetMapping("/methods")
    public Result<List<Map<String, String>>> getPaymentMethods() {
        List<Map<String, String>> methods = paymentService.getPaymentMethods();
        return Result.success(methods);
    }

    // ==================== 模拟支付接口（开发测试用） ====================

    @Operation(summary = "获取模拟支付信息", description = "根据支付Token获取支付详情")
    @GetMapping("/mock/info/{payToken}")
    public Result<Map<String, Object>> getMockPayInfo(
            @Parameter(description = "支付Token") @PathVariable String payToken) {
        Map<String, Object> result = paymentService.getMockPayInfo(payToken);
        return Result.success(result);
    }

    @Operation(summary = "模拟支付确认", description = "确认模拟支付结果（成功/失败）")
    @PostMapping("/mock/confirm")
    public Result<Void> mockPayConfirm(
            @Parameter(description = "支付Token") @RequestParam String payToken,
            @Parameter(description = "是否支付成功") @RequestParam(defaultValue = "true") Boolean success) {
        paymentService.mockPayConfirm(payToken, success);
        return Result.success(success ? "支付成功" : "支付失败", null);
    }
}