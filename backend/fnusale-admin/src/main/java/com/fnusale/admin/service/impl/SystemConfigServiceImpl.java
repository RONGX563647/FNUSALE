package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.admin.mapper.SystemConfigHistoryMapper;
import com.fnusale.admin.mapper.SystemConfigMapper;
import com.fnusale.admin.service.SystemConfigService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.CampusFenceDTO;
import com.fnusale.common.dto.admin.ConfigUpdateDTO;
import com.fnusale.common.dto.admin.SeckillConfigDTO;
import com.fnusale.common.entity.SystemConfig;
import com.fnusale.common.entity.SystemConfigHistory;
import com.fnusale.common.vo.admin.ConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final SystemConfigHistoryMapper systemConfigHistoryMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CONFIG_CACHE_PREFIX = "admin:config:";
    private static final String CAMPUS_FENCE_KEY = "campus_fence";
    private static final String SECKILL_CONFIG_KEY = "seckill_config";

    @Override
    public List<ConfigVO> getConfigList() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SystemConfig::getConfigKey);
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        return configs.stream().map(this::convertToVO).toList();
    }

    @Override
    public ConfigVO getConfigByKey(String configKey) {
        // 先查缓存
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            ConfigVO vo = new ConfigVO();
            vo.setConfigKey(configKey);
            vo.setConfigValue(cachedValue);
            return vo;
        }

        // 查数据库
        SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            return null;
        }

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), 1, TimeUnit.HOURS);

        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue, Long adminId) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            throw new IllegalArgumentException("配置项不存在: " + configKey);
        }

        String oldValue = config.getConfigValue();

        // 记录配置修改历史
        SystemConfigHistory history = new SystemConfigHistory();
        history.setConfigKey(configKey);
        history.setOldValue(oldValue);
        history.setNewValue(configValue);
        history.setAdminId(adminId);
        history.setOperateTime(LocalDateTime.now());
        systemConfigHistoryMapper.insert(history);

        // 更新配置
        config.setConfigValue(configValue);
        config.setUpdateTime(LocalDateTime.now());
        config.setAdminId(adminId);
        systemConfigMapper.updateById(config);

        // 更新缓存
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        redisTemplate.opsForValue().set(cacheKey, configValue, 1, TimeUnit.HOURS);

        log.info("更新配置, key: {}, value: {}, adminId: {}", configKey, configValue, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfig(List<ConfigUpdateDTO> configs, Long adminId) {
        for (ConfigUpdateDTO dto : configs) {
            updateConfig(dto.getConfigKey(), dto.getConfigValue(), adminId);
        }
    }

    @Override
    public CampusFenceDTO getCampusFenceConfig() {
        ConfigVO config = getConfigByKey(CAMPUS_FENCE_KEY);
        if (config == null || config.getConfigValue() == null) {
            return new CampusFenceDTO();
        }

        try {
            return objectMapper.readValue(config.getConfigValue(), CampusFenceDTO.class);
        } catch (JsonProcessingException e) {
            log.error("解析校园围栏配置失败", e);
            return new CampusFenceDTO();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCampusFenceConfig(CampusFenceDTO dto, Long adminId) {
        try {
            String jsonValue = objectMapper.writeValueAsString(dto);
            updateConfig(CAMPUS_FENCE_KEY, jsonValue, adminId);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("校园围栏配置格式错误", e);
        }
    }

    @Override
    public SeckillConfigDTO getSeckillConfig() {
        ConfigVO config = getConfigByKey(SECKILL_CONFIG_KEY);
        if (config == null || config.getConfigValue() == null) {
            return new SeckillConfigDTO();
        }

        try {
            return objectMapper.readValue(config.getConfigValue(), SeckillConfigDTO.class);
        } catch (JsonProcessingException e) {
            log.error("解析秒杀配置失败", e);
            return new SeckillConfigDTO();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillConfig(SeckillConfigDTO dto, Long adminId) {
        try {
            String jsonValue = objectMapper.writeValueAsString(dto);
            updateConfig(SECKILL_CONFIG_KEY, jsonValue, adminId);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("秒杀配置格式错误", e);
        }
    }

    @Override
    public void refreshCache() {
        // 清除所有配置缓存
        java.util.Set<String> keys = redisTemplate.keys(CONFIG_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("刷新配置缓存完成");
    }

    @Override
    public PageResult<ConfigVO> getConfigHistory(String configKey, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SystemConfigHistory> wrapper = new LambdaQueryWrapper<>();
        if (configKey != null && !configKey.isEmpty()) {
            wrapper.eq(SystemConfigHistory::getConfigKey, configKey);
        }
        wrapper.orderByDesc(SystemConfigHistory::getOperateTime);

        Page<SystemConfigHistory> page = new Page<>(pageNum, pageSize);
        Page<SystemConfigHistory> result = systemConfigHistoryMapper.selectPage(page, wrapper);

        List<ConfigVO> voList = result.getRecords().stream()
                .map(this::convertHistoryToVO)
                .toList();

        return new PageResult<>(pageNum, pageSize, result.getTotal(), voList);
    }

    private ConfigVO convertToVO(SystemConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    private ConfigVO convertHistoryToVO(SystemConfigHistory history) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(history, vo, "configValue", "updateTime");
        // 字段名不一致的手动映射
        vo.setConfigValue(history.getNewValue());
        vo.setUpdateTime(history.getOperateTime());
        return vo;
    }
}