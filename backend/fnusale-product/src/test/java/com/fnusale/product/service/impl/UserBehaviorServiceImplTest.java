package com.fnusale.product.service.impl;

import com.fnusale.common.entity.Product;
import com.fnusale.common.entity.UserBehavior;
import com.fnusale.common.enums.BehaviorType;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.product.mapper.ProductMapper;
import com.fnusale.product.mapper.UserBehaviorMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户行为服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserBehaviorServiceImplTest {

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private UserBehaviorServiceImpl userBehaviorService;

    private Product testProduct;
    private UserBehavior testBehavior;

    @BeforeEach
    void setUp() {
        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("测试商品");
        testProduct.setUserId(2L);

        // 初始化测试行为记录
        testBehavior = new UserBehavior();
        testBehavior.setId(1L);
        testBehavior.setUserId(1L);
        testBehavior.setProductId(1L);
        testBehavior.setBehaviorType(BehaviorType.COLLECT.getCode());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("收藏商品测试")
    class AddFavoriteTests {

        @Test
        @DisplayName("正常收藏_成功")
        void addFavorite_success() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(userBehaviorMapper.checkFavorite(1L, 1L)).thenReturn(0);
            when(userBehaviorMapper.insert(any(UserBehavior.class))).thenReturn(1);

            assertDoesNotThrow(() -> userBehaviorService.addFavorite(1L));
            verify(userBehaviorMapper).insert(any(UserBehavior.class));
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void addFavorite_notLogin_throwsException() {
            assertThrows(BusinessException.class, () -> userBehaviorService.addFavorite(1L));
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void addFavorite_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userBehaviorService.addFavorite(1L));
        }

        @Test
        @DisplayName("已收藏_抛出异常")
        void addFavorite_alreadyFavorited_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(userBehaviorMapper.checkFavorite(1L, 1L)).thenReturn(1);

            assertThrows(BusinessException.class, () -> userBehaviorService.addFavorite(1L));
        }
    }

    @Nested
    @DisplayName("取消收藏测试")
    class RemoveFavoriteTests {

        @Test
        @DisplayName("正常取消收藏_成功")
        void removeFavorite_success() {
            UserContext.setCurrentUserId(1L);

            when(userBehaviorMapper.selectByUserProductBehavior(1L, 1L, BehaviorType.COLLECT.getCode()))
                    .thenReturn(testBehavior);
            when(userBehaviorMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> userBehaviorService.removeFavorite(1L));
            verify(userBehaviorMapper).deleteById(1L);
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void removeFavorite_notLogin_throwsException() {
            assertThrows(BusinessException.class, () -> userBehaviorService.removeFavorite(1L));
        }

        @Test
        @DisplayName("未收藏_抛出异常")
        void removeFavorite_notFavorited_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(userBehaviorMapper.selectByUserProductBehavior(1L, 1L, BehaviorType.COLLECT.getCode()))
                    .thenReturn(null);

            assertThrows(BusinessException.class, () -> userBehaviorService.removeFavorite(1L));
        }
    }

    @Nested
    @DisplayName("点赞商品测试")
    class AddLikeTests {

        @Test
        @DisplayName("正常点赞_成功")
        void addLike_success() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(userBehaviorMapper.checkLike(1L, 1L)).thenReturn(0);
            when(userBehaviorMapper.insert(any(UserBehavior.class))).thenReturn(1);

            assertDoesNotThrow(() -> userBehaviorService.addLike(1L));
            verify(userBehaviorMapper).insert(any(UserBehavior.class));
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void addLike_notLogin_throwsException() {
            assertThrows(BusinessException.class, () -> userBehaviorService.addLike(1L));
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void addLike_productNotFound_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userBehaviorService.addLike(1L));
        }

        @Test
        @DisplayName("已点赞_抛出异常")
        void addLike_alreadyLiked_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(userBehaviorMapper.checkLike(1L, 1L)).thenReturn(1);

            assertThrows(BusinessException.class, () -> userBehaviorService.addLike(1L));
        }
    }

    @Nested
    @DisplayName("取消点赞测试")
    class RemoveLikeTests {

        @Test
        @DisplayName("正常取消点赞_成功")
        void removeLike_success() {
            UserContext.setCurrentUserId(1L);
            testBehavior.setBehaviorType(BehaviorType.LIKE.getCode());

            when(userBehaviorMapper.selectByUserProductBehavior(1L, 1L, BehaviorType.LIKE.getCode()))
                    .thenReturn(testBehavior);
            when(userBehaviorMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> userBehaviorService.removeLike(1L));
            verify(userBehaviorMapper).deleteById(1L);
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void removeLike_notLogin_throwsException() {
            assertThrows(BusinessException.class, () -> userBehaviorService.removeLike(1L));
        }

        @Test
        @DisplayName("未点赞_抛出异常")
        void removeLike_notLiked_throwsException() {
            UserContext.setCurrentUserId(1L);

            when(userBehaviorMapper.selectByUserProductBehavior(1L, 1L, BehaviorType.LIKE.getCode()))
                    .thenReturn(null);

            assertThrows(BusinessException.class, () -> userBehaviorService.removeLike(1L));
        }
    }

    @Nested
    @DisplayName("记录浏览测试")
    class RecordBrowseTests {

        @Test
        @DisplayName("已登录_记录成功")
        void recordBrowse_loggedIn_success() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(userBehaviorMapper.insert(any(UserBehavior.class))).thenReturn(1);

            assertDoesNotThrow(() -> userBehaviorService.recordBrowse(1L));
            verify(userBehaviorMapper).insert(any(UserBehavior.class));
        }

        @Test
        @DisplayName("未登录_不记录")
        void recordBrowse_notLoggedIn_noRecord() {
            userBehaviorService.recordBrowse(1L);

            verify(userBehaviorMapper, never()).insert(any());
        }

        @Test
        @DisplayName("商品不存在_不记录")
        void recordBrowse_productNotFound_noRecord() {
            UserContext.setCurrentUserId(1L);

            when(productMapper.selectById(1L)).thenReturn(null);

            assertDoesNotThrow(() -> userBehaviorService.recordBrowse(1L));
            verify(userBehaviorMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("检查是否收藏测试")
    class IsFavoritedTests {

        @Test
        @DisplayName("已收藏_返回true")
        void isFavorited_true() {
            when(userBehaviorMapper.checkFavorite(1L, 1L)).thenReturn(1);

            boolean result = userBehaviorService.isFavorited(1L, 1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("未收藏_返回false")
        void isFavorited_false() {
            when(userBehaviorMapper.checkFavorite(1L, 1L)).thenReturn(0);

            boolean result = userBehaviorService.isFavorited(1L, 1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("参数为空_返回false")
        void isFavorited_nullParams_returnsFalse() {
            assertFalse(userBehaviorService.isFavorited(null, 1L));
            assertFalse(userBehaviorService.isFavorited(1L, null));
            assertFalse(userBehaviorService.isFavorited(null, null));
        }
    }

    @Nested
    @DisplayName("检查是否点赞测试")
    class IsLikedTests {

        @Test
        @DisplayName("已点赞_返回true")
        void isLiked_true() {
            when(userBehaviorMapper.checkLike(1L, 1L)).thenReturn(1);

            boolean result = userBehaviorService.isLiked(1L, 1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("未点赞_返回false")
        void isLiked_false() {
            when(userBehaviorMapper.checkLike(1L, 1L)).thenReturn(0);

            boolean result = userBehaviorService.isLiked(1L, 1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("参数为空_返回false")
        void isLiked_nullParams_returnsFalse() {
            assertFalse(userBehaviorService.isLiked(null, 1L));
            assertFalse(userBehaviorService.isLiked(1L, null));
            assertFalse(userBehaviorService.isLiked(null, null));
        }
    }
}