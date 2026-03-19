package com.fnusale.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单编号查询
     */
    @Select("SELECT * FROM t_order WHERE order_no = #{orderNo} AND is_deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据商品ID查询订单
     */
    @Select("SELECT * FROM t_order WHERE product_id = #{productId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    Order selectByProductId(@Param("productId") Long productId);

    /**
     * 统计用户各状态订单数量
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE user_id = #{userId} AND order_status = #{status} AND is_deleted = 0")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 统计卖家各状态订单数量
     */
    @Select("SELECT COUNT(*) FROM t_order o " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "WHERE p.user_id = #{sellerId} AND o.order_status = #{status} AND o.is_deleted = 0")
    int countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") String status);

    /**
     * 分页查询买家订单
     */
    @Select("SELECT o.*, p.product_name, p.product_desc, pi.image_url as product_image, " +
            "cpp.pick_point_name, u.username as seller_name, u.id as seller_id " +
            "FROM t_order o " +
            "LEFT JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_product_image pi ON p.id = pi.product_id AND pi.is_main_image = 1 " +
            "LEFT JOIN t_campus_pick_point cpp ON o.pick_point_id = cpp.id " +
            "LEFT JOIN t_user u ON p.user_id = u.id " +
            "WHERE o.user_id = #{userId} " +
            "AND (#{status} IS NULL OR o.order_status = #{status}) " +
            "AND o.is_deleted = 0 " +
            "ORDER BY o.create_time DESC")
    IPage<Order> selectPageByUserId(Page<Order> page,
            @Param("userId") Long userId, @Param("status") String status);

    /**
     * 分页查询卖家订单
     */
    @Select("SELECT o.*, p.product_name, p.product_desc, pi.image_url as product_image, " +
            "cpp.pick_point_name, u.username as buyer_name, u.id as buyer_id " +
            "FROM t_order o " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_product_image pi ON p.id = pi.product_id AND pi.is_main_image = 1 " +
            "LEFT JOIN t_campus_pick_point cpp ON o.pick_point_id = cpp.id " +
            "LEFT JOIN t_user u ON o.user_id = u.id " +
            "WHERE p.user_id = #{sellerId} " +
            "AND (#{status} IS NULL OR o.order_status = #{status}) " +
            "AND o.is_deleted = 0 " +
            "ORDER BY o.create_time DESC")
    IPage<Order> selectPageBySellerId(Page<Order> page,
            @Param("sellerId") Long sellerId, @Param("status") String status);

    /**
     * 更新订单状态
     */
    @Update("UPDATE t_order SET order_status = #{status}, update_time = NOW() " +
            "WHERE id = #{orderId} AND is_deleted = 0")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 支付成功更新
     */
    @Update("UPDATE t_order SET pay_status = 'PAID', order_status = 'WAIT_PICK', " +
            "update_time = NOW() WHERE id = #{orderId} AND is_deleted = 0")
    int updatePaySuccess(@Param("orderId") Long orderId);

    /**
     * 取消订单
     */
    @Update("UPDATE t_order SET order_status = 'CANCEL', cancel_reason = #{reason}, " +
            "update_time = NOW() WHERE id = #{orderId} AND is_deleted = 0")
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);

    /**
     * 确认收货
     */
    @Update("UPDATE t_order SET order_status = 'SUCCESS', success_time = NOW(), " +
            "update_time = NOW() WHERE id = #{orderId} AND is_deleted = 0")
    int confirmReceipt(@Param("orderId") Long orderId);

    /**
     * 退款成功更新
     */
    @Update("UPDATE t_order SET pay_status = 'REFUNDED', order_status = 'CANCEL', " +
            "update_time = NOW() WHERE id = #{orderId} AND is_deleted = 0")
    int updateRefundSuccess(@Param("orderId") Long orderId);

    /**
     * 查询超时未支付订单
     */
    @Select("SELECT * FROM t_order WHERE order_status = 'UNPAID' " +
            "AND create_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE) " +
            "AND is_deleted = 0 LIMIT #{limit}")
    java.util.List<Order> selectUnpaidTimeoutOrders(
            @Param("timeoutMinutes") int timeoutMinutes, @Param("limit") int limit);

    /**
     * 标记商品已备好
     */
    @Update("UPDATE t_order SET ready_time = NOW(), update_time = NOW() " +
            "WHERE id = #{orderId} AND is_deleted = 0")
    int markReady(@Param("orderId") Long orderId);

    /**
     * 延长收货时间
     */
    @Update("UPDATE t_order SET extend_receive_days = IFNULL(extend_receive_days, 0) + #{days}, " +
            "update_time = NOW() WHERE id = #{orderId} AND is_deleted = 0")
    int extendReceiveTime(@Param("orderId") Long orderId, @Param("days") int days);
}