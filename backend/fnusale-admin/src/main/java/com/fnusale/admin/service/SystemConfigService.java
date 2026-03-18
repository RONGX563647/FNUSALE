package com.fnusale.admin.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.CampusFenceDTO;
import com.fnusale.common.dto.admin.ConfigUpdateDTO;
import com.fnusale.common.dto.admin.SeckillConfigDTO;
import com.fnusale.common.vo.admin.ConfigVO;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 获取配置列表
     */
    List<ConfigVO> getConfigList();

    /**
     * 获取配置详情
     */
    ConfigVO getConfigByKey(String configKey);

    /**
     * 更新配置
     */
    void updateConfig(String configKey, String configValue, Long adminId);

    /**
     * 批量更新配置
     */
    void batchUpdateConfig(List<ConfigUpdateDTO> configs, Long adminId);

    /**
     * 获取校园围栏配置
     */
    CampusFenceDTO getCampusFenceConfig();

    /**
     * 更新校园围栏配置
     */
    void updateCampusFenceConfig(CampusFenceDTO dto, Long adminId);

    /**
     * 获取秒杀配置
     */
    SeckillConfigDTO getSeckillConfig();

    /**
     * 更新秒杀配置
     */
    void updateSeckillConfig(SeckillConfigDTO dto, Long adminId);

    /**
     * 刷新缓存
     */
    void refreshCache();

    /**
     * 获取配置修改历史
     */
    PageResult<ConfigVO> getConfigHistory(String configKey, Integer pageNum, Integer pageSize);
}