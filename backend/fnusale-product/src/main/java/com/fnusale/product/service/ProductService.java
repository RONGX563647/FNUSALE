package com.fnusale.product.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.product.ProductPublishDTO;
import com.fnusale.common.dto.product.ProductQueryDTO;
import com.fnusale.common.vo.product.ProductVO;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 发布商品
     *
     * @param dto 商品发布DTO
     * @return 商品ID
     */
    Long publish(ProductPublishDTO dto);

    /**
     * 更新商品
     *
     * @param id  商品ID
     * @param dto 商品发布DTO
     */
    void update(Long id, ProductPublishDTO dto);

    /**
     * 删除商品（逻辑删除）
     *
     * @param id 商品ID
     */
    void delete(Long id);

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品VO
     */
    ProductVO getById(Long id);

    /**
     * 分页查询商品
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<ProductVO> getPage(ProductQueryDTO dto);

    /**
     * 搜索商品
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductVO> search(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 上架商品
     *
     * @param id 商品ID
     */
    void onShelf(Long id);

    /**
     * 下架商品
     *
     * @param id 商品ID
     */
    void offShelf(Long id);

    /**
     * 保存草稿
     *
     * @param dto 商品发布DTO
     * @return 商品ID
     */
    Long saveDraft(ProductPublishDTO dto);

    /**
     * 获取草稿列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductVO> getDraftList(Integer pageNum, Integer pageSize);

    /**
     * 获取用户发布的商品列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductVO> getMyProducts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * AI识别品类
     *
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    Object recognizeCategory(String imageUrl);

    /**
     * 获取推荐商品
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductVO> getRecommend(Integer pageNum, Integer pageSize);

    /**
     * 获取附近商品
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param distance  距离范围（米）
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 分页结果
     */
    PageResult<ProductVO> getNearby(String longitude, String latitude, Integer distance, Integer pageNum, Integer pageSize);

    /**
     * 批量获取商品信息
     *
     * @param productIds 商品ID列表
     * @return 商品ID -> ProductVO映射
     */
    Map<Long, ProductVO> getProductsByIds(List<Long> productIds);
}