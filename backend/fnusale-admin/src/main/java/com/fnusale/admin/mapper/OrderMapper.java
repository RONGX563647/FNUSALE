package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单Mapper（Admin模块）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 统计今日成交订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE DATE(success_time) = #{date} AND order_status = 'SUCCESS' AND is_deleted = 0")
    int countTodaySuccess(@Param("date") LocalDate date);

    /**
     * 统计今日成交金额
     */
    @Select("SELECT COALESCE(SUM(actual_pay_amount), 0) FROM t_order WHERE DATE(success_time) = #{date} AND order_status = 'SUCCESS' AND is_deleted = 0")
    BigDecimal sumTodayAmount(@Param("date") LocalDate date);
}