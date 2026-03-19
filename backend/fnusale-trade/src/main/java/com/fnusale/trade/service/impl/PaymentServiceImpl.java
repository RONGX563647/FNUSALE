package com.fnusale.trade.service.impl;

import com.fnusale.common.dto.trade.PaymentCreateDTO;
import com.fnusale.common.entity.Order;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.PayStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.trade.mapper.OrderMapper;
import com.fnusale.trade.service.OrderService;
import com.fnusale.trade.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现
 * 使用模拟支付，支持开发测试环境
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 模拟支付Token前缀
     */
    private static final String MOCK_PAY_TOKEN_PREFIX = "mock:pay:token:";
    /**
     * 模拟支付Token有效期（分钟）
     */
    private static final int MOCK_PAY_TOKEN_EXPIRE_MINUTES = 15;

    @Override
    public Map<String, Object> createPayment(PaymentCreateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }

        if (!OrderStatus.UNPAID.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("订单状态异常，无法支付");
        }

        if (!PayStatus.UNPAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException("订单支付状态异常");
        }

        // 校验支付方式
        if (!Arrays.asList("WECHAT", "ALIPAY", "CAMPUS_CARD").contains(dto.getPayType())) {
            throw new BusinessException("不支持的支付方式");
        }

        // 生成模拟支付Token
        String payToken = generatePayToken(order, dto.getPayType());

        // 更新订单支付方式
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setPayType(dto.getPayType());
        orderMapper.updateById(updateOrder);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("payToken", payToken);
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getActualPayAmount());
        result.put("payType", dto.getPayType());
        result.put("payTypeName", getPayTypeName(dto.getPayType()));
        // 模拟支付页面URL（前端可跳转到模拟支付页面）
        result.put("payUrl", "/mock-pay?token=" + payToken);
        result.put("expireMinutes", MOCK_PAY_TOKEN_EXPIRE_MINUTES);

        log.info("创建模拟支付订单成功，orderId: {}, payType: {}, payToken: {}",
                order.getId(), dto.getPayType(), payToken);
        return result;
    }

    @Override
    public void mockPayConfirm(String payToken, Boolean success) {
        // 从Redis获取支付信息
        String tokenKey = MOCK_PAY_TOKEN_PREFIX + payToken;
        String payInfo = redisTemplate.opsForValue().get(tokenKey);

        if (payInfo == null) {
            throw new BusinessException("支付Token已过期或不存在");
        }

        // 解析支付信息：orderId:payType:userId
        String[] parts = payInfo.split(":");
        if (parts.length != 3) {
            throw new BusinessException("支付Token无效");
        }

        Long orderId = Long.parseLong(parts[0]);
        String payType = parts[1];
        Long userId = Long.parseLong(parts[2]);

        // 验证当前用户
        Long currentUserId = UserContext.getUserIdOrThrow();
        if (!userId.equals(currentUserId)) {
            throw new BusinessException("无权操作该支付");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 幂等性检查
        if (PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException("订单已支付");
        }

        if (!OrderStatus.UNPAID.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("订单状态已变更，无法支付");
        }

        if (Boolean.TRUE.equals(success)) {
            // 支付成功
            orderService.handlePaySuccess(orderId);
            // 删除支付Token
            redisTemplate.delete(tokenKey);
            log.info("模拟支付成功，orderId: {}, payType: {}", orderId, payType);
        } else {
            // 支付失败，记录日志，Token保留供重试
            log.warn("模拟支付失败，orderId: {}, payType: {}", orderId, payType);
            throw new BusinessException("支付失败，请重试");
        }
    }

    @Override
    public Map<String, Object> getMockPayInfo(String payToken) {
        String tokenKey = MOCK_PAY_TOKEN_PREFIX + payToken;
        String payInfo = redisTemplate.opsForValue().get(tokenKey);

        if (payInfo == null) {
            throw new BusinessException("支付Token已过期或不存在");
        }

        String[] parts = payInfo.split(":");
        if (parts.length != 3) {
            throw new BusinessException("支付Token无效");
        }

        Long orderId = Long.parseLong(parts[0]);
        String payType = parts[1];

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getActualPayAmount());
        result.put("payType", payType);
        result.put("payTypeName", getPayTypeName(payType));
        result.put("orderStatus", order.getOrderStatus());
        result.put("payStatus", order.getPayStatus());

        return result;
    }

    @Override
    public void handlePayCallback(String payType, String callbackData) {
        // 真实支付回调入口，模拟支付不使用此方法
        log.info("收到支付回调，payType: {}, data: {}", payType, callbackData);

        String orderNo = parseOrderNoFromCallback(callbackData);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回调订单不存在，orderNo: {}", orderNo);
            return;
        }

        if (PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            log.info("订单已支付，跳过处理，orderNo: {}", orderNo);
            return;
        }

        orderService.handlePaySuccess(order.getId());
    }

    @Override
    public Map<String, Object> queryPayStatus(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", order.getOrderNo());
        result.put("payStatus", order.getPayStatus());
        result.put("orderStatus", order.getOrderStatus());
        result.put("payStatusDesc", getPayStatusDesc(order.getPayStatus()));

        return result;
    }

    @Override
    public void applyRefund(Long orderId, String reason) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }

        if (!PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException("订单未支付，无法退款");
        }

        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态无法退款");
        }

        // 模拟退款：直接更新订单状态
        orderService.handleRefundSuccess(orderId);
        log.info("模拟退款成功，orderId: {}, reason: {}", orderId, reason);
    }

    @Override
    public void handleRefundCallback(String payType, String callbackData) {
        log.info("收到退款回调，payType: {}", payType);

        String orderNo = parseOrderNoFromCallback(callbackData);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("退款回调订单不存在，orderNo: {}", orderNo);
            return;
        }

        orderService.handleRefundSuccess(order.getId());
    }

    @Override
    public Map<String, Object> queryRefundStatus(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("refundStatus", PayStatus.REFUNDED.getCode().equals(order.getPayStatus()) ? "SUCCESS" : "PENDING");

        return result;
    }

    @Override
    public List<Map<String, String>> getPaymentMethods() {
        List<Map<String, String>> methods = new ArrayList<>();

        Map<String, String> wechat = new HashMap<>();
        wechat.put("payType", "WECHAT");
        wechat.put("payName", "微信支付");
        wechat.put("icon", "wechat");
        methods.add(wechat);

        Map<String, String> alipay = new HashMap<>();
        alipay.put("payType", "ALIPAY");
        alipay.put("payName", "支付宝");
        alipay.put("icon", "alipay");
        methods.add(alipay);

        Map<String, String> campusCard = new HashMap<>();
        campusCard.put("payType", "CAMPUS_CARD");
        campusCard.put("payName", "校园卡");
        campusCard.put("icon", "campus-card");
        methods.add(campusCard);

        return methods;
    }

    /**
     * 生成模拟支付Token
     */
    private String generatePayToken(Order order, String payType) {
        String payToken = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = MOCK_PAY_TOKEN_PREFIX + payToken;
        // 存储格式：orderId:payType:userId
        String payInfo = order.getId() + ":" + payType + ":" + order.getUserId();

        redisTemplate.opsForValue().set(tokenKey, payInfo, MOCK_PAY_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return payToken;
    }

    /**
     * 获取支付方式名称
     */
    private String getPayTypeName(String payType) {
        switch (payType) {
            case "WECHAT":
                return "微信支付";
            case "ALIPAY":
                return "支付宝";
            case "CAMPUS_CARD":
                return "校园卡";
            default:
                return payType;
        }
    }

    /**
     * 获取支付状态描述
     */
    private String getPayStatusDesc(String payStatus) {
        if (PayStatus.UNPAID.getCode().equals(payStatus)) {
            return "未支付";
        } else if (PayStatus.PAID.getCode().equals(payStatus)) {
            return "已支付";
        } else if (PayStatus.REFUNDED.getCode().equals(payStatus)) {
            return "已退款";
        }
        return payStatus;
    }

    /**
     * 从回调数据中解析订单号（真实支付时使用）
     */
    private String parseOrderNoFromCallback(String callbackData) {
        // 简单实现：直接返回订单号
        // 真实支付需解析回调数据
        return callbackData;
    }
}