package com.fnusale.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券 Mapper
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 查询可领取的优惠券列表
     */
    @Select("SELECT * FROM t_coupon WHERE enable_status = 1 " +
            "AND start_time <= #{now} AND end_time >= #{now} " +
            "AND received_count < total_count " +
            "ORDER BY create_time DESC")
    List<Coupon> selectAvailableCoupons(@Param("now") LocalDateTime now);

    /**
     * 分页查询优惠券
     */
    @Select("<script>" +
            "SELECT * FROM t_coupon WHERE 1=1 " +
            "<if test='name != null and name != \"\"'> AND coupon_name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='type != null and type != \"\"'> AND coupon_type = #{type}</if>" +
            "<if test='status != null'> AND enable_status = #{status}</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<Coupon> selectCouponPage(Page<Coupon> page,
                                    @Param("name") String name,
                                    @Param("type") String type,
                                    @Param("status") Integer status);

    /**
     * 增加已领取数量
     */
    @Update("UPDATE t_coupon SET received_count = received_count + 1 WHERE id = #{couponId} AND received_count < total_count")
    int incrementReceivedCount(@Param("couponId") Long couponId);

    /**
     * 增加已使用数量
     */
    @Update("UPDATE t_coupon SET used_count = used_count + 1 WHERE id = #{couponId}")
    int incrementUsedCount(@Param("couponId") Long couponId);
}