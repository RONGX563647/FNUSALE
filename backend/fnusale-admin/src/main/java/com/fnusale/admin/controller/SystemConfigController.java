package com.fnusale.admin.controller;

import com.fnusale.admin.service.SystemConfigService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.admin.CampusFenceDTO;
import com.fnusale.common.dto.admin.ConfigUpdateDTO;
import com.fnusale.common.dto.admin.SeckillConfigDTO;
import com.fnusale.common.vo.admin.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置管理", description = "系统配置、参数管理等接口（管理员）")
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取配置列表", description = "获取所有系统配置列表")
    @GetMapping("/list")
    public Result<List<ConfigVO>> getConfigList() {
        return Result.success(systemConfigService.getConfigList());
    }

    @Operation(summary = "获取配置详情", description = "根据配置键获取配置值")
    @GetMapping("/{configKey}")
    public Result<ConfigVO> getConfigByKey(
            @Parameter(description = "配置键") @PathVariable String configKey) {
        return Result.success(systemConfigService.getConfigByKey(configKey));
    }

    @Operation(summary = "更新配置", description = "更新系统配置")
    @PutMapping("/{configKey}")
    public Result<Void> updateConfig(
            @Parameter(description = "配置键") @PathVariable String configKey,
            @Parameter(description = "配置值") @RequestParam String configValue) {
        Long adminId = 1L;
        systemConfigService.updateConfig(configKey, configValue, adminId);
        return Result.success();
    }

    @Operation(summary = "批量更新配置", description = "批量更新系统配置")
    @PutMapping("/batch")
    public Result<Void> batchUpdateConfig(@Valid @RequestBody List<ConfigUpdateDTO> configs) {
        Long adminId = 1L;
        systemConfigService.batchUpdateConfig(configs, adminId);
        return Result.success();
    }

    @Operation(summary = "获取校园围栏配置", description = "获取校园围栏经纬度配置")
    @GetMapping("/campus-fence")
    public Result<CampusFenceDTO> getCampusFenceConfig() {
        return Result.success(systemConfigService.getCampusFenceConfig());
    }

    @Operation(summary = "更新校园围栏配置", description = "更新校园围栏经纬度配置")
    @PutMapping("/campus-fence")
    public Result<Void> updateCampusFenceConfig(@RequestBody CampusFenceDTO dto) {
        Long adminId = 1L;
        systemConfigService.updateCampusFenceConfig(dto, adminId);
        return Result.success();
    }

    @Operation(summary = "获取秒杀配置", description = "获取秒杀相关配置")
    @GetMapping("/seckill")
    public Result<SeckillConfigDTO> getSeckillConfig() {
        return Result.success(systemConfigService.getSeckillConfig());
    }

    @Operation(summary = "更新秒杀配置", description = "更新秒杀相关配置")
    @PutMapping("/seckill")
    public Result<Void> updateSeckillConfig(@RequestBody SeckillConfigDTO dto) {
        Long adminId = 1L;
        systemConfigService.updateSeckillConfig(dto, adminId);
        return Result.success();
    }

    @Operation(summary = "刷新缓存", description = "刷新系统配置缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        systemConfigService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "获取配置修改历史", description = "获取配置项的修改历史记录")
    @GetMapping("/history")
    public Result<PageResult<ConfigVO>> getConfigHistory(
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(systemConfigService.getConfigHistory(configKey, pageNum, pageSize));
    }
}