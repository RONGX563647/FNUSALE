package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.SystemLogMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.SystemLog;
import com.fnusale.common.entity.User;
import com.fnusale.common.vo.admin.SystemLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogMapper systemLogMapper;
    private final UserMapper userMapper;

    @Override
    public void log(Long userId, String module, String type, String content, String ip, String device) {
        SystemLog systemLog = new SystemLog();
        systemLog.setOperateUserId(userId);
        systemLog.setModuleName(module);
        systemLog.setOperateType(type);
        systemLog.setOperateContent(content);
        systemLog.setIpAddress(ip);
        systemLog.setDeviceInfo(device);
        systemLog.setLogType("OPERATE");
        systemLog.setCreateTime(LocalDateTime.now());

        systemLogMapper.insert(systemLog);
    }

    @Override
    public PageResult<SystemLogVO> getLogPage(String module, String type, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(SystemLog::getModuleName, module);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(SystemLog::getOperateType, type);
        }
        wrapper.orderByDesc(SystemLog::getCreateTime);

        Page<SystemLog> page = new Page<>(pageNum, pageSize);
        Page<SystemLog> result = systemLogMapper.selectPage(page, wrapper);

        java.util.List<SystemLogVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return new PageResult<>(pageNum, pageSize, result.getTotal(), voList);
    }

    @Override
    public String exportLogs(String startDate, String endDate) {
        // TODO: 实现Excel导出
        log.info("导出日志, startDate: {}, endDate: {}", startDate, endDate);
        return null;
    }

    private SystemLogVO convertToVO(SystemLog systemLog) {
        SystemLogVO vo = new SystemLogVO();
        vo.setLogId(systemLog.getId());
        vo.setOperateUserId(systemLog.getOperateUserId());
        vo.setModuleName(systemLog.getModuleName());
        vo.setOperateType(systemLog.getOperateType());
        vo.setOperateContent(systemLog.getOperateContent());
        vo.setIpAddress(systemLog.getIpAddress());
        vo.setDeviceInfo(systemLog.getDeviceInfo());
        vo.setExceptionInfo(systemLog.getExceptionInfo());
        vo.setLogType(systemLog.getLogType());
        vo.setCreateTime(systemLog.getCreateTime());

        // 获取操作用户名
        if (systemLog.getOperateUserId() != null) {
            User user = userMapper.selectById(systemLog.getOperateUserId());
            if (user != null) {
                vo.setOperateUsername(user.getUsername());
            }
        }

        return vo;
    }
}