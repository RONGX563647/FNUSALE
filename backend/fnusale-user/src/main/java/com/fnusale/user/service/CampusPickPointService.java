package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import com.fnusale.common.vo.user.CampusPickPointVO;

import java.util.List;

/**
 * 校园自提点服务接口
 */
public interface CampusPickPointService {

    /**
     * 获取自提点列表
     */
    List<CampusPickPointVO> getList();

    /**
     * 获取附近自提点（支持IP定位）
     *
     * @param longitude 经度（可选）
     * @param latitude  纬度（可选）
     * @param distance  距离范围（米）
     * @param ip        用户IP地址
     * @return 按距离排序的自提点列表
     */
    List<CampusPickPointVO> getNearby(String longitude, String latitude, Integer distance, String ip);

    /**
     * 获取自提点详情
     */
    CampusPickPointVO getById(Long id);

    /**
     * 新增自提点
     */
    void add(CampusPickPointDTO dto);

    /**
     * 更新自提点
     */
    void update(Long id, CampusPickPointDTO dto);

    /**
     * 删除自提点
     */
    void delete(Long id);

    /**
     * 更新启用状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 分页查询
     */
    PageResult<CampusPickPointVO> getPage(String campusArea, Integer status, Integer pageNum, Integer pageSize);
}
