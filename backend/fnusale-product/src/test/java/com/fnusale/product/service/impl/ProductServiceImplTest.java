package com.fnusale.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductCategory testCategory;
    private ProductImage testImage;

    @BeforeEach
    void setUp() {
        // 初始化测试品类
        testCategory = new ProductCategory();
        testCategory.setId(1L);
        testCategory.setCategoryName("数码产品");
        testCategory.setEnableStatus(1);

        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("测试商品");
        testProduct.setUserId(1L);
        testProduct.setCategoryId(1L);
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setOriginalPrice(new BigDecimal("199.99"));
        testProduct.setProductStatus(ProductStatus.ON_SHELF.getCode());
        testProduct.setNewDegree("NEW");
        testProduct.setIsDeleted(0);
        testProduct.setCreateTime(LocalDateTime.now());

        // 初始化测试图片
        testImage = new ProductImage();
        testImage.setId(1L);
        testImage.setProductId(1L);
        testImage.setImageUrl("http://example.com/image.jpg");
        testImage.setIsMainImage(1);
        testImage.setSort(1);
        testImage.setCreateTime(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("发布商品测试")
    class PublishTests {

        @Test
        @DisplayName("正常发布_成功")
        void publish_success() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();

            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(1L);
                return 1;
            });
            when(productImageMapper.insert(any(ProductImage.class))).thenReturn(1);
            when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

            Long result = productService.publish(dto);

            assertNotNull(result);
            assertEquals(1L, result);
            verify(productMapper).insert(any(Product.class));
            verify(productImageMapper, times(2)).insert(any(ProductImage.class));
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void publish_notLogin_throwsException() {
            ProductPublishDTO dto = createValidPublishDTO();

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("商品名称为空_抛出异常")
        void publish_emptyName_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setProductName("");

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("商品名称过短_抛出异常")
        void publish_nameTooShort_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setProductName("a");

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("商品名称过长_抛出异常")
        void publish_nameTooLong_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setProductName("a".repeat(ProductConstants.PRODUCT_NAME_MAX_LENGTH + 1));

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("品类不存在_抛出异常")
        void publish_categoryNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            when(productCategoryMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("品类已禁用_抛出异常")
        void publish_categoryDisabled_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            testCategory.setEnableStatus(0);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("价格为空_抛出异常")
        void publish_nullPrice_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setPrice(null);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("价格过低_抛出异常")
        void publish_priceTooLow_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setPrice(new BigDecimal("0.001"));
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("价格过高_抛出异常")
        void publish_priceTooHigh_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setPrice(new BigDecimal("9999999.99"));
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("售价高于原价_抛出异常")
        void publish_priceHigherThanOriginal_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setPrice(new BigDecimal("299.99"));
            dto.setOriginalPrice(new BigDecimal("199.99"));
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("图片为空_抛出异常")
        void publish_emptyImages_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setImageUrls(null);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("图片过多_抛出异常")
        void publish_tooManyImages_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            List<String> images = new ArrayList<>();
            for (int i = 0; i <= ProductConstants.MAX_IMAGE_COUNT; i++) {
                images.add("http://example.com/image" + i + ".jpg");
            }
            dto.setImageUrls(images);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("新旧程度为空_抛出异常")
        void publish_emptyNewDegree_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setNewDegree(null);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }

        @Test
        @DisplayName("新旧程度无效_抛出异常")
        void publish_invalidNewDegree_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setNewDegree("INVALID");
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productService.publish(dto));
        }
    }

    @Nested
    @DisplayName("更新商品测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新_成功")
        void update_success() {
            UserContext.setCurrentUserId(1L);

            // 设置商品为下架状态才可编辑
            testProduct.setProductStatus(ProductStatus.OFF_SHELF.getCode());

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setProductName("更新后的商品");

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            doNothing().when(productImageMapper).deleteByProductId(1L);
            when(productImageMapper.insert(any(ProductImage.class))).thenReturn(1);
            when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

            assertDoesNotThrow(() -> productService.update(1L, dto));
            verify(productMapper).updateById(any(Product.class));
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void update_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.update(1L, dto));
        }

        @Test
        @DisplayName("无权操作_抛出异常")
        void update_notOwner_throwsException() {
            UserContext.setCurrentUserId(2L);

            ProductPublishDTO dto = createValidPublishDTO();
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.update(1L, dto));
        }

        @Test
        @DisplayName("商品已上架_抛出异常")
        void update_productOnShelf_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            testProduct.setProductStatus(ProductStatus.ON_SHELF.getCode());
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.update(1L, dto));
        }
    }

    @Nested
    @DisplayName("删除商品测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除_成功")
        void delete_success() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> productService.delete(1L));
            verify(productMapper).deleteById(1L);
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void delete_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.delete(1L));
        }

        @Test
        @DisplayName("无权操作_抛出异常")
        void delete_notOwner_throwsException() {
            UserContext.setCurrentUserId(2L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.delete(1L));
        }
    }

    @Nested
    @DisplayName("获取商品详情测试")
    class GetByIdTests {

        @Test
        @DisplayName("从缓存获取_成功")
        void getById_fromCache_success() {
            ProductVO cachedVO = new ProductVO();
            cachedVO.setId(1L);
            cachedVO.setProductName("测试商品");

            when(valueOperations.get(anyString())).thenReturn("{\"id\":1,\"productName\":\"测试商品\"}");

            ProductVO result = productService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(productMapper, never()).selectProductVOById(anyLong());
        }

        @Test
        @DisplayName("从数据库获取_成功")
        void getById_fromDatabase_success() {
            ProductVO productVO = new ProductVO();
            productVO.setId(1L);
            productVO.setProductName("测试商品");
            productVO.setNewDegree("NEW");

            when(valueOperations.get(anyString())).thenReturn(null);
            when(productMapper.selectProductVOById(1L)).thenReturn(productVO);
            when(productImageMapper.selectByProductId(1L)).thenReturn(List.of(testImage));

            ProductVO result = productService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertNotNull(result.getImageUrls());
            assertEquals(1, result.getImageUrls().size());
            assertEquals("http://example.com/image.jpg", result.getMainImageUrl());
            verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void getById_notFound_throwsException() {
            when(valueOperations.get(anyString())).thenReturn(null);
            when(productMapper.selectProductVOById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.getById(1L));
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class GetPageTests {

        @Test
        @DisplayName("正常查询_成功")
        void getPage_success() {
            ProductQueryDTO dto = new ProductQueryDTO();
            dto.setPageNum(1);
            dto.setPageSize(10);

            ProductVO productVO = createProductVO();
            Page<ProductVO> page = new Page<>(1, 10);
            page.setRecords(List.of(productVO));
            page.setTotal(1);

            when(productMapper.selectProductPage(any(Page.class), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        Page<ProductVO> p = invocation.getArgument(0);
                        p.setRecords(List.of(productVO));
                        p.setTotal(1);
                        return p;
                    });
            when(productImageMapper.selectByProductIds(anyList())).thenReturn(List.of(testImage));

            var result = productService.getPage(dto);

            assertNotNull(result);
            assertEquals(1L, result.getTotal());
        }

        @Test
        @DisplayName("带排序查询_成功")
        void getPage_withSort_success() {
            ProductQueryDTO dto = new ProductQueryDTO();
            dto.setPageNum(1);
            dto.setPageSize(10);
            dto.setSortBy("price");
            dto.setSortOrder("asc");

            ProductVO productVO = createProductVO();
            Page<ProductVO> page = new Page<>(1, 10);

            lenient().when(productMapper.selectProductPage(any(Page.class), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        Page<ProductVO> p = invocation.getArgument(0);
                        p.setRecords(List.of(productVO));
                        p.setTotal(1);
                        return p;
                    });
            lenient().when(productImageMapper.selectByProductIds(anyList())).thenReturn(List.of(testImage));

            var result = productService.getPage(dto);

            assertNotNull(result);
        }

        @Test
        @DisplayName("无效排序字段_使用默认排序")
        void getPage_invalidSortBy_useDefault() {
            ProductQueryDTO dto = new ProductQueryDTO();
            dto.setPageNum(1);
            dto.setPageSize(10);
            dto.setSortBy("invalid_field"); // 非法字段，会被过滤为null

            ProductVO productVO = createProductVO();
            Page<ProductVO> page = new Page<>(1, 10);

            lenient().when(productMapper.selectProductPage(any(Page.class), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        Page<ProductVO> p = invocation.getArgument(0);
                        p.setRecords(List.of(productVO));
                        p.setTotal(1);
                        return p;
                    });
            lenient().when(productImageMapper.selectByProductIds(anyList())).thenReturn(List.of(testImage));

            var result = productService.getPage(dto);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("上架测试")
    class OnShelfTests {

        @Test
        @DisplayName("正常上架_成功")
        void onShelf_success() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.OFF_SHELF.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.onShelf(1L));
            verify(productMapper).updateById(any(Product.class));
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void onShelf_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.onShelf(1L));
        }

        @Test
        @DisplayName("已上架_抛出异常")
        void onShelf_alreadyOnShelf_throwsException() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.ON_SHELF.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.onShelf(1L));
        }

        @Test
        @DisplayName("已售出_抛出异常")
        void onShelf_soldOut_throwsException() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.SOLD_OUT.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.onShelf(1L));
        }

        @Test
        @DisplayName("违规商品_抛出异常")
        void onShelf_illegal_throwsException() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.ILLEGAL.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.onShelf(1L));
        }
    }

    @Nested
    @DisplayName("下架测试")
    class OffShelfTests {

        @Test
        @DisplayName("正常下架_成功")
        void offShelf_success() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.ON_SHELF.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.offShelf(1L));
            verify(productMapper).updateById(any(Product.class));
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void offShelf_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productService.offShelf(1L));
        }

        @Test
        @DisplayName("非上架状态_抛出异常")
        void offShelf_notOnShelf_throwsException() {
            UserContext.setCurrentUserId(1L);
            testProduct.setProductStatus(ProductStatus.OFF_SHELF.getCode());

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            assertThrows(BusinessException.class, () -> productService.offShelf(1L));
        }
    }

    @Nested
    @DisplayName("保存草稿测试")
    class SaveDraftTests {

        @Test
        @DisplayName("正常保存_成功")
        void saveDraft_success() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();

            when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(1L);
                return 1;
            });
            when(productImageMapper.insert(any(ProductImage.class))).thenReturn(1);

            Long result = productService.saveDraft(dto);

            assertNotNull(result);
            assertEquals(1L, result);
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void saveDraft_notLogin_throwsException() {
            ProductPublishDTO dto = createValidPublishDTO();

            assertThrows(BusinessException.class, () -> productService.saveDraft(dto));
        }

        @Test
        @DisplayName("商品名称为空_抛出异常")
        void saveDraft_emptyName_throwsException() {
            UserContext.setCurrentUserId(1L);

            ProductPublishDTO dto = createValidPublishDTO();
            dto.setProductName("");

            assertThrows(BusinessException.class, () -> productService.saveDraft(dto));
        }
    }

    @Nested
    @DisplayName("搜索商品测试")
    class SearchTests {

        @Test
        @DisplayName("关键词搜索_成功")
        void search_success() {
            ProductVO productVO = createProductVO();

            when(productMapper.selectProductPage(any(Page.class), eq("测试"), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        Page<ProductVO> p = invocation.getArgument(0);
                        p.setRecords(List.of(productVO));
                        p.setTotal(1);
                        return p;
                    });
            when(productImageMapper.selectByProductIds(anyList())).thenReturn(List.of(testImage));

            var result = productService.search("测试", 1, 10);

            assertNotNull(result);
            assertEquals(1L, result.getTotal());
        }
    }

    // ==================== 辅助方法 ====================

    private ProductPublishDTO createValidPublishDTO() {
        ProductPublishDTO dto = new ProductPublishDTO();
        dto.setProductName("测试商品名称");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("99.99"));
        dto.setOriginalPrice(new BigDecimal("199.99"));
        dto.setNewDegree("NEW");
        dto.setImageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"));
        return dto;
    }

    private ProductVO createProductVO() {
        ProductVO vo = new ProductVO();
        vo.setId(1L);
        vo.setProductName("测试商品");
        vo.setUserId(1L);
        vo.setNewDegree("NEW");
        return vo;
    }
}