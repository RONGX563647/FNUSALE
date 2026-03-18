package com.fnusale.admin.controller;

import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.vo.admin.SystemLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统日志控制器
 */
@Tag(name = "系统日志", description = "操作日志查询接口（管理员）")
@RestController
@RequestMapping("/admin/log")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    @Operation(summary = "获取日志列表", description = "分页获取操作日志")
    @GetMapping("/page")
    public Result<PageResult<SystemLogVO>> getLogPage(
            @Parameter(description = "操作模块") @RequestParam(required = false) String moduleName,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operateType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(systemLogService.getLogPage(moduleName, operateType, pageNum, pageSize));
    }

    @Operation(summary = "导出日志", description = "导出操作日志")
    @GetMapping("/export")
    public Result<String> exportLogs(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        String url = systemLogService.exportLogs(startDate, endDate);
        return Result.success(url);
    }
}