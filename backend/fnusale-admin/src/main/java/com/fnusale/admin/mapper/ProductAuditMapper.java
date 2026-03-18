package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ProductAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 商品审核记录Mapper
 */
@Mapper
public interface ProductAuditMapper extends BaseMapper<ProductAudit> {

    /**
     * 统计今日审核通过数
     */
    @Select("SELECT COUNT(*) FROM t_product_audit WHERE audit_result = 'PASS' AND DATE(audit_time) = #{date}")
    int countTodayPass(@Param("date") LocalDate date);

    /**
     * 统计今日审核驳回数
     */
    @Select("SELECT COUNT(*) FROM t_product_audit WHERE audit_result = 'REJECT' AND DATE(audit_time) = #{date}")
    int countTodayReject(@Param("date") LocalDate date);
}