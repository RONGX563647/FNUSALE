package com.fnusale.im.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.Result;
import com.fnusale.common.entity.ImMessage;
import com.fnusale.common.entity.ImSession;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.common.vo.im.SessionVO;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.im.client.ProductClient;
import com.fnusale.im.client.UserClient;
import com.fnusale.im.dto.SessionCreateDTO;
import com.fnusale.im.mapper.ImMessageMapper;
import com.fnusale.im.mapper.ImSessionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 会话服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImSessionServiceImplTest {

    @Mock
    private ImSessionMapper sessionMapper;

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private UserClient userClient;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private ImSessionServiceImpl sessionService;

    private ImSession testSession;
    private UserVO testUser;
    private ProductVO testProduct;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        // 初始化测试会话
        testSession = new ImSession();
        testSession.setId(1L);
        testSession.setUser1Id(1L);
        testSession.setUser2Id(2L);
        testSession.setProductId(100L);
        testSession.setUnreadCountU1(0);
        testSession.setUnreadCountU2(2);
        testSession.setSessionStatus("NORMAL");
        testSession.setIsPinnedU1(0);
        testSession.setIsPinnedU2(0);
        testSession.setIsDeletedU1(0);
        testSession.setIsDeletedU2(0);
        testSession.setCreateTime(LocalDateTime.now());
        testSession.setUpdateTime(LocalDateTime.now());

        // 初始化测试用户
        testUser = new UserVO();
        testUser.setId(2L);
        testUser.setUsername("seller");
        testUser.setAvatarUrl("http://example.com/avatar.jpg");
        testUser.setAuthStatus(AuthStatus.AUTH_SUCCESS.getCode());

        // 初始化测试商品
        testProduct = new ProductVO();
        testProduct.setId(100L);
        testProduct.setProductName("测试商品");
        testProduct.setPrice(new BigDecimal("99.00"));
        testProduct.setMainImageUrl("http://example.com/product.jpg");
        testProduct.setUserId(2L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clearCurrentUserId();
    }

    @Nested
    @DisplayName("创建会话测试")
    class CreateSessionTests {

        @Test
        @DisplayName("正常创建会话_成功")
        void createSession_success() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(2L);
            dto.setProductId(100L);

            when(userClient.getAuthStatus(1L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(userClient.getAuthStatus(2L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(productClient.getProductById(100L)).thenReturn(Result.success(testProduct));
            when(sessionMapper.selectByUsersAndProduct(1L, 2L, 100L)).thenReturn(null);
            when(sessionMapper.insert(any(ImSession.class))).thenAnswer(invocation -> {
                ImSession session = invocation.getArgument(0);
                session.setId(1L);
                return 1;
            });

            Long sessionId = sessionService.createSession(dto);

            assertNotNull(sessionId);
            verify(sessionMapper).insert(any(ImSession.class));
        }

        @Test
        @DisplayName("会话已存在_返回已存在会话ID")
        void createSession_alreadyExists_returnsExistingId() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(2L);
            dto.setProductId(100L);

            when(userClient.getAuthStatus(1L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(userClient.getAuthStatus(2L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(productClient.getProductById(100L)).thenReturn(Result.success(testProduct));
            when(sessionMapper.selectByUsersAndProduct(1L, 2L, 100L)).thenReturn(testSession);

            Long sessionId = sessionService.createSession(dto);

            assertEquals(1L, sessionId);
            verify(sessionMapper, never()).insert(any(ImSession.class));
        }

        @Test
        @DisplayName("与自己创建会话_抛出异常")
        void createSession_withSelf_throwsException() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(1L);
            dto.setProductId(100L);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.createSession(dto));
            assertEquals("不能与自己创建会话", exception.getMessage());
        }

        @Test
        @DisplayName("用户未认证_抛出异常")
        void createSession_userNotAuth_throwsException() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(2L);
            dto.setProductId(100L);

            when(userClient.getAuthStatus(1L)).thenReturn(Result.success(AuthStatus.UNAUTH.getCode()));

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.createSession(dto));
            assertEquals("用户未完成校园认证，无法发起会话", exception.getMessage());
        }

        @Test
        @DisplayName("商品不存在_抛出异常")
        void createSession_productNotFound_throwsException() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(2L);
            dto.setProductId(999L);

            when(userClient.getAuthStatus(1L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(userClient.getAuthStatus(2L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(productClient.getProductById(999L)).thenReturn(Result.success(null));

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.createSession(dto));
            assertEquals("商品不存在", exception.getMessage());
        }

        @Test
        @DisplayName("商品发布者不匹配_抛出异常")
        void createSession_sellerMismatch_throwsException() {
            SessionCreateDTO dto = new SessionCreateDTO();
            dto.setTargetUserId(3L); // 与商品发布者不一致
            dto.setProductId(100L);

            when(userClient.getAuthStatus(1L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(userClient.getAuthStatus(3L)).thenReturn(Result.success(AuthStatus.AUTH_SUCCESS.getCode()));
            when(productClient.getProductById(100L)).thenReturn(Result.success(testProduct));

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.createSession(dto));
            assertEquals("商品发布者信息不匹配", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("获取会话列表测试")
    class GetSessionListTests {

        @Test
        @DisplayName("获取会话列表_成功")
        void getSessionList_success() {
            List<ImSession> sessions = new ArrayList<>();
            sessions.add(testSession);

            // Mock批量查询用户接口
            Map<Long, UserVO> userMap = new HashMap<>();
            userMap.put(1L, createUserVO(1L, "buyer"));
            userMap.put(2L, testUser);

            // Mock批量查询商品接口
            Map<Long, ProductVO> productMap = new HashMap<>();
            productMap.put(100L, testProduct);

            when(sessionMapper.selectByUserId(1L)).thenReturn(sessions);
            when(userClient.getUsersByIds(anyList())).thenReturn(Result.success(userMap));
            when(productClient.getProductsByIds(anyList())).thenReturn(Result.success(productMap));

            List<SessionVO> result = sessionService.getSessionList();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getSessionId());
            assertEquals(0, result.get(0).getUnreadCount()); // 用户1是user1，未读数为unreadCountU1=0
        }

        @Test
        @DisplayName("空会话列表_返回空列表")
        void getSessionList_empty_returnsEmptyList() {
            when(sessionMapper.selectByUserId(1L)).thenReturn(Collections.emptyList());

            List<SessionVO> result = sessionService.getSessionList();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取会话详情测试")
    class GetSessionByIdTests {

        @Test
        @DisplayName("获取会话详情_成功")
        void getSessionById_success() {
            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(userClient.getUserById(2L)).thenReturn(Result.success(testUser));
            when(productClient.getProductById(100L)).thenReturn(Result.success(testProduct));

            SessionVO result = sessionService.getSessionById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getSessionId());
            assertEquals(2L, result.getTargetUserId());
            assertEquals(100L, result.getProductId());
        }

        @Test
        @DisplayName("会话不存在_抛出异常")
        void getSessionById_notFound_throwsException() {
            when(sessionMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.getSessionById(999L));
            assertEquals("会话不存在", exception.getMessage());
        }

        @Test
        @DisplayName("非会话参与者_抛出异常")
        void getSessionById_notParticipant_throwsException() {
            UserContext.clearCurrentUserId();
            UserContext.setCurrentUserId(999L);

            when(sessionMapper.selectById(1L)).thenReturn(testSession);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.getSessionById(1L));
            assertEquals("您不是该会话的参与者", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("获取未读消息数测试")
    class GetUnreadCountTests {

        @Test
        @DisplayName("获取未读消息数_成功")
        void getUnreadCount_success() {
            when(messageMapper.countUnreadByUserId(1L)).thenReturn(5);

            Integer count = sessionService.getUnreadCount();

            assertEquals(5, count);
        }
    }

    @Nested
    @DisplayName("标记会话已读测试")
    class MarkAsReadTests {

        @Test
        @DisplayName("标记会话已读_成功")
        void markAsRead_success() {
            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(messageMapper.markAsReadBySession(1L, 1L)).thenReturn(2);

            assertDoesNotThrow(() -> sessionService.markAsRead(1L));
            verify(messageMapper).markAsReadBySession(1L, 1L);
            verify(sessionMapper).resetUnreadU1(1L);
        }

        @Test
        @DisplayName("会话不存在_抛出异常")
        void markAsRead_sessionNotFound_throwsException() {
            when(sessionMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.markAsRead(999L));
            assertEquals("会话不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("置顶会话测试")
    class PinSessionTests {

        @Test
        @DisplayName("置顶会话_成功")
        void pinSession_success() {
            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(sessionMapper.countPinnedByUserId(1L)).thenReturn(2);

            assertDoesNotThrow(() -> sessionService.pinSession(1L));
            verify(sessionMapper).updateById(any(ImSession.class));
        }

        @Test
        @DisplayName("置顶数量超限_抛出异常")
        void pinSession_exceedLimit_throwsException() {
            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(sessionMapper.countPinnedByUserId(1L)).thenReturn(5);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.pinSession(1L));
            assertEquals("最多只能置顶5个会话", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("取消置顶测试")
    class UnpinSessionTests {

        @Test
        @DisplayName("取消置顶_成功")
        void unpinSession_success() {
            testSession.setIsPinnedU1(1);
            when(sessionMapper.selectById(1L)).thenReturn(testSession);

            assertDoesNotThrow(() -> sessionService.unpinSession(1L));
            verify(sessionMapper).updateById(any(ImSession.class));
        }
    }

    @Nested
    @DisplayName("获取会话消息列表测试")
    class GetMessagesTests {

        @Test
        @DisplayName("获取消息列表_成功")
        void getMessages_success() {
            Page<ImMessage> page = new Page<>(1, 20);
            List<ImMessage> messages = new ArrayList<>();
            ImMessage msg = new ImMessage();
            msg.setId(1L);
            msg.setSessionId(1L);
            msg.setSenderId(1L);
            msg.setReceiverId(2L);
            msg.setMessageType("TEXT");
            msg.setMessageContent("测试消息");
            msg.setSendTime(LocalDateTime.now());
            msg.setIsRead(0);
            messages.add(msg);
            page.setRecords(messages);
            page.setTotal(1);

            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(messageMapper.selectBySessionId(any(Page.class), eq(1L))).thenReturn(page);

            var result = sessionService.getMessages(1L, 1, 20);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }
    }

    @Nested
    @DisplayName("删除会话测试")
    class DeleteSessionTests {

        @Test
        @DisplayName("用户1删除会话_成功")
        void deleteSession_user1_success() {
            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(sessionMapper.softDeleteU1(1L)).thenReturn(1);

            assertDoesNotThrow(() -> sessionService.deleteSession(1L));
            verify(sessionMapper).softDeleteU1(1L);
        }

        @Test
        @DisplayName("用户2删除会话_成功")
        void deleteSession_user2_success() {
            UserContext.clearCurrentUserId();
            UserContext.setCurrentUserId(2L);

            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(sessionMapper.softDeleteU2(1L)).thenReturn(1);

            assertDoesNotThrow(() -> sessionService.deleteSession(1L));
            verify(sessionMapper).softDeleteU2(1L);
        }

        @Test
        @DisplayName("会话不存在_抛出异常")
        void deleteSession_notFound_throwsException() {
            when(sessionMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.deleteSession(999L));
            assertEquals("会话不存在", exception.getMessage());
        }

        @Test
        @DisplayName("非会话参与者_抛出异常")
        void deleteSession_notParticipant_throwsException() {
            UserContext.clearCurrentUserId();
            UserContext.setCurrentUserId(999L);

            when(sessionMapper.selectById(1L)).thenReturn(testSession);

            BusinessException exception = assertThrows(BusinessException.class, () -> sessionService.deleteSession(1L));
            assertEquals("您不是该会话的参与者", exception.getMessage());
        }
    }

    // 辅助方法
    private UserVO createUserVO(Long id, String username) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setUsername(username);
        user.setAvatarUrl("http://example.com/avatar.jpg");
        return user;
    }
}