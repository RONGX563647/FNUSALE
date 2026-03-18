package com.fnusale.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户优惠券 Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户的优惠券列表
     */
    @Select("<script>" +
            "SELECT uc.*, c.coupon_name, c.coupon_type, c.full_amount, c.reduce_amount, c.category_id " +
            "FROM t_user_coupon uc " +
            "LEFT JOIN t_coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} " +
            "<if test='status != null and status != \"\"'> AND uc.coupon_status = #{status}</if>" +
            " ORDER BY uc.receive_time DESC" +
            "</script>")
    List<UserCoupon> selectByUserId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 检查用户是否已领取某优惠券
     */
    @Select("SELECT COUNT(*) FROM t_user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 查询用户可用于指定商品的优惠券
     */
    @Select("SELECT uc.*, c.coupon_name, c.coupon_type, c.full_amount, c.reduce_amount, c.category_id " +
            "FROM t_user_coupon uc " +
            "LEFT JOIN t_coupon c ON uc.coupon_id = c.id " +
            "LEFT JOIN t_product p ON #{productId} = p.id " +
            "WHERE uc.user_id = #{userId} " +
            "AND uc.coupon_status = 'UNUSED' " +
            "AND uc.expire_time > NOW() " +
            "AND (c.coupon_type != 'CATEGORY' OR c.category_id = p.category_id) " +
            "AND (c.coupon_type != 'FULL_REDUCE' OR #{price} >= c.full_amount) " +
            "ORDER BY c.reduce_amount DESC")
    List<UserCoupon> selectUsableCoupons(@Param("userId") Long userId,
                                          @Param("productId") Long productId,
                                          @Param("price") java.math.BigDecimal price);

    /**
     * 更新过期优惠券状态
     */
    @Update("UPDATE t_user_coupon SET coupon_status = 'EXPIRED' WHERE coupon_status = 'UNUSED' AND expire_time < NOW()")
    int updateExpiredCoupons();

    /**
     * 分页查询用户优惠券（管理员用）
     */
    @Select("SELECT uc.*, c.coupon_name, c.coupon_type, c.full_amount, c.reduce_amount, c.category_id, u.username " +
            "FROM t_user_coupon uc " +
            "LEFT JOIN t_coupon c ON uc.coupon_id = c.id " +
            "LEFT JOIN t_user u ON uc.user_id = u.id " +
            "WHERE uc.coupon_id = #{couponId} " +
            "ORDER BY uc.receive_time DESC")
    IPage<UserCoupon> selectUserCouponPage(Page<UserCoupon> page, @Param("couponId") Long couponId);
}