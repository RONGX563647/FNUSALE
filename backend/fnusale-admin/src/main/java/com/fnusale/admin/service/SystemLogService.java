package com.fnusale.admin.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.admin.SystemLogVO;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {

    /**
     * 记录操作日志
     */
    void log(Long userId, String module, String type, String content, String ip, String device);

    /**
     * 分页查询日志
     */
    PageResult<SystemLogVO> getLogPage(String module, String type, Integer pageNum, Integer pageSize);

    /**
     * 导出日志
     */
    String exportLogs(String startDate, String endDate);
}