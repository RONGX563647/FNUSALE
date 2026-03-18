package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM t_system_config WHERE config_key = #{configKey}")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);
}