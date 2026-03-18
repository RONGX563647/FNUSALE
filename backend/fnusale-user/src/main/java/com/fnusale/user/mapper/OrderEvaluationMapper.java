package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.OrderEvaluation;
import com.fnusale.common.entity.UserRating;
import com.fnusale.common.vo.user.UserEvaluationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单评价Mapper
 * 注：评价数据来自订单模块的t_order_evaluation表
 */
@Mapper
public interface OrderEvaluationMapper extends BaseMapper<OrderEvaluation> {

    /**
     * 查询用户收到的评价列表
     */
    @Select("SELECT e.*, u.username as evaluator_name, u.avatar_url as evaluator_avatar " +
            "FROM t_order_evaluation e " +
            "LEFT JOIN t_user u ON e.evaluator_id = u.id " +
            "WHERE e.evaluated_id = #{userId} " +
            "ORDER BY e.create_time DESC")
    List<UserEvaluationVO> selectByEvaluatedId(@Param("userId") Long userId);

    /**
     * 查询用户发出的评价列表
     */
    @Select("SELECT e.*, u.username as evaluated_name, u.avatar_url as evaluated_avatar " +
            "FROM t_order_evaluation e " +
            "LEFT JOIN t_user u ON e.evaluated_id = u.id " +
            "WHERE e.evaluator_id = #{userId} " +
            "ORDER BY e.create_time DESC")
    List<UserEvaluationVO> selectByEvaluatorId(@Param("userId") Long userId);

    /**
     * 查询评价详情
     */
    @Select("SELECT e.*, u.username as evaluator_name, u.avatar_url as evaluator_avatar " +
            "FROM t_order_evaluation e " +
            "LEFT JOIN t_user u ON e.evaluator_id = u.id " +
            "WHERE e.id = #{id}")
    UserEvaluationVO selectVOById(@Param("id") Long id);

    /**
     * 检查是否已回复
     */
    @Select("SELECT COUNT(*) FROM t_order_evaluation WHERE id = #{id} AND reply_content IS NOT NULL")
    int hasReply(@Param("id") Long id);

    /**
     * 检查是否已追加评价
     */
    @Select("SELECT COUNT(*) FROM t_order_evaluation WHERE id = #{id} AND append_content IS NOT NULL")
    int hasAppend(@Param("id") Long id);

    /**
     * 更新追加评价
     */
    @org.apache.ibatis.annotations.Update("UPDATE t_order_evaluation SET append_content = #{appendContent}, " +
            "append_image_url = #{appendImageUrl}, append_time = NOW() WHERE id = #{id}")
    int updateAppend(@Param("id") Long id, @Param("appendContent") String appendContent,
                     @Param("appendImageUrl") String appendImageUrl);

    /**
     * 更新回复
     */
    @org.apache.ibatis.annotations.Update("UPDATE t_order_evaluation SET reply_content = #{replyContent}, " +
            "reply_time = NOW() WHERE id = #{id}")
    int updateReply(@Param("id") Long id, @Param("replyContent") String replyContent);
}