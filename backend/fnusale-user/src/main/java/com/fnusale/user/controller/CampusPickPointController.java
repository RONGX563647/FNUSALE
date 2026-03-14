package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校园自提点控制器
 */
@Tag(name = "校园自提点管理", description = "校园自提点的增删改查接口")
@RestController
@RequestMapping("/pick-point")
public class CampusPickPointController {

    @Operation(summary = "获取自提点列表", description = "获取所有启用的校园自提点列表")
    @GetMapping("/list")
    public Result<List<Object>> getList() {
        // TODO: 实现获取自提点列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取附近自提点", description = "根据定位获取附近的校园自提点")
    @GetMapping("/nearby")
    public Result<List<Object>> getNearby(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "距离范围(米)") @RequestParam(defaultValue = "1000") Integer distance) {
        // TODO: 实现获取附近自提点逻辑
        return Result.success();
    }

    @Operation(summary = "获取自提点详情", description = "根据ID获取自提点详细信息")
    @GetMapping("/{id}")
    public Result<Object> getById(
            @Parameter(description = "自提点ID") @PathVariable Long id) {
        // TODO: 实现获取自提点详情逻辑
        return Result.success();
    }

    @Operation(summary = "新增自提点", description = "添加新的校园自提点（管理员）")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CampusPickPointDTO dto) {
        // TODO: 实现新增自提点逻辑
        return Result.success();
    }

    @Operation(summary = "更新自提点", description = "更新自提点信息（管理员）")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "自提点ID") @PathVariable Long id,
            @Valid @RequestBody CampusPickPointDTO dto) {
        // TODO: 实现更新自提点逻辑
        return Result.success();
    }

    @Operation(summary = "删除自提点", description = "删除自提点（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "自提点ID") @PathVariable Long id) {
        // TODO: 实现删除自提点逻辑
        return Result.success();
    }

    @Operation(summary = "启用/禁用自提点", description = "切换自提点启用状态（管理员）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "自提点ID") @PathVariable Long id,
            @Parameter(description = "启用状态(0-禁用,1-启用)") @RequestParam Integer status) {
        // TODO: 实现启用/禁用自提点逻辑
        return Result.success();
    }

    @Operation(summary = "分页查询自提点", description = "分页查询自提点列表（管理员）")
    @GetMapping("/page")
    public Result<PageResult<Object>> getPage(
            @Parameter(description = "校区") @RequestParam(required = false) String campusArea,
            @Parameter(description = "启用状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现分页查询自提点逻辑
        return Result.success();
    }
}