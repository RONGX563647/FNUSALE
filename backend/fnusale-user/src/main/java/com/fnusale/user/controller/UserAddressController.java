package com.fnusale.user.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.dto.user.UserAddressDTO;
import com.fnusale.common.vo.user.UserAddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 * 提供用户地址的增删改查接口
 */
@Tag(name = "用户地址管理", description = "用户地址的增删改查接口，支持自提点和自定义地址")
@RestController
@RequestMapping("/address")
public class UserAddressController {

    @Operation(summary = "获取我的地址列表", description = "获取当前用户的所有地址，每个用户最多10个地址")
    @GetMapping("/list")
    public Result<List<UserAddressVO>> getList() {
        // TODO: 实现获取地址列表逻辑
        return Result.success();
    }

    @Operation(summary = "获取地址详情", description = "根据ID获取地址详细信息")
    @GetMapping("/{id}")
    public Result<UserAddressVO> getById(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id) {
        // TODO: 实现获取地址详情逻辑
        return Result.success();
    }

    @Operation(summary = "新增地址", description = "添加新地址，可关联自提点或自定义地址")
    @PostMapping
    public Result<Void> add(
            @Parameter(description = "地址请求", required = true) @Valid @RequestBody UserAddressDTO dto) {
        // TODO: 实现新增地址逻辑
        return Result.success();
    }

    @Operation(summary = "更新地址", description = "更新地址信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id,
            @Parameter(description = "地址请求", required = true) @Valid @RequestBody UserAddressDTO dto) {
        // TODO: 实现更新地址逻辑
        return Result.success();
    }

    @Operation(summary = "删除地址", description = "删除指定地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id) {
        // TODO: 实现删除地址逻辑
        return Result.success();
    }

    @Operation(summary = "设置默认地址", description = "设置指定地址为默认地址，自动取消原默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id) {
        // TODO: 实现设置默认地址逻辑
        return Result.success();
    }

    @Operation(summary = "获取默认地址", description = "获取当前用户的默认地址")
    @GetMapping("/default")
    public Result<UserAddressVO> getDefault() {
        // TODO: 实现获取默认地址逻辑
        return Result.success();
    }
}