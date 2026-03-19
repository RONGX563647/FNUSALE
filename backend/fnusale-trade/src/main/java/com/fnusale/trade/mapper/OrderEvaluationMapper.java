package com.fnusale.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.OrderEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单评价Mapper
 */
@Mapper
public interface OrderEvaluationMapper extends BaseMapper<OrderEvaluation> {

    /**
     * 根据订单ID查询评价
     */
    @Select("SELECT e.*, u.username as evaluator_name, p.product_name " +
            "FROM t_order_evaluation e " +
            "LEFT JOIN t_user u ON e.evaluator_id = u.id " +
            "LEFT JOIN t_order o ON e.order_id = o.id " +
            "LEFT JOIN t_product p ON o.product_id = p.id " +
            "WHERE e.order_id = #{orderId}")
    OrderEvaluation selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据商品ID分页查询评价
     */
    @Select("SELECT e.*, u.username as evaluator_name, u.avatar_url as evaluator_avatar " +
            "FROM t_order_evaluation e " +
            "INNER JOIN t_order o ON e.order_id = o.id " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_user u ON e.evaluator_id = u.id " +
            "WHERE p.id = #{productId} " +
            "ORDER BY e.create_time DESC")
    IPage<OrderEvaluation> selectPageByProductId(Page<OrderEvaluation> page,
            @Param("productId") Long productId);

    /**
     * 根据评价者ID分页查询
     */
    @Select("SELECT e.*, p.product_name, pi.image_url as product_image, u.username as evaluated_name " +
            "FROM t_order_evaluation e " +
            "INNER JOIN t_order o ON e.order_id = o.id " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_product_image pi ON p.id = pi.product_id AND pi.is_main_image = 1 " +
            "LEFT JOIN t_user u ON e.evaluated_id = u.id " +
            "WHERE e.evaluator_id = #{evaluatorId} " +
            "ORDER BY e.create_time DESC")
    IPage<OrderEvaluation> selectPageByEvaluatorId(Page<OrderEvaluation> page,
            @Param("evaluatorId") Long evaluatorId);

    /**
     * 根据被评价者ID分页查询
     */
    @Select("SELECT e.*, u.username as evaluator_name, u.avatar_url as evaluator_avatar, " +
            "p.product_name, pi.image_url as product_image " +
            "FROM t_order_evaluation e " +
            "INNER JOIN t_order o ON e.order_id = o.id " +
            "INNER JOIN t_product p ON o.product_id = p.id " +
            "LEFT JOIN t_product_image pi ON p.id = pi.product_id AND pi.is_main_image = 1 " +
            "LEFT JOIN t_user u ON e.evaluator_id = u.id " +
            "WHERE e.evaluated_id = #{evaluatedId} " +
            "ORDER BY e.create_time DESC")
    IPage<OrderEvaluation> selectPageByEvaluatedId(Page<OrderEvaluation> page,
            @Param("evaluatedId") Long evaluatedId);

    /**
     * 检查订单是否已评价
     */
    @Select("SELECT COUNT(*) FROM t_order_evaluation WHERE order_id = #{orderId}")
    int countByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计用户收到的评价数
     */
    @Select("SELECT COUNT(*) FROM t_order_evaluation WHERE evaluated_id = #{userId}")
    int countByEvaluatedId(@Param("userId") Long userId);

    /**
     * 统计用户好评数
     */
    @Select("SELECT COUNT(*) FROM t_order_evaluation WHERE evaluated_id = #{userId} AND score >= 4")
    int countPositiveByEvaluatedId(@Param("userId") Long userId);

    /**
     * 计算用户平均评分
     */
    @Select("SELECT IFNULL(AVG(score), 0) FROM t_order_evaluation WHERE evaluated_id = #{userId}")
    Double avgScoreByEvaluatedId(@Param("userId") Long userId);
}