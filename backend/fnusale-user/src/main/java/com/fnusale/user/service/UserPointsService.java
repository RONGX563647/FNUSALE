package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.user.UserPointsVO;

/**
 * 用户积分服务接口
 */
public interface UserPointsService {

    /**
     * 获取用户积分信息
     */
    UserPointsVO getUserPoints(Long userId);

    /**
     * 增加积分
     */
    void addPoints(Long userId, Integer points, String changeType, String remark);

    /**
     * 扣减积分
     */
    void deductPoints(Long userId, Integer points, String changeType, String remark);

    /**
     * 获取积分变动记录
     */
    PageResult<Object> getPointsLogs(Long userId, Integer pageNum, Integer pageSize);
}