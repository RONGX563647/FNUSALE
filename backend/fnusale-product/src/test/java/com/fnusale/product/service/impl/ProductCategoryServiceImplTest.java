package com.fnusale.product.service.impl;

import com.fnusale.common.dto.product.ProductCategoryDTO;
import com.fnusale.common.entity.ProductCategory;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.product.mapper.ProductCategoryMapper;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品品类服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ProductCategoryServiceImpl productCategoryService;

    private ProductCategory testCategory;
    private ProductCategory childCategory;

    @BeforeEach
    void setUp() {
        // 初始化测试一级品类
        testCategory = new ProductCategory();
        testCategory.setId(1L);
        testCategory.setCategoryName("数码产品");
        testCategory.setParentCategoryId(0L);
        testCategory.setEnableStatus(1);
        testCategory.setAiMappingValue("digital");

        // 初始化测试子品类
        childCategory = new ProductCategory();
        childCategory.setId(2L);
        childCategory.setCategoryName("手机");
        childCategory.setParentCategoryId(1L);
        childCategory.setEnableStatus(1);
    }

    @AfterEach
    void tearDown() {
        // 清理
    }

    @Nested
    @DisplayName("获取品类树测试")
    class GetTreeTests {

        @Test
        @DisplayName("正常获取_成功")
        void getTree_success() {
            when(productCategoryMapper.selectAllEnabled()).thenReturn(List.of(testCategory, childCategory));

            var result = productCategoryService.getTree();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("数码产品", result.get(0).getCategoryName());
            assertNotNull(result.get(0).getChildren());
            assertEquals(1, result.get(0).getChildren().size());
        }

        @Test
        @DisplayName("空品类列表_返回空列表")
        void getTree_emptyList_returnsEmpty() {
            when(productCategoryMapper.selectAllEnabled()).thenReturn(List.of());

            var result = productCategoryService.getTree();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取一级品类列表测试")
    class GetListTests {

        @Test
        @DisplayName("正常获取_成功")
        void getList_success() {
            when(productCategoryMapper.selectTopCategories()).thenReturn(List.of(testCategory));

            var result = productCategoryService.getList();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("数码产品", result.get(0).getCategoryName());
        }

        @Test
        @DisplayName("无一级品类_返回空列表")
        void getList_empty_returnsEmpty() {
            when(productCategoryMapper.selectTopCategories()).thenReturn(List.of());

            var result = productCategoryService.getList();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取子品类测试")
    class GetChildrenTests {

        @Test
        @DisplayName("正常获取_成功")
        void getChildren_success() {
            when(productCategoryMapper.selectByParentId(1L)).thenReturn(List.of(childCategory));

            var result = productCategoryService.getChildren(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("手机", result.get(0).getCategoryName());
        }

        @Test
        @DisplayName("父ID为空_抛出异常")
        void getChildren_nullParentId_throwsException() {
            assertThrows(BusinessException.class, () -> productCategoryService.getChildren(null));
        }

        @Test
        @DisplayName("无子品类_返回空列表")
        void getChildren_noChildren_returnsEmpty() {
            when(productCategoryMapper.selectByParentId(1L)).thenReturn(List.of());

            var result = productCategoryService.getChildren(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取品类详情测试")
    class GetByIdTests {

        @Test
        @DisplayName("正常获取_成功")
        void getById_success() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            var result = productCategoryService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("数码产品", result.getCategoryName());
        }

        @Test
        @DisplayName("ID为空_抛出异常")
        void getById_nullId_throwsException() {
            assertThrows(BusinessException.class, () -> productCategoryService.getById(null));
        }

        @Test
        @DisplayName("品类不存在_抛出异常")
        void getById_notFound_throwsException() {
            when(productCategoryMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.getById(999L));
        }
    }

    @Nested
    @DisplayName("新增品类测试")
    class AddTests {

        @Test
        @DisplayName("正常新增一级品类_成功")
        void add_topCategory_success() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("图书");
            dto.setParentCategoryId(0L);
            dto.setEnableStatus(1);

            when(productCategoryMapper.countByName("图书")).thenReturn(0);
            when(productCategoryMapper.insert(any(ProductCategory.class))).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.add(dto));
            verify(productCategoryMapper).insert(any(ProductCategory.class));
        }

        @Test
        @DisplayName("正常新增子品类_成功")
        void add_childCategory_success() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("笔记本电脑");
            dto.setParentCategoryId(1L);
            dto.setEnableStatus(1);

            when(productCategoryMapper.countByName("笔记本电脑")).thenReturn(0);
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.insert(any(ProductCategory.class))).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.add(dto));
            verify(productCategoryMapper).insert(any(ProductCategory.class));
        }

        @Test
        @DisplayName("品类名称为空_抛出异常")
        void add_emptyName_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("");

            assertThrows(BusinessException.class, () -> productCategoryService.add(dto));
        }

        @Test
        @DisplayName("品类名称已存在_抛出异常")
        void add_nameExists_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("数码产品");
            dto.setParentCategoryId(0L);

            when(productCategoryMapper.countByName("数码产品")).thenReturn(1);

            assertThrows(BusinessException.class, () -> productCategoryService.add(dto));
        }

        @Test
        @DisplayName("父品类不存在_抛出异常")
        void add_parentNotFound_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("新分类");
            dto.setParentCategoryId(999L);

            when(productCategoryMapper.countByName("新分类")).thenReturn(0);
            when(productCategoryMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.add(dto));
        }
    }

    @Nested
    @DisplayName("更新品类测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新_成功")
        void update_success() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("更新后的名称");

            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.countByName("更新后的名称")).thenReturn(0);
            when(productCategoryMapper.updateById(any(ProductCategory.class))).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.update(1L, dto));
            verify(productCategoryMapper).updateById(any(ProductCategory.class));
        }

        @Test
        @DisplayName("品类不存在_抛出异常")
        void update_notFound_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("更新后的名称");

            when(productCategoryMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.update(1L, dto));
        }

        @Test
        @DisplayName("名称已存在_抛出异常")
        void update_nameExists_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setCategoryName("已存在的名称");

            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.countByName("已存在的名称")).thenReturn(1);

            assertThrows(BusinessException.class, () -> productCategoryService.update(1L, dto));
        }

        @Test
        @DisplayName("设置自己为父品类_抛出异常")
        void update_selfParent_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setParentCategoryId(1L);

            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productCategoryService.update(1L, dto));
        }

        @Test
        @DisplayName("父品类不存在_抛出异常")
        void update_parentNotFound_throwsException() {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            dto.setParentCategoryId(999L);

            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.update(1L, dto));
        }
    }

    @Nested
    @DisplayName("删除品类测试")
    class DeleteTests {

        @Test
        @DisplayName("品类不存在_抛出异常")
        void delete_notFound_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.delete(1L));
        }

        @Test
        @DisplayName("有子品类_抛出异常")
        void delete_hasChildren_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.countChildrenById(1L)).thenReturn(2);

            assertThrows(BusinessException.class, () -> productCategoryService.delete(1L));
        }

        @Test
        @DisplayName("有商品_抛出异常")
        void delete_hasProducts_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.countChildrenById(1L)).thenReturn(0);
            when(productCategoryMapper.countProductsByCategoryId(1L)).thenReturn(5);

            assertThrows(BusinessException.class, () -> productCategoryService.delete(1L));
        }

        @Test
        @DisplayName("正常删除_成功")
        void delete_success() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.countChildrenById(1L)).thenReturn(0);
            when(productCategoryMapper.countProductsByCategoryId(1L)).thenReturn(0);
            when(productCategoryMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.delete(1L));
            verify(productCategoryMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("更新状态测试")
    class UpdateStatusTests {

        @Test
        @DisplayName("启用品类_成功")
        void updateStatus_enable_success() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.updateById(any(ProductCategory.class))).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.updateStatus(1L, 1));
            verify(productCategoryMapper).updateById(any(ProductCategory.class));
        }

        @Test
        @DisplayName("禁用品类_成功")
        void updateStatus_disable_success() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productCategoryMapper.updateById(any(ProductCategory.class))).thenReturn(1);

            assertDoesNotThrow(() -> productCategoryService.updateStatus(1L, 0));
            verify(productCategoryMapper).updateById(any(ProductCategory.class));
        }

        @Test
        @DisplayName("品类不存在_抛出异常")
        void updateStatus_notFound_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> productCategoryService.updateStatus(1L, 1));
        }

        @Test
        @DisplayName("状态为空_抛出异常")
        void updateStatus_nullStatus_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productCategoryService.updateStatus(1L, null));
        }

        @Test
        @DisplayName("状态无效_抛出异常")
        void updateStatus_invalidStatus_throwsException() {
            when(productCategoryMapper.selectById(1L)).thenReturn(testCategory);

            assertThrows(BusinessException.class, () -> productCategoryService.updateStatus(1L, 2));
        }
    }

    @Nested
    @DisplayName("获取热门品类测试")
    class GetHotCategoriesTests {

        @Test
        @DisplayName("正常获取_成功")
        void getHotCategories_success() {
            when(productCategoryMapper.selectTopCategories()).thenReturn(List.of(testCategory));

            var result = productCategoryService.getHotCategories();

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }
}