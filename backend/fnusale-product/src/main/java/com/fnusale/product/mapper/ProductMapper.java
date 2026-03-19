package com.fnusale.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.Product;
import com.fnusale.common.vo.product.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据用户ID查询商品列表
     */
    @Select("SELECT p.*, pc.category_name, cpp.pick_point_name, u.username as publisher_name, u.credit_score as publisher_credit_score " +
            "FROM t_product p " +
            "LEFT JOIN t_product_category pc ON p.category_id = pc.id " +
            "LEFT JOIN t_campus_pick_point cpp ON p.pick_point_id = cpp.id " +
            "LEFT JOIN t_user u ON p.user_id = u.id " +
            "WHERE p.user_id = #{userId} AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC")
    List<ProductVO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询商品列表
     */
    @Select("SELECT p.*, pc.category_name, cpp.pick_point_name, u.username as publisher_name, u.credit_score as publisher_credit_score " +
            "FROM t_product p " +
            "LEFT JOIN t_product_category pc ON p.category_id = pc.id " +
            "LEFT JOIN t_campus_pick_point cpp ON p.pick_point_id = cpp.id " +
            "LEFT JOIN t_user u ON p.user_id = u.id " +
            "WHERE p.product_status = #{status} AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC")
    List<ProductVO> selectByStatus(@Param("status") String status);

    /**
     * 分页查询商品详情（带关联信息）
     */
    IPage<ProductVO> selectProductPage(Page<ProductVO> page, @Param("keyword") String keyword,
                                        @Param("categoryId") Long categoryId, @Param("minPrice") java.math.BigDecimal minPrice,
                                        @Param("maxPrice") java.math.BigDecimal maxPrice, @Param("newDegree") String newDegree,
                                        @Param("isSeckill") Integer isSeckill, @Param("productStatus") String productStatus,
                                        @Param("sortBy") String sortBy, @Param("sortOrder") String sortOrder,
                                        @Param("userId") Long userId);

    /**
     * 根据ID查询商品详情（带关联信息）
     */
    @Select("SELECT p.*, pc.category_name, cpp.pick_point_name, u.username as publisher_name, u.credit_score as publisher_credit_score " +
            "FROM t_product p " +
            "LEFT JOIN t_product_category pc ON p.category_id = pc.id " +
            "LEFT JOIN t_campus_pick_point cpp ON p.pick_point_id = cpp.id " +
            "LEFT JOIN t_user u ON p.user_id = u.id " +
            "WHERE p.id = #{id} AND p.is_deleted = 0")
    ProductVO selectProductVOById(@Param("id") Long id);

    /**
     * 统计用户发布的商品数量
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE user_id = #{userId} AND is_deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计指定状态的商品数量
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE product_status = #{status} AND is_deleted = 0")
    int countByStatus(@Param("status") String status);

    /**
     * 批量查询商品详情（带关联信息）
     */
    List<ProductVO> selectProductVOByIds(@Param("ids") List<Long> ids);
}