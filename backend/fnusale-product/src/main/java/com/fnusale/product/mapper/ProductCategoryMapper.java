package com.fnusale.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品品类Mapper
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 查询所有启用的品类
     */
    @Select("SELECT * FROM t_product_category WHERE enable_status = 1 ORDER BY id")
    List<ProductCategory> selectAllEnabled();

    /**
     * 根据父ID查询子品类
     */
    @Select("SELECT * FROM t_product_category WHERE parent_category_id = #{parentId} AND enable_status = 1 ORDER BY id")
    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询一级品类（无父品类）
     */
    @Select("SELECT * FROM t_product_category WHERE (parent_category_id IS NULL OR parent_category_id = 0) AND enable_status = 1 ORDER BY id")
    List<ProductCategory> selectTopCategories();

    /**
     * 根据AI映射值查询品类
     */
    @Select("SELECT * FROM t_product_category WHERE ai_mapping_value = #{aiMappingValue} AND enable_status = 1 LIMIT 1")
    ProductCategory selectByAiMappingValue(@Param("aiMappingValue") String aiMappingValue);

    /**
     * 检查品类名称是否存在
     */
    @Select("SELECT COUNT(*) FROM t_product_category WHERE category_name = #{categoryName} AND enable_status = 1")
    int countByName(@Param("categoryName") String categoryName);

    /**
     * 检查品类下是否有子品类
     */
    @Select("SELECT COUNT(*) FROM t_product_category WHERE parent_category_id = #{id} AND enable_status = 1")
    int countChildrenById(@Param("id") Long id);

    /**
     * 检查品类下是否有商品
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE category_id = #{categoryId} AND is_deleted = 0")
    int countProductsByCategoryId(@Param("categoryId") Long categoryId);
}