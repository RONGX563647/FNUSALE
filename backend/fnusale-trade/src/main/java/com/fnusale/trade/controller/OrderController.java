package com.fnusale.trade.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.trade.OrderCreateDTO;
import com.fnusale.common.vo.trade.OrderStatisticsVO;
import com.fnusale.common.vo.trade.OrderVO;
import com.fnusale.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单创建、查询、状态管理等接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "创建新订单")
    @PostMapping
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        Long orderId = orderService.createOrder(dto);
        return Result.success("创建成功", orderId);
    }

    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderById(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        OrderVO order = orderService.getOrderById(orderId);
        return Result.success(order);
    }

    @Operation(summary = "根据订单号查询", description = "根据订单编号查询订单")
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> getOrderByNo(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        OrderVO order = orderService.getOrderByNo(orderNo);
        return Result.success(order);
    }

    @Operation(summary = "获取我的订单列表", description = "获取当前用户的订单列表")
    @GetMapping("/my")
    public Result<PageResult<OrderVO>> getMyOrders(
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OrderVO> result = orderService.getMyOrders(status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "取消订单", description = "取消未支付的订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return Result.success("取消成功", null);
    }

    @Operation(summary = "确认收货", description = "买家确认收货")
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmReceipt(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        orderService.confirmReceipt(orderId);
        return Result.success("确认收货成功", null);
    }

    @Operation(summary = "申请退款", description = "申请订单退款")
    @PostMapping("/{orderId}/refund")
    public Result<Void> applyRefund(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "退款原因") @RequestParam String reason) {
        orderService.applyRefund(orderId, reason);
        return Result.success("申请成功", null);
    }

    @Operation(summary = "延长收货时间", description = "延长订单自动确认时间")
    @PutMapping("/{orderId}/extend")
    public Result<Void> extendReceiveTime(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        orderService.extendReceiveTime(orderId);
        return Result.success("延长成功", null);
    }

    @Operation(summary = "获取订单统计", description = "获取买家订单统计数据")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        OrderStatisticsVO statistics = orderService.getOrderStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取卖家订单列表", description = "获取卖家视角的订单列表")
    @GetMapping("/seller")
    public Result<PageResult<OrderVO>> getSellerOrders(
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OrderVO> result = orderService.getSellerOrders(status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "发货", description = "卖家标记商品已备好（自提场景）")
    @PutMapping("/{orderId}/ready")
    public Result<Void> markReady(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        orderService.markReady(orderId);
        return Result.success("标记成功", null);
    }
}