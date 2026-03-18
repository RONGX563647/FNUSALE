package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.UserQueryDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.UserDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminUserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User createTestUser(Long id, String username, String authStatus, Integer creditScore) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAuthStatus(authStatus);
        user.setCreditScore(creditScore);
        user.setPhone("13812345678");
        user.setStudentTeacherId("2021001001");
        user.setIdentityType("STUDENT");
        user.setAuthResultRemark(null);
        user.setAuthImageUrl("http://example.com/auth.jpg");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setBirthday(null);
        user.setCampusEmail("test@campus.edu");
        user.setCreateTime(java.time.LocalDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("用户分页查询测试")
    class GetUserPageTest {

        @Test
        @DisplayName("正常分页查询返回结果")
        void getUserPage_success() {
            // Arrange
            UserQueryDTO query = new UserQueryDTO();
            query.setPageNum(1);
            query.setPageSize(10);
            query.setUsername("test");
            query.setAuthStatus("AUTH_SUCCESS");
            query.setIdentityType("STUDENT");

            Page<UserDetailVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            UserDetailVO vo = new UserDetailVO();
            vo.setUserId(1L);
            vo.setUsername("testuser");
            mockPage.setRecords(List.of(vo));

            IPage<UserDetailVO> iPage = mockPage;
            when(userMapper.selectUserPage(any(Page.class), eq("test"), eq("AUTH_SUCCESS"), eq("STUDENT")))
                    .thenReturn(iPage);

            // Act
            PageResult<UserDetailVO> result = adminUserService.getUserPage(query);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
            assertEquals("testuser", result.getList().get(0).getUsername());
        }
    }

    @Nested
    @DisplayName("用户详情查询测试")
    class GetUserDetailTest {

        @Test
        @DisplayName("查询存在的用户返回详情")
        void getUserDetail_existingUser_returnsDetail() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 80);
            when(userMapper.selectById(1L)).thenReturn(user);

            // Act
            UserDetailVO result = adminUserService.getUserDetail(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("testuser", result.getUsername());
            assertEquals(80, result.getCreditScore());
            // 验证手机号脱敏
            assertEquals("138****5678", result.getPhone());
            // 验证学号脱敏 - "2021001001" -> "****1001"
            assertEquals("****1001", result.getStudentTeacherId());
        }

        @Test
        @DisplayName("查询不存在的用户抛出异常")
        void getUserDetail_nonExistingUser_throwsException() {
            // Arrange
            when(userMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThrows(BusinessException.class, () -> adminUserService.getUserDetail(999L));
        }
    }

    @Nested
    @DisplayName("认证审核测试")
    class AuthTest {

        @Test
        @DisplayName("认证通过成功")
        void authPass_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.UNDER_REVIEW.getCode(), 60);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminUserService.authPass(1L, 100L);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(1L) &&
                    u.getAuthStatus().equals(AuthStatus.AUTH_SUCCESS.getCode())
            ));
            verify(systemLogService).log(eq(100L), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("认证通过-用户不在审核中抛出异常")
        void authPass_notUnderReview_throwsException() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 60);
            when(userMapper.selectById(1L)).thenReturn(user);

            // Act & Assert
            assertThrows(BusinessException.class, () -> adminUserService.authPass(1L, 100L));
        }

        @Test
        @DisplayName("认证驳回成功")
        void authReject_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.UNDER_REVIEW.getCode(), 60);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminUserService.authReject(1L, 100L, "材料不清晰");

            // Assert
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(1L) &&
                    u.getAuthStatus().equals(AuthStatus.AUTH_FAILED.getCode()) &&
                    "材料不清晰".equals(u.getAuthResultRemark())
            ));
        }
    }

    @Nested
    @DisplayName("用户封禁测试")
    class BanUserTest {

        @Test
        @DisplayName("封禁用户成功-设置信誉分为0")
        void banUser_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 80);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminUserService.banUser(1L, 100L, "违规操作");

            // Assert
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(1L) && u.getCreditScore().equals(0)
            ));
        }

        @Test
        @DisplayName("解封用户成功-恢复信誉分为60")
        void unbanUser_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 0);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminUserService.unbanUser(1L, 100L);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                    u.getId().equals(1L) && u.getCreditScore().equals(60)
            ));
        }
    }

    @Nested
    @DisplayName("信誉分调整测试")
    class AdjustCreditTest {

        @Test
        @DisplayName("增加信誉分成功")
        void adjustCredit_increase_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 80);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            Integer newScore = adminUserService.adjustCredit(1L, 10, "奖励", 100L);

            // Assert
            assertEquals(90, newScore);
        }

        @Test
        @DisplayName("减少信誉分成功")
        void adjustCredit_decrease_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 80);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            Integer newScore = adminUserService.adjustCredit(1L, -10, "违规", 100L);

            // Assert
            assertEquals(70, newScore);
        }

        @Test
        @DisplayName("信誉分上限为100")
        void adjustCredit_max100() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 95);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            Integer newScore = adminUserService.adjustCredit(1L, 10, "奖励", 100L);

            // Assert
            assertEquals(100, newScore);
        }

        @Test
        @DisplayName("信誉分下限为0")
        void adjustCredit_min0() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.AUTH_SUCCESS.getCode(), 5);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            Integer newScore = adminUserService.adjustCredit(1L, -10, "严重违规", 100L);

            // Assert
            assertEquals(0, newScore);
        }
    }

    @Nested
    @DisplayName("待审核认证列表测试")
    class GetPendingAuthListTest {

        @Test
        @DisplayName("查询待审核认证列表成功")
        void getPendingAuthList_success() {
            // Arrange
            User user = createTestUser(1L, "testuser", AuthStatus.UNDER_REVIEW.getCode(), 60);
            Page<User> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(user));

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // Act
            PageResult<UserDetailVO> result = adminUserService.getPendingAuthList(1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }
    }
}