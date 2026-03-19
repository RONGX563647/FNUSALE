package com.fnusale.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.TradeDispute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 交易纠纷Mapper
 */
@Mapper
public interface TradeDisputeMapper extends BaseMapper<TradeDispute> {

    /**
     * 根据订单ID查询纠纷
     */
    @Select("SELECT * FROM t_trade_dispute WHERE order_id = #{orderId} ORDER BY create_time DESC LIMIT 1")
    TradeDispute selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 分页查询用户相关纠纷
     */
    @Select("SELECT d.*, o.order_no, p.product_name, u1.username as initiator_name, " +
            "u2.username as accused_name " +
            "FROM t_trade_dispute d " +
            "INNER JOIN t_order o ON d.order_id = o.id " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_user u1 ON d.initiator_id = u1.id " +
            "LEFT JOIN t_user u2 ON d.accused_id = u2.id " +
            "WHERE (d.initiator_id = #{userId} OR d.accused_id = #{userId}) " +
            "AND (#{status} IS NULL OR d.dispute_status = #{status}) " +
            "ORDER BY d.create_time DESC")
    IPage<TradeDispute> selectPageByUserId(Page<TradeDispute> page,
            @Param("userId") Long userId, @Param("status") String status);

    /**
     * 分页查询待处理纠纷（管理员用）
     */
    @Select("SELECT d.*, o.order_no, p.product_name, u1.username as initiator_name, " +
            "u2.username as accused_name " +
            "FROM t_trade_dispute d " +
            "INNER JOIN t_order o ON d.order_id = o.id " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_user u1 ON d.initiator_id = u1.id " +
            "LEFT JOIN t_user u2 ON d.accused_id = u2.id " +
            "WHERE (#{status} IS NULL OR d.dispute_status = #{status}) " +
            "ORDER BY d.create_time DESC")
    IPage<TradeDispute> selectPageByStatus(Page<TradeDispute> page, @Param("status") String status);

    /**
     * 检查订单是否存在未解决纠纷
     */
    @Select("SELECT COUNT(*) FROM t_trade_dispute WHERE order_id = #{orderId} " +
            "AND dispute_status != 'RESOLVED'")
    int countUnresolvedByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计各状态纠纷数
     */
    @Select("SELECT COUNT(*) FROM t_trade_dispute WHERE dispute_status = #{status}")
    int countByStatus(@Param("status") String status);
}