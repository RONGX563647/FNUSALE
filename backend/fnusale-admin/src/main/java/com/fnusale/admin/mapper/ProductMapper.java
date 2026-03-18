package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.Product;
import com.fnusale.common.vo.admin.PendingProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 商品Mapper（Admin模块）
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询待审核商品
     */
    Page<PendingProductVO> selectPendingProducts(Page<PendingProductVO> page);

    /**
     * 统计待审核商品数
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE product_status = 'DRAFT' AND is_deleted = 0")
    int countPendingAudit();

    /**
     * 统计今日商品发布数
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE DATE(create_time) = #{date} AND is_deleted = 0")
    int countTodayPublish(@Param("date") LocalDate date);
}