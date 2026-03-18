package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.ProductAuditMapper;
import com.fnusale.admin.mapper.ProductMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.Product;
import com.fnusale.common.entity.ProductAudit;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.AuditResult;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.AuditRecordVO;
import com.fnusale.common.vo.admin.AuditStatisticsVO;
import com.fnusale.common.vo.admin.PendingProductVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminProductAuditService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminProductAuditServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductAuditMapper productAuditMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private AdminProductAuditServiceImpl adminProductAuditService;

    private Product createTestProduct(Long id, String status, Long userId) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("测试商品");
        product.setProductStatus(status);
        product.setUserId(userId);
        product.setPrice(new BigDecimal("99.00"));
        product.setProductDesc("测试描述");
        return product;
    }

    private User createTestUser(Long id, Integer creditScore) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setCreditScore(creditScore);
        return user;
    }

    @Nested
    @DisplayName("待审核商品列表测试")
    class GetPendingListTest {

        @Test
        @DisplayName("查询待审核商品列表成功")
        void getPendingList_success() {
            // Arrange
            Page<PendingProductVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            PendingProductVO vo = new PendingProductVO();
            vo.setProductId(1L);
            vo.setProductName("测试商品");
            mockPage.setRecords(List.of(vo));

            when(productMapper.selectPendingProducts(any(Page.class))).thenReturn(mockPage);

            // Act
            PageResult<PendingProductVO> result = adminProductAuditService.getPendingList(1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }
    }

    @Nested
    @DisplayName("审核通过测试")
    class AuditPassTest {

        @Test
        @DisplayName("审核通过成功")
        void auditPass_success() {
            // Arrange
            Product product = createTestProduct(1L, ProductStatus.DRAFT.getCode(), 100L);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(productAuditMapper.insert(any(ProductAudit.class))).thenReturn(1);

            // Act
            adminProductAuditService.auditPass(1L, 1L);

            // Assert
            verify(productMapper).updateById(argThat(p ->
                    p.getId().equals(1L) &&
                    p.getProductStatus().equals(ProductStatus.ON_SHELF.getCode())
            ));
            verify(productAuditMapper).insert(argThat(a ->
                    a.getProductId().equals(1L) &&
                    a.getAuditResult().equals(AuditResult.PASS.getCode())
            ));
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void auditPass_productNotFound_throwsException() {
            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> adminProductAuditService.auditPass(1L, 1L));
        }

        @Test
        @DisplayName("商品状态不允许审核抛出异常")
        void auditPass_invalidStatus_throwsException() {
            Product product = createTestProduct(1L, ProductStatus.ON_SHELF.getCode(), 100L);
            when(productMapper.selectById(1L)).thenReturn(product);

            assertThrows(BusinessException.class, () -> adminProductAuditService.auditPass(1L, 1L));
        }
    }

    @Nested
    @DisplayName("审核驳回测试")
    class AuditRejectTest {

        @Test
        @DisplayName("审核驳回成功")
        void auditReject_success() {
            // Arrange
            Product product = createTestProduct(1L, ProductStatus.DRAFT.getCode(), 100L);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(productAuditMapper.insert(any(ProductAudit.class))).thenReturn(1);

            // Act
            adminProductAuditService.auditReject(1L, 1L, "描述不清晰");

            // Assert
            verify(productMapper).updateById(argThat(p ->
                    p.getId().equals(1L) &&
                    p.getProductStatus().equals(ProductStatus.OFF_SHELF.getCode())
            ));
            verify(productAuditMapper).insert(argThat(a ->
                    a.getProductId().equals(1L) &&
                    a.getAuditResult().equals(AuditResult.REJECT.getCode()) &&
                    "描述不清晰".equals(a.getRejectReason())
            ));
        }
    }

    @Nested
    @DisplayName("批量审核测试")
    class BatchAuditTest {

        @Test
        @DisplayName("批量审核成功")
        void batchAuditPass_success() {
            // Arrange
            Product product1 = createTestProduct(1L, ProductStatus.DRAFT.getCode(), 100L);
            Product product2 = createTestProduct(2L, ProductStatus.DRAFT.getCode(), 101L);
            when(productMapper.selectById(1L)).thenReturn(product1);
            when(productMapper.selectById(2L)).thenReturn(product2);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(productAuditMapper.insert(any(ProductAudit.class))).thenReturn(1);

            // Act
            Integer count = adminProductAuditService.batchAuditPass(List.of(1L, 2L), 1L);

            // Assert
            assertEquals(2, count);
        }

        @Test
        @DisplayName("批量审核超过50个抛出异常")
        void batchAuditPass_tooMany_throwsException() {
            List<Long> ids = java.util.stream.IntStream.range(0, 51)
                    .mapToObj(i -> (long) i)
                    .toList();

            assertThrows(BusinessException.class, () -> adminProductAuditService.batchAuditPass(ids, 1L));
        }

        @Test
        @DisplayName("批量审核空列表返回0")
        void batchAuditPass_emptyList_returnsZero() {
            Integer count = adminProductAuditService.batchAuditPass(List.of(), 1L);
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("强制下架测试")
    class ForceOffShelfTest {

        @Test
        @DisplayName("强制下架成功并扣减信誉分")
        void forceOffShelf_success() {
            // Arrange
            Product product = createTestProduct(1L, ProductStatus.ON_SHELF.getCode(), 100L);
            User user = createTestUser(100L, 80);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(userMapper.selectById(100L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(productAuditMapper.insert(any(ProductAudit.class))).thenReturn(1);

            // Act
            adminProductAuditService.forceOffShelf(1L, 1L, "违规商品");

            // Assert
            verify(productMapper).updateById(argThat(p ->
                    p.getId().equals(1L) &&
                    p.getProductStatus().equals(ProductStatus.ILLEGAL.getCode()) &&
                    "违规商品".equals(p.getIllegalReason())
            ));
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(100L) && u.getCreditScore().equals(70)
            ));
        }

        @Test
        @DisplayName("强制下架信誉分不够扣时为0")
        void forceOffShelf_creditScoreFloor() {
            // Arrange
            Product product = createTestProduct(1L, ProductStatus.ON_SHELF.getCode(), 100L);
            User user = createTestUser(100L, 5);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(userMapper.selectById(100L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(productAuditMapper.insert(any(ProductAudit.class))).thenReturn(1);

            // Act
            adminProductAuditService.forceOffShelf(1L, 1L, "违规商品");

            // Assert
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(100L) && u.getCreditScore().equals(0)
            ));
        }
    }

    @Nested
    @DisplayName("审核记录测试")
    class GetAuditRecordsTest {

        @Test
        @DisplayName("获取审核记录成功")
        void getAuditRecords_success() {
            // Arrange
            ProductAudit audit = new ProductAudit();
            audit.setId(1L);
            audit.setProductId(1L);
            audit.setAdminId(1L);
            audit.setAuditResult(AuditResult.PASS.getCode());
            audit.setAuditTime(LocalDateTime.now());

            User admin = createTestUser(1L, 100);
            admin.setUsername("admin");

            when(productAuditMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(audit));
            when(userMapper.selectById(1L)).thenReturn(admin);

            // Act
            List<AuditRecordVO> records = adminProductAuditService.getAuditRecords(1L);

            // Assert
            assertNotNull(records);
            assertEquals(1, records.size());
            assertEquals("admin", records.get(0).getAdminName());
        }
    }

    @Nested
    @DisplayName("审核统计测试")
    class GetAuditStatisticsTest {

        @Test
        @DisplayName("获取审核统计成功")
        void getAuditStatistics_success() {
            // Arrange
            when(productMapper.countPendingAudit()).thenReturn(10);
            when(productAuditMapper.countTodayPass(any())).thenReturn(5);
            when(productAuditMapper.countTodayReject(any())).thenReturn(3);

            // Act
            AuditStatisticsVO stats = adminProductAuditService.getAuditStatistics();

            // Assert
            assertNotNull(stats);
            assertEquals(10, stats.getPendingCount());
            assertEquals(5, stats.getTodayPassCount());
            assertEquals(3, stats.getTodayRejectCount());
            // 通过率 = 5 / (5+3) * 100 = 62.5
            assertEquals(62.5, stats.getTodayPassRate(), 0.1);
        }

        @Test
        @DisplayName("审核统计-无审核记录时通过率为0")
        void getAuditStatistics_noRecords() {
            when(productMapper.countPendingAudit()).thenReturn(0);
            when(productAuditMapper.countTodayPass(any())).thenReturn(0);
            when(productAuditMapper.countTodayReject(any())).thenReturn(0);

            AuditStatisticsVO stats = adminProductAuditService.getAuditStatistics();

            assertEquals(0.0, stats.getTodayPassRate(), 0.1);
        }
    }
}