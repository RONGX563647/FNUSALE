package com.fnusale.trade.service;

import com.fnusale.common.dto.trade.PaymentCreateDTO;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 发起支付
     */
    Map<String, Object> createPayment(PaymentCreateDTO dto);

    /**
     * 处理支付回调
     */
    void handlePayCallback(String payType, String callbackData);

    /**
     * 查询支付状态
     */
    Map<String, Object> queryPayStatus(Long orderId);

    /**
     * 申请退款
     */
    void applyRefund(Long orderId, String reason);

    /**
     * 处理退款回调
     */
    void handleRefundCallback(String payType, String callbackData);

    /**
     * 查询退款状态
     */
    Map<String, Object> queryRefundStatus(Long orderId);

    /**
     * 获取支付方式列表
     */
    List<Map<String, String>> getPaymentMethods();

    /**
     * 模拟支付确认（开发测试用）
     * @param payToken 支付token
     * @param success 是否支付成功
     */
    void mockPayConfirm(String payToken, Boolean success);

    /**
     * 获取模拟支付信息
     * @param payToken 支付token
     */
    Map<String, Object> getMockPayInfo(String payToken);
}