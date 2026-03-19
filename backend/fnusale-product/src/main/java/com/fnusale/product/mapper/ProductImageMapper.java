package com.fnusale.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品图片Mapper
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    /**
     * 根据商品ID查询图片列表
     */
    @Select("SELECT * FROM t_product_image WHERE product_id = #{productId} ORDER BY sort, id")
    List<ProductImage> selectByProductId(@Param("productId") Long productId);

    /**
     * 批量查询商品图片
     */
    @Select("<script>" +
            "SELECT * FROM t_product_image WHERE product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY product_id, sort, id" +
            "</script>")
    List<ProductImage> selectByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * 查询商品主图
     */
    @Select("SELECT * FROM t_product_image WHERE product_id = #{productId} AND is_main_image = 1 LIMIT 1")
    ProductImage selectMainImageByProductId(@Param("productId") Long productId);

    /**
     * 删除商品所有图片
     */
    @Select("DELETE FROM t_product_image WHERE product_id = #{productId}")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * 统计商品图片数量
     */
    @Select("SELECT COUNT(*) FROM t_product_image WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);
}