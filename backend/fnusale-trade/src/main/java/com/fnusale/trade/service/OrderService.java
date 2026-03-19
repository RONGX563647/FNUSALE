package com.fnusale.trade.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.OrderCreateDTO;
import com.fnusale.common.vo.trade.OrderStatisticsVO;
import com.fnusale.common.vo.trade.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Long createOrder(OrderCreateDTO dto);

    /**
     * 根据ID获取订单详情
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 根据订单号获取订单详情
     */
    OrderVO getOrderByNo(String orderNo);

    /**
     * 获取买家订单列表
     */
    PageResult<OrderVO> getMyOrders(String status, Integer pageNum, Integer pageSize);

    /**
     * 获取卖家订单列表
     */
    PageResult<OrderVO> getSellerOrders(String status, Integer pageNum, Integer pageSize);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * 确认收货
     */
    void confirmReceipt(Long orderId);

    /**
     * 卖家标记商品已备好
     */
    void markReady(Long orderId);

    /**
     * 延长收货时间
     */
    void extendReceiveTime(Long orderId);

    /**
     * 申请退款
     */
    void applyRefund(Long orderId, String reason);

    /**
     * 获取订单统计
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 支付成功回调处理
     */
    void handlePaySuccess(Long orderId);

    /**
     * 退款成功回调处理
     */
    void handleRefundSuccess(Long orderId);
}