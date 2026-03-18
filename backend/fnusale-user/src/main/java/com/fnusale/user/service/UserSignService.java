package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.SignDTO;
import com.fnusale.common.vo.user.SignRecordVO;
import com.fnusale.common.vo.user.SignResultVO;
import com.fnusale.common.vo.user.SignStatusVO;
import com.fnusale.common.vo.user.UserPointsVO;

import java.util.List;

/**
 * 签到服务接口
 */
public interface UserSignService {

    /**
     * 每日签到
     */
    SignResultVO sign(Long userId);

    /**
     * 查询签到状态
     */
    SignStatusVO getSignStatus(Long userId);

    /**
     * 签到统计
     */
    SignStatusVO getSignStatistics(Long userId);

    /**
     * 签到记录
     */
    PageResult<SignRecordVO> getSignRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 签到日历
     */
    List<String> getSignCalendar(Long userId, String month);

    /**
     * 补签
     */
    SignResultVO repairSign(Long userId, SignDTO dto);
}