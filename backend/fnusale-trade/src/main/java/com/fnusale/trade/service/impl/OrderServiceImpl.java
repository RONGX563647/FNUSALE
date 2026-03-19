package com.fnusale.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.OrderCreateDTO;
import com.fnusale.common.entity.Order;
import com.fnusale.common.entity.Product;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.PayStatus;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.trade.OrderStatisticsVO;
import com.fnusale.common.vo.trade.OrderVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.trade.client.MarketingClient;
import com.fnusale.trade.client.ProductClient;
import com.fnusale.trade.client.UserClient;
import com.fnusale.trade.event.*;
import com.fnusale.trade.mapper.OrderMapper;
import com.fnusale.trade.mq.producer.OrderMessageProducer;
import com.fnusale.trade.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final MarketingClient marketingClient;
    private final StringRedisTemplate redisTemplate;
    private final OrderMessageProducer orderMessageProducer;

    private static final String ORDER_NO_KEY_PREFIX = "order:no:";
    private static final int UNPAID_TIMEOUT_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        // 1. 校验用户认证状态
        var authResult = userClient.getAuthStatus(userId);
        if (!authResult.isSuccess() || !AuthStatus.AUTH_SUCCESS.getCode().equals(authResult.getData())) {
            throw new BusinessException("请先完成校园身份认证");
        }

        // 2. 获取商品信息
        var productResult = productClient.getProductById(dto.getProductId());
        if (!productResult.isSuccess() || productResult.getData() == null) {
            throw new BusinessException("商品不存在");
        }
        ProductVO product = productResult.getData();

        // 3. 校验商品状态
        if (!ProductStatus.ON_SHELF.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品已下架或已售出");
        }

        // 4. 禁止购买自己的商品
        if (userId.equals(product.getUserId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 5. 计算价格
        BigDecimal productPrice = product.getPrice();
        BigDecimal couponDeductAmount = BigDecimal.ZERO;

        // 6. 优惠券校验和计算
        if (dto.getCouponId() != null) {
            var validateResult = marketingClient.validateCoupon(
                    userId, dto.getCouponId(), product.getCategoryId(), productPrice);
            if (validateResult.isSuccess() && Boolean.TRUE.equals(validateResult.getData())) {
                // 获取优惠券信息计算抵扣金额
                var couponResult = marketingClient.getCouponById(dto.getCouponId());
                if (couponResult.isSuccess() && couponResult.getData() != null) {
                    var coupon = couponResult.getData();
                    // 检查满减条件
                    if (coupon.getFullAmount() == null || productPrice.compareTo(coupon.getFullAmount()) >= 0) {
                        couponDeductAmount = coupon.getReduceAmount() != null ? coupon.getReduceAmount() : BigDecimal.ZERO;
                    }
                }
            }
        }

        BigDecimal actualPayAmount = productPrice.subtract(couponDeductAmount);
        if (actualPayAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualPayAmount = BigDecimal.ZERO;
        }

        // 7. 生成订单编号
        String orderNo = generateOrderNo();

        // 8. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(dto.getProductId());
        order.setProductPrice(productPrice);
        order.setCouponDeductAmount(couponDeductAmount);
        order.setActualPayAmount(actualPayAmount);
        order.setPickPointId(dto.getPickPointId() != null ? dto.getPickPointId() : product.getPickPointId());
        order.setPayType(dto.getPayType());
        order.setPayStatus(PayStatus.UNPAID.getCode());
        order.setOrderStatus(OrderStatus.UNPAID.getCode());
        order.setCouponId(dto.getCouponId());

        orderMapper.insert(order);

        // 9. 发送订单超时取消消息（30分钟后检查）
        orderMessageProducer.sendOrderTimeoutMessage(order.getId(), orderNo, 30);

        // 10. 发送订单创建通知（通知卖家）
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(order.getId())
                .orderNo(orderNo)
                .buyerId(userId)
                .sellerId(product.getUserId())
                .productId(dto.getProductId())
                .eventId(java.util.UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .build();
        orderMessageProducer.sendOrderCreateNotifyMessage(orderEvent);

        log.info("订单创建成功，orderId: {}, orderNo: {}, userId: {}", order.getId(), orderNo, userId);
        return order.getId();
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验权限（买家或卖家才能查看）
        var productResult = productClient.getProductById(order.getProductId());
        if (!order.getUserId().equals(userId)) {
            if (!productResult.isSuccess() || !userId.equals(productResult.getData().getUserId())) {
                throw new BusinessException("无权查看该订单");
            }
        }

        return buildOrderVO(order);
    }

    @Override
    public OrderVO getOrderByNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderVO(order);
    }

    @Override
    public PageResult<OrderVO> getMyOrders(String status, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = orderMapper.selectPageByUserId(page, userId, status);
        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, orderPage.getTotal(), voList);
    }

    @Override
    public PageResult<OrderVO> getSellerOrders(String status, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = orderMapper.selectPageBySellerId(page, userId, status);
        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, orderPage.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家能取消订单，且只有待付款状态可取消
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权取消该订单");
        }
        if (!OrderStatus.UNPAID.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        orderMapper.cancelOrder(orderId, reason != null ? reason : "买家取消");
        log.info("订单取消成功，orderId: {}, reason: {}", orderId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家能确认收货，且只有待自提状态可确认
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可确认收货");
        }

        orderMapper.confirmReceipt(orderId);

        // 获取商品信息
        var productResult = productClient.getProductById(order.getProductId());
        String productName = "";
        Long sellerId = null;
        if (productResult.isSuccess() && productResult.getData() != null) {
            productName = productResult.getData().getProductName();
            sellerId = productResult.getData().getUserId();
        }

        // 发送订单完成消息（异步更新商品状态、通知卖家）
        OrderCompleteEvent completeEvent = OrderCompleteEvent.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .buyerId(userId)
                .sellerId(sellerId)
                .productId(order.getProductId())
                .amount(order.getActualPayAmount())
                .productName(productName)
                .eventId(java.util.UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .build();
        orderMessageProducer.sendOrderCompleteMessage(completeEvent);

        log.info("确认收货成功，orderId: {}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReady(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验是否为卖家
        var productResult = productClient.getProductById(order.getProductId());
        if (!productResult.isSuccess() || !userId.equals(productResult.getData().getUserId())) {
            throw new BusinessException("无权操作该订单");
        }

        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可操作");
        }

        // 标记商品已备好
        orderMapper.markReady(orderId);
        log.info("卖家标记商品已备好，orderId: {}", orderId);
    }

    @Override
    public void extendReceiveTime(Long orderId) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可延长收货时间");
        }

        // 检查延长次数限制（最多延长2次，每次3天）
        int currentExtendDays = order.getExtendReceiveDays() != null ? order.getExtendReceiveDays() : 0;
        int extendCount = currentExtendDays / 3;
        if (extendCount >= 2) {
            throw new BusinessException("已达延长收货次数上限");
        }

        // 延长3天
        orderMapper.extendReceiveTime(orderId, 3);
        log.info("延长收货时间成功，orderId: {}, 已延长天数: {}", orderId, currentExtendDays + 3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long orderId, String reason) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可申请退款");
        }

        // 获取卖家ID
        Long sellerId = null;
        var productResult = productClient.getProductById(order.getProductId());
        if (productResult.isSuccess() && productResult.getData() != null) {
            sellerId = productResult.getData().getUserId();
        }

        // 发送退款处理消息（异步处理）
        OrderRefundEvent refundEvent = OrderRefundEvent.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .buyerId(userId)
                .sellerId(sellerId)
                .productId(order.getProductId())
                .refundAmount(order.getActualPayAmount())
                .reason(reason)
                .payType(order.getPayType())
                .eventId(java.util.UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .build();
        orderMessageProducer.sendRefundMessage(refundEvent);

        log.info("退款申请已提交，orderId: {}, reason: {}", orderId, reason);
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        Long userId = UserContext.getUserIdOrThrow();

        int unpaidCount = orderMapper.countByUserIdAndStatus(userId, OrderStatus.UNPAID.getCode());
        int waitPickCount = orderMapper.countByUserIdAndStatus(userId, OrderStatus.WAIT_PICK.getCode());
        int successCount = orderMapper.countByUserIdAndStatus(userId, OrderStatus.SUCCESS.getCode());
        int cancelCount = orderMapper.countByUserIdAndStatus(userId, OrderStatus.CANCEL.getCode());

        return OrderStatisticsVO.builder()
                .unpaidCount(unpaidCount)
                .waitPickCount(waitPickCount)
                .successCount(successCount)
                .cancelCount(cancelCount)
                .refundCount(0)
                .totalCount(unpaidCount + waitPickCount + successCount + cancelCount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(Long orderId) {
        int rows = orderMapper.updatePaySuccess(orderId);
        if (rows > 0) {
            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                // 获取卖家ID
                Long sellerId = null;
                var productResult = productClient.getProductById(order.getProductId());
                if (productResult.isSuccess() && productResult.getData() != null) {
                    sellerId = productResult.getData().getUserId();
                }

                // 发送支付成功消息（异步处理优惠券核销、通知等）
                OrderPayEvent payEvent = OrderPayEvent.builder()
                        .orderId(orderId)
                        .orderNo(order.getOrderNo())
                        .buyerId(order.getUserId())
                        .sellerId(sellerId)
                        .productId(order.getProductId())
                        .payAmount(order.getActualPayAmount())
                        .payType(order.getPayType())
                        .couponId(order.getCouponId())
                        .couponDeductAmount(order.getCouponDeductAmount())
                        .eventId(java.util.UUID.randomUUID().toString())
                        .eventTime(LocalDateTime.now())
                        .build();
                orderMessageProducer.sendPaySuccessMessage(payEvent);
            }
            log.info("支付成功，订单状态更新，orderId: {}", orderId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundSuccess(Long orderId) {
        int rows = orderMapper.updateRefundSuccess(orderId);
        if (rows > 0) {
            log.info("退款成功，订单状态更新，orderId: {}", orderId);
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = ORDER_NO_KEY_PREFIX + dateStr;
        Long seq = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 2, TimeUnit.DAYS);
        return "XS" + dateStr + String.format("%06d", seq);
    }

    /**
     * 构建订单VO
     */
    private OrderVO buildOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        // 获取商品信息
        var productResult = productClient.getProductById(order.getProductId());
        if (productResult.isSuccess() && productResult.getData() != null) {
            ProductVO product = productResult.getData();
            vo.setProductName(product.getProductName());
            vo.setProductImage(product.getMainImageUrl());
            vo.setSellerId(product.getUserId());

            // 获取卖家信息
            var sellerResult = userClient.getUserById(product.getUserId());
            if (sellerResult.isSuccess() && sellerResult.getData() != null) {
                vo.setSellerName(sellerResult.getData().getUsername());
            }
        }

        return vo;
    }
}