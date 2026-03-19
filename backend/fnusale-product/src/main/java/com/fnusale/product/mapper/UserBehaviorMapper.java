package com.fnusale.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户行为Mapper
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 查询用户对商品的行为
     */
    @Select("SELECT * FROM t_user_behavior WHERE user_id = #{userId} AND product_id = #{productId} AND behavior_type = #{behaviorType}")
    UserBehavior selectByUserProductBehavior(@Param("userId") Long userId, @Param("productId") Long productId, @Param("behaviorType") String behaviorType);

    /**
     * 查询用户收藏的商品ID列表
     */
    @Select("SELECT product_id FROM t_user_behavior WHERE user_id = #{userId} AND behavior_type = 'COLLECT' ORDER BY behavior_time DESC")
    List<Long> selectFavoriteProductIds(@Param("userId") Long userId);

    /**
     * 查询用户点赞的商品ID列表
     */
    @Select("SELECT product_id FROM t_user_behavior WHERE user_id = #{userId} AND behavior_type = 'LIKE' ORDER BY behavior_time DESC")
    List<Long> selectLikeProductIds(@Param("userId") Long userId);

    /**
     * 统计商品的收藏数
     */
    @Select("SELECT COUNT(*) FROM t_user_behavior WHERE product_id = #{productId} AND behavior_type = 'COLLECT'")
    int countFavoritesByProductId(@Param("productId") Long productId);

    /**
     * 统计商品的点赞数
     */
    @Select("SELECT COUNT(*) FROM t_user_behavior WHERE product_id = #{productId} AND behavior_type = 'LIKE'")
    int countLikesByProductId(@Param("productId") Long productId);

    /**
     * 检查用户是否收藏了商品
     */
    @Select("SELECT COUNT(*) FROM t_user_behavior WHERE user_id = #{userId} AND product_id = #{productId} AND behavior_type = 'COLLECT'")
    int checkFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 检查用户是否点赞了商品
     */
    @Select("SELECT COUNT(*) FROM t_user_behavior WHERE user_id = #{userId} AND product_id = #{productId} AND behavior_type = 'LIKE'")
    int checkLike(@Param("userId") Long userId, @Param("productId") Long productId);
}