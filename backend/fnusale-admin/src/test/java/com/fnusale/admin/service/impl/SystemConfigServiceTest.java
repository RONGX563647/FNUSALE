package com.fnusale.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.admin.mapper.SystemConfigHistoryMapper;
import com.fnusale.admin.mapper.SystemConfigMapper;
import com.fnusale.common.dto.admin.CampusFenceDTO;
import com.fnusale.common.dto.admin.ConfigUpdateDTO;
import com.fnusale.common.dto.admin.SeckillConfigDTO;
import com.fnusale.common.entity.SystemConfig;
import com.fnusale.common.vo.admin.ConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * SystemConfigService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigMapper systemConfigMapper;

    @Mock
    private SystemConfigHistoryMapper systemConfigHistoryMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    private SystemConfig createTestConfig(Long id, String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setId(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigDesc("测试配置");
        config.setAdminId(1L);
        return config;
    }

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("配置列表测试")
    class GetConfigListTest {

        @Test
        @DisplayName("获取配置列表成功")
        void getConfigList_success() {
            // Arrange
            SystemConfig config1 = createTestConfig(1L, "app_name", "FNUSALE");
            SystemConfig config2 = createTestConfig(2L, "app_version", "1.0.0");
            when(systemConfigMapper.selectList(any())).thenReturn(List.of(config1, config2));

            // Act
            List<ConfigVO> result = systemConfigService.getConfigList();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("获取配置测试")
    class GetConfigByKeyTest {

        @Test
        @DisplayName("从缓存获取配置成功")
        void getConfigByKey_fromCache() {
            // Arrange
            when(valueOperations.get("admin:config:app_name")).thenReturn("FNUSALE");

            // Act
            ConfigVO result = systemConfigService.getConfigByKey("app_name");

            // Assert
            assertNotNull(result);
            assertEquals("app_name", result.getConfigKey());
            assertEquals("FNUSALE", result.getConfigValue());
            verify(systemConfigMapper, never()).selectByConfigKey(any());
        }

        @Test
        @DisplayName("从数据库获取配置成功")
        void getConfigByKey_fromDatabase() {
            // Arrange
            when(valueOperations.get("admin:config:app_name")).thenReturn(null);
            SystemConfig config = createTestConfig(1L, "app_name", "FNUSALE");
            when(systemConfigMapper.selectByConfigKey("app_name")).thenReturn(config);

            // Act
            ConfigVO result = systemConfigService.getConfigByKey("app_name");

            // Assert
            assertNotNull(result);
            assertEquals("app_name", result.getConfigKey());
            assertEquals("FNUSALE", result.getConfigValue());
            verify(valueOperations).set(eq("admin:config:app_name"), eq("FNUSALE"), eq(1L), any());
        }

        @Test
        @DisplayName("配置不存在返回null")
        void getConfigByKey_notFound() {
            // Arrange
            when(valueOperations.get(any())).thenReturn(null);
            when(systemConfigMapper.selectByConfigKey(any())).thenReturn(null);

            // Act
            ConfigVO result = systemConfigService.getConfigByKey("nonexistent");

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("更新配置测试")
    class UpdateConfigTest {

        @Test
        @DisplayName("更新配置成功")
        void updateConfig_success() {
            // Arrange
            SystemConfig config = createTestConfig(1L, "app_name", "OLD_VALUE");
            when(systemConfigMapper.selectByConfigKey("app_name")).thenReturn(config);
            when(systemConfigMapper.updateById(any())).thenReturn(1);
            when(systemConfigHistoryMapper.insert(any())).thenReturn(1);

            // Act
            systemConfigService.updateConfig("app_name", "NEW_VALUE", 1L);

            // Assert
            verify(systemConfigMapper).updateById(argThat(c ->
                    c.getConfigValue().equals("NEW_VALUE") &&
                    c.getAdminId().equals(1L)
            ));
            verify(valueOperations).set(eq("admin:config:app_name"), eq("NEW_VALUE"), eq(1L), any());
        }

        @Test
        @DisplayName("更新不存在的配置抛出异常")
        void updateConfig_notFound_throwsException() {
            when(systemConfigMapper.selectByConfigKey(any())).thenReturn(null);

            assertThrows(IllegalArgumentException.class,
                    () -> systemConfigService.updateConfig("nonexistent", "value", 1L));
        }
    }

    @Nested
    @DisplayName("批量更新配置测试")
    class BatchUpdateConfigTest {

        @Test
        @DisplayName("批量更新配置成功")
        void batchUpdateConfig_success() {
            // Arrange
            SystemConfig config1 = createTestConfig(1L, "key1", "value1");
            SystemConfig config2 = createTestConfig(2L, "key2", "value2");
            when(systemConfigMapper.selectByConfigKey("key1")).thenReturn(config1);
            when(systemConfigMapper.selectByConfigKey("key2")).thenReturn(config2);
            when(systemConfigMapper.updateById(any())).thenReturn(1);
            when(systemConfigHistoryMapper.insert(any())).thenReturn(1);

            ConfigUpdateDTO dto1 = new ConfigUpdateDTO();
            dto1.setConfigKey("key1");
            dto1.setConfigValue("newValue1");
            ConfigUpdateDTO dto2 = new ConfigUpdateDTO();
            dto2.setConfigKey("key2");
            dto2.setConfigValue("newValue2");

            // Act
            systemConfigService.batchUpdateConfig(List.of(dto1, dto2), 1L);

            // Assert
            verify(systemConfigMapper, times(2)).updateById(any());
        }
    }

    @Nested
    @DisplayName("校园围栏配置测试")
    class CampusFenceConfigTest {

        @Test
        @DisplayName("获取校园围栏配置-有数据")
        void getCampusFenceConfig_withData() throws Exception {
            // Arrange
            CampusFenceDTO fenceDTO = new CampusFenceDTO();
            CampusFenceDTO.FencePoint point = new CampusFenceDTO.FencePoint();
            point.setLng(113.0);
            point.setLat(23.0);
            fenceDTO.setFencePoints(List.of(point));
            String json = objectMapper.writeValueAsString(fenceDTO);

            when(valueOperations.get("admin:config:campus_fence")).thenReturn(json);

            // Act
            CampusFenceDTO result = systemConfigService.getCampusFenceConfig();

            // Assert
            assertNotNull(result);
            assertNotNull(result.getFencePoints());
            assertEquals(1, result.getFencePoints().size());
        }

        @Test
        @DisplayName("获取校园围栏配置-无数据返回空对象")
        void getCampusFenceConfig_noData() {
            // Arrange
            when(valueOperations.get(any())).thenReturn(null);
            when(systemConfigMapper.selectByConfigKey(any())).thenReturn(null);

            // Act
            CampusFenceDTO result = systemConfigService.getCampusFenceConfig();

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("更新校园围栏配置成功")
        void updateCampusFenceConfig_success() {
            // Arrange
            SystemConfig config = createTestConfig(1L, "campus_fence", "{}");
            when(systemConfigMapper.selectByConfigKey("campus_fence")).thenReturn(config);
            when(systemConfigMapper.updateById(any())).thenReturn(1);
            when(systemConfigHistoryMapper.insert(any())).thenReturn(1);

            CampusFenceDTO dto = new CampusFenceDTO();
            CampusFenceDTO.FencePoint point = new CampusFenceDTO.FencePoint();
            point.setLng(113.0);
            point.setLat(23.0);
            dto.setFencePoints(List.of(point));

            // Act
            systemConfigService.updateCampusFenceConfig(dto, 1L);

            // Assert
            verify(systemConfigMapper).updateById(any());
        }
    }

    @Nested
    @DisplayName("秒杀配置测试")
    class SeckillConfigTest {

        @Test
        @DisplayName("获取秒杀配置-有数据")
        void getSeckillConfig_withData() throws Exception {
            // Arrange
            SeckillConfigDTO seckillDTO = new SeckillConfigDTO();
            seckillDTO.setLimitPerUser(5);
            String json = objectMapper.writeValueAsString(seckillDTO);

            when(valueOperations.get("admin:config:seckill_config")).thenReturn(json);

            // Act
            SeckillConfigDTO result = systemConfigService.getSeckillConfig();

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getLimitPerUser());
        }

        @Test
        @DisplayName("获取秒杀配置-无数据返回空对象")
        void getSeckillConfig_noData() {
            // Arrange
            when(valueOperations.get(any())).thenReturn(null);
            when(systemConfigMapper.selectByConfigKey(any())).thenReturn(null);

            // Act
            SeckillConfigDTO result = systemConfigService.getSeckillConfig();

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("更新秒杀配置成功")
        void updateSeckillConfig_success() {
            // Arrange
            SystemConfig config = createTestConfig(1L, "seckill_config", "{}");
            when(systemConfigMapper.selectByConfigKey("seckill_config")).thenReturn(config);
            when(systemConfigMapper.updateById(any())).thenReturn(1);
            when(systemConfigHistoryMapper.insert(any())).thenReturn(1);

            SeckillConfigDTO dto = new SeckillConfigDTO();
            dto.setLimitPerUser(10);
            dto.setQpsLimit(500);

            // Act
            systemConfigService.updateSeckillConfig(dto, 1L);

            // Assert
            verify(systemConfigMapper).updateById(any());
        }
    }

    @Nested
    @DisplayName("刷新缓存测试")
    class RefreshCacheTest {

        @Test
        @DisplayName("刷新缓存成功")
        void refreshCache_success() {
            // Arrange
            Set<String> keys = Set.of("admin:config:key1", "admin:config:key2");
            when(redisTemplate.keys("admin:config:*")).thenReturn(keys);

            // Act
            systemConfigService.refreshCache();

            // Assert
            verify(redisTemplate).delete((java.util.Collection<String>) keys);
        }

        @Test
        @DisplayName("刷新缓存-无缓存键")
        void refreshCache_noKeys() {
            // Arrange
            when(redisTemplate.keys(any())).thenReturn(null);

            // Act
            systemConfigService.refreshCache();

            // Assert
            verify(redisTemplate, never()).delete(any(java.util.Collection.class));
        }
    }
}