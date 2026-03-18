package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.SystemLogMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.SystemLog;
import com.fnusale.common.entity.User;
import com.fnusale.common.vo.admin.SystemLogVO;
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
 * SystemLogService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SystemLogServiceTest {

    @Mock
    private SystemLogMapper systemLogMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SystemLogServiceImpl systemLogService;

    private SystemLog createTestLog(Long id, Long userId, String module, String type) {
        SystemLog log = new SystemLog();
        log.setId(id);
        log.setOperateUserId(userId);
        log.setModuleName(module);
        log.setOperateType(type);
        log.setOperateContent("测试操作");
        log.setIpAddress("192.168.1.1");
        log.setDeviceInfo("Chrome");
        log.setLogType("OPERATE");
        log.setCreateTime(java.time.LocalDateTime.now());
        return log;
    }

    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    @Nested
    @DisplayName("记录日志测试")
    class LogTest {

        @Test
        @DisplayName("记录日志成功")
        void log_success() {
            // Arrange
            when(systemLogMapper.insert(any(SystemLog.class))).thenReturn(1);

            // Act
            systemLogService.log(1L, "USER", "UPDATE", "更新用户信息", "192.168.1.1", "Chrome");

            // Assert
            verify(systemLogMapper).insert(argThat(log ->
                    log.getOperateUserId().equals(1L) &&
                    log.getModuleName().equals("USER") &&
                    log.getOperateType().equals("UPDATE") &&
                    log.getOperateContent().equals("更新用户信息") &&
                    log.getIpAddress().equals("192.168.1.1") &&
                    log.getDeviceInfo().equals("Chrome") &&
                    log.getLogType().equals("OPERATE")
            ));
        }

        @Test
        @DisplayName("记录日志-null参数处理")
        void log_withNullParams() {
            // Arrange
            when(systemLogMapper.insert(any(SystemLog.class))).thenReturn(1);

            // Act
            systemLogService.log(1L, "SYSTEM", "QUERY", "查询操作", null, null);

            // Assert
            verify(systemLogMapper).insert(any(SystemLog.class));
        }
    }

    @Nested
    @DisplayName("日志分页查询测试")
    class GetLogPageTest {

        @Test
        @DisplayName("分页查询日志成功")
        void getLogPage_success() {
            // Arrange
            SystemLog log1 = createTestLog(1L, 1L, "USER", "UPDATE");
            SystemLog log2 = createTestLog(2L, 2L, "PRODUCT", "ADD");

            Page<SystemLog> mockPage = new Page<>(1, 10);
            mockPage.setTotal(2);
            mockPage.setRecords(List.of(log1, log2));

            User user1 = createTestUser(1L, "admin1");
            User user2 = createTestUser(2L, "admin2");

            when(systemLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(1L)).thenReturn(user1);
            when(userMapper.selectById(2L)).thenReturn(user2);

            // Act
            PageResult<SystemLogVO> result = systemLogService.getLogPage(null, null, 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(2, result.getList().size());
            assertEquals("admin1", result.getList().get(0).getOperateUsername());
            assertEquals("admin2", result.getList().get(1).getOperateUsername());
        }

        @Test
        @DisplayName("按模块查询日志")
        void getLogPage_withModule() {
            // Arrange
            SystemLog log = createTestLog(1L, 1L, "USER", "UPDATE");

            Page<SystemLog> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(log));

            User user = createTestUser(1L, "admin");

            when(systemLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(1L)).thenReturn(user);

            // Act
            PageResult<SystemLogVO> result = systemLogService.getLogPage("USER", null, 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("按操作类型查询日志")
        void getLogPage_withType() {
            // Arrange
            SystemLog log = createTestLog(1L, 1L, "USER", "UPDATE");

            Page<SystemLog> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(log));

            User user = createTestUser(1L, "admin");

            when(systemLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(1L)).thenReturn(user);

            // Act
            PageResult<SystemLogVO> result = systemLogService.getLogPage(null, "UPDATE", 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("查询日志-用户不存在时不设置用户名")
        void getLogPage_userNotFound() {
            // Arrange
            SystemLog log = createTestLog(1L, 999L, "SYSTEM", "QUERY");

            Page<SystemLog> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(log));

            when(systemLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(999L)).thenReturn(null);

            // Act
            PageResult<SystemLogVO> result = systemLogService.getLogPage(null, null, 1, 10);

            // Assert
            assertNotNull(result);
            assertNull(result.getList().get(0).getOperateUsername());
        }

        @Test
        @DisplayName("查询日志-空结果")
        void getLogPage_empty() {
            // Arrange
            Page<SystemLog> mockPage = new Page<>(1, 10);
            mockPage.setTotal(0);
            mockPage.setRecords(List.of());

            when(systemLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // Act
            PageResult<SystemLogVO> result = systemLogService.getLogPage(null, null, 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("导出日志测试")
    class ExportLogsTest {

        @Test
        @DisplayName("导出日志-目前返回null(TODO)")
        void exportLogs_returnsNull() {
            // Act
            String result = systemLogService.exportLogs("2024-01-01", "2024-01-31");

            // Assert
            assertNull(result); // TODO功能，暂返回null
        }
    }
}