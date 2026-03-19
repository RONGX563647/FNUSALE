package com.fnusale.product.service;

import com.fnusale.common.dto.product.ProductCategoryDTO;
import com.fnusale.common.vo.product.ProductCategoryVO;

import java.util.List;

/**
 * 商品品类服务接口
 */
public interface ProductCategoryService {

    /**
     * 获取品类树
     *
     * @return 品类树列表
     */
    List<ProductCategoryVO> getTree();

    /**
     * 获取一级品类列表
     *
     * @return 一级品类列表
     */
    List<ProductCategoryVO> getList();

    /**
     * 获取子品类
     *
     * @param parentId 父品类ID
     * @return 子品类列表
     */
    List<ProductCategoryVO> getChildren(Long parentId);

    /**
     * 获取品类详情
     *
     * @param id 品类ID
     * @return 品类VO
     */
    ProductCategoryVO getById(Long id);

    /**
     * 新增品类
     *
     * @param dto 品类DTO
     */
    void add(ProductCategoryDTO dto);

    /**
     * 更新品类
     *
     * @param id  品类ID
     * @param dto 品类DTO
     */
    void update(Long id, ProductCategoryDTO dto);

    /**
     * 删除品类
     *
     * @param id 品类ID
     */
    void delete(Long id);

    /**
     * 更新品类状态
     *
     * @param id     品类ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取热门品类
     *
     * @return 热门品类列表
     */
    List<ProductCategoryVO> getHotCategories();
}