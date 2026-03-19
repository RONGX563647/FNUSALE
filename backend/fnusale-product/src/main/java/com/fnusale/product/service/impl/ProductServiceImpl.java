package com.fnusale.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.constant.ProductConstants;
import com.fnusale.common.dto.product.ProductPublishDTO;
import com.fnusale.common.dto.product.ProductQueryDTO;
import com.fnusale.common.entity.Product;
import com.fnusale.common.entity.ProductCategory;
import com.fnusale.common.entity.ProductImage;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.product.mapper.ProductCategoryMapper;
import com.fnusale.product.mapper.ProductImageMapper;
import com.fnusale.product.mapper.ProductMapper;
import com.fnusale.product.mq.producer.MessageProducer;
import com.fnusale.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductImageMapper productImageMapper;
    private final StringRedisTemplate redisTemplate;
    private final MessageProducer messageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ProductPublishDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        // 校验商品名称
        validateProductName(dto.getProductName());

        // 校验品类
        ProductCategory category = productCategoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getEnableStatus() != 1) {
            throw new BusinessException("品类不存在或已禁用");
        }

        // 校验价格
        validatePrice(dto.getPrice(), dto.getOriginalPrice());

        // 校验图片数量
        validateImages(dto.getImageUrls());

        // 校验新旧程度
        validateNewDegree(dto.getNewDegree());

        // 创建商品
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setUserId(userId);
        product.setProductStatus(ProductStatus.ON_SHELF.getCode());
        product.setIsSeckill(dto.getIsSeckill() != null ? dto.getIsSeckill() : 0);

        // 处理经纬度
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            product.setLongitude(new BigDecimal(dto.getLongitude()));
            product.setLatitude(new BigDecimal(dto.getLatitude()));
        }

        productMapper.insert(product);

        // 保存图片
        saveImages(product.getId(), dto.getImageUrls());

        // 清除商品列表缓存
        clearProductListCache();

        log.info("商品发布成功，productId: {}, userId: {}", product.getId(), userId);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductPublishDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }

        // 只有下架状态的商品可以编辑
        if (!ProductStatus.OFF_SHELF.getCode().equals(product.getProductStatus())
                && !ProductStatus.DRAFT.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品当前状态不允许编辑");
        }

        // 校验商品名称
        validateProductName(dto.getProductName());

        // 校验品类
        ProductCategory category = productCategoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getEnableStatus() != 1) {
            throw new BusinessException("品类不存在或已禁用");
        }

        // 校验价格
        validatePrice(dto.getPrice(), dto.getOriginalPrice());

        // 校验图片数量
        validateImages(dto.getImageUrls());

        // 校验新旧程度
        validateNewDegree(dto.getNewDegree());

        // 更新商品
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            product.setLongitude(new BigDecimal(dto.getLongitude()));
            product.setLatitude(new BigDecimal(dto.getLatitude()));
        }

        productMapper.updateById(product);

        // 删除旧图片
        productImageMapper.deleteByProductId(id);

        // 保存新图片
        saveImages(id, dto.getImageUrls());

        // 清除缓存
        clearProductDetailCache(id);
        clearProductListCache();

        log.info("商品更新成功，productId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = UserContext.getUserIdOrThrow();

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }

        // 逻辑删除
        productMapper.deleteById(id);

        // 清除缓存
        clearProductDetailCache(id);
        clearProductListCache();

        log.info("商品删除成功，productId: {}", id);
    }

    @Override
    public ProductVO getById(Long id) {
        // 尝试从缓存获取
        String cacheKey = ProductConstants.PRODUCT_DETAIL_CACHE_PREFIX + id;
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, ProductVO.class);
        }

        ProductVO productVO = productMapper.selectProductVOById(id);
        if (productVO == null) {
            throw new BusinessException("商品不存在");
        }

        // 查询图片列表
        List<ProductImage> images = productImageMapper.selectByProductId(id);
        List<String> imageUrls = new ArrayList<>();
        String mainImageUrl = null;
        for (ProductImage image : images) {
            imageUrls.add(image.getImageUrl());
            if (image.getIsMainImage() == 1) {
                mainImageUrl = image.getImageUrl();
            }
        }
        productVO.setImageUrls(imageUrls);
        productVO.setMainImageUrl(mainImageUrl);

        // 设置新旧程度描述
        productVO.setNewDegreeDesc(getNewDegreeDesc(productVO.getNewDegree()));

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(productVO),
                ProductConstants.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return productVO;
    }

    @Override
    public PageResult<ProductVO> getPage(ProductQueryDTO dto) {
        Page<ProductVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 白名单校验防止SQL注入
        String sortBy = validateSortBy(dto.getSortBy());
        String sortOrder = validateSortOrder(dto.getSortOrder());

        productMapper.selectProductPage(page, dto.getKeyword(), dto.getCategoryId(),
                dto.getMinPrice(), dto.getMaxPrice(), dto.getNewDegree(),
                dto.getIsSeckill(), dto.getProductStatus(), sortBy, sortOrder, null);

        // 批量填充图片信息
        fillProductImages(page.getRecords());

        return PageResult.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), page.getRecords());
    }

    /**
     * 校验排序字段（白名单）
     */
    private String validateSortBy(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return null;
        }
        Set<String> allowedFields = Set.of("price", "time", "create_time");
        if (allowedFields.contains(sortBy.toLowerCase())) {
            return sortBy.toLowerCase();
        }
        return null;
    }

    /**
     * 校验排序方向（白名单）
     */
    private String validateSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isEmpty()) {
            return "desc";
        }
        if ("asc".equalsIgnoreCase(sortOrder)) {
            return "asc";
        }
        return "desc";
    }

    @Override
    public PageResult<ProductVO> search(String keyword, Integer pageNum, Integer pageSize) {
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setKeyword(keyword);
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setProductStatus(ProductStatus.ON_SHELF.getCode());

        return getPage(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onShelf(Long id) {
        Long userId = UserContext.getUserIdOrThrow();

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }

        if (ProductStatus.ON_SHELF.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品已上架");
        }

        if (ProductStatus.SOLD_OUT.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("已成交商品不能重新上架");
        }

        if (ProductStatus.ILLEGAL.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("违规商品不能上架");
        }

        product.setProductStatus(ProductStatus.ON_SHELF.getCode());
        productMapper.updateById(product);

        // 清除缓存
        clearProductDetailCache(id);
        clearProductListCache();

        log.info("商品上架成功，productId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(Long id) {
        Long userId = UserContext.getUserIdOrThrow();

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }

        if (!ProductStatus.ON_SHELF.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品当前状态不允许下架");
        }

        product.setProductStatus(ProductStatus.OFF_SHELF.getCode());
        productMapper.updateById(product);

        // 清除缓存
        clearProductDetailCache(id);
        clearProductListCache();

        log.info("商品下架成功，productId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(ProductPublishDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        // 校验商品名称
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new BusinessException("商品名称不能为空");
        }

        // 创建草稿
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setUserId(userId);
        product.setProductStatus(ProductStatus.DRAFT.getCode());
        product.setIsSeckill(dto.getIsSeckill() != null ? dto.getIsSeckill() : 0);

        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            product.setLongitude(new BigDecimal(dto.getLongitude()));
            product.setLatitude(new BigDecimal(dto.getLatitude()));
        }

        productMapper.insert(product);

        // 保存图片
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            saveImages(product.getId(), dto.getImageUrls());
        }

        log.info("草稿保存成功，productId: {}", product.getId());
        return product.getId();
    }

    @Override
    public PageResult<ProductVO> getDraftList(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();

        Page<ProductVO> page = new Page<>(pageNum, pageSize);
        // 直接在SQL中添加userId条件，避免内存过滤
        productMapper.selectProductPage(page, null, null, null, null, null, null,
                ProductStatus.DRAFT.getCode(), null, "desc", userId);

        fillProductImages(page.getRecords());

        return PageResult.of(pageNum, pageSize, page.getTotal(), page.getRecords());
    }

    @Override
    public PageResult<ProductVO> getMyProducts(Long userId, Integer pageNum, Integer pageSize) {
        Page<ProductVO> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        // 直接在SQL中添加userId条件，避免内存过滤
        productMapper.selectProductPage(page, null, null, null, null, null, null,
                null, "create_time", "desc", userId);

        fillProductImages(page.getRecords());

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public Object recognizeCategory(String imageUrl) {
        // TODO: 对接阿里云视觉AI
        log.info("AI识别品类，imageUrl: {}", imageUrl);
        return null;
    }

    @Override
    public PageResult<ProductVO> getRecommend(Integer pageNum, Integer pageSize) {
        // TODO: 对接推荐系统
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setProductStatus(ProductStatus.ON_SHELF.getCode());

        return getPage(dto);
    }

    @Override
    public PageResult<ProductVO> getNearby(String longitude, String latitude, Integer distance, Integer pageNum, Integer pageSize) {
        // TODO: 实现基于地理位置的查询
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setProductStatus(ProductStatus.ON_SHELF.getCode());

        return getPage(dto);
    }

    @Override
    public Map<Long, ProductVO> getProductsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductVO> products = productMapper.selectProductVOByIds(productIds);
        if (products.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量填充图片信息
        fillProductImages(products);

        return products.stream()
                .collect(Collectors.toMap(ProductVO::getId, p -> p, (v1, v2) -> v1));
    }

    // ==================== 私有方法 ====================

    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new BusinessException("商品名称不能为空");
        }
        if (productName.length() < ProductConstants.PRODUCT_NAME_MIN_LENGTH
                || productName.length() > ProductConstants.PRODUCT_NAME_MAX_LENGTH) {
            throw new BusinessException("商品名称长度应在" + ProductConstants.PRODUCT_NAME_MIN_LENGTH
                    + "-" + ProductConstants.PRODUCT_NAME_MAX_LENGTH + "个字符之间");
        }
    }

    private void validatePrice(BigDecimal price, BigDecimal originalPrice) {
        if (price == null) {
            throw new BusinessException("售价不能为空");
        }
        if (price.compareTo(ProductConstants.MIN_PRICE) < 0
                || price.compareTo(ProductConstants.MAX_PRICE) > 0) {
            throw new BusinessException("售价应在" + ProductConstants.MIN_PRICE + "-" + ProductConstants.MAX_PRICE + "之间");
        }
        if (originalPrice != null && price.compareTo(originalPrice) > 0) {
            throw new BusinessException("售价不能高于原价");
        }
    }

    private void validateImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new BusinessException("请至少上传一张商品图片");
        }
        if (imageUrls.size() > ProductConstants.MAX_IMAGE_COUNT) {
            throw new BusinessException("最多上传" + ProductConstants.MAX_IMAGE_COUNT + "张图片");
        }
    }

    private void validateNewDegree(String newDegree) {
        if (newDegree == null || newDegree.isEmpty()) {
            throw new BusinessException("新旧程度不能为空");
        }
        try {
            com.fnusale.common.enums.NewDegree.valueOf(newDegree);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("新旧程度参数不正确");
        }
    }

    private void saveImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setIsMainImage(i == 0 ? 1 : 0);
            image.setSort(i + 1);
            image.setCreateTime(LocalDateTime.now());
            productImageMapper.insert(image);
        }
    }

    private void fillProductImages(List<ProductVO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        // 批量查询所有商品的图片，避免N+1问题
        List<Long> productIds = products.stream()
                .map(ProductVO::getId)
                .collect(Collectors.toList());
        List<ProductImage> allImages = productImageMapper.selectByProductIds(productIds);

        // 按商品ID分组
        Map<Long, List<ProductImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId));

        // 填充图片信息
        for (ProductVO product : products) {
            List<ProductImage> images = imageMap.getOrDefault(product.getId(), Collections.emptyList());
            List<String> imageUrls = new ArrayList<>();
            for (ProductImage image : images) {
                imageUrls.add(image.getImageUrl());
                if (image.getIsMainImage() == 1) {
                    product.setMainImageUrl(image.getImageUrl());
                }
            }
            product.setImageUrls(imageUrls);
            product.setNewDegreeDesc(getNewDegreeDesc(product.getNewDegree()));
        }
    }

    private String getNewDegreeDesc(String newDegree) {
        if (newDegree == null) {
            return null;
        }
        for (com.fnusale.common.enums.NewDegree degree : com.fnusale.common.enums.NewDegree.values()) {
            if (degree.getCode().equals(newDegree)) {
                return degree.getDesc();
            }
        }
        return null;
    }

    private void clearProductDetailCache(Long productId) {
        String key = ProductConstants.PRODUCT_DETAIL_CACHE_PREFIX + productId;
        redisTemplate.delete(key);
    }

    private void clearProductListCache() {
        // 清除商品列表相关缓存
        redisTemplate.delete(redisTemplate.keys(ProductConstants.PRODUCT_LIST_CACHE_PREFIX + "*"));
    }
}