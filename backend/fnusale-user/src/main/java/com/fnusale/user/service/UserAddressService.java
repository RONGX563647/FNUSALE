package com.fnusale.user.service;

import com.fnusale.common.dto.user.UserAddressDTO;
import com.fnusale.common.vo.user.UserAddressVO;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService {

    /**
     * 获取用户地址列表
     */
    List<UserAddressVO> getList(Long userId);

    /**
     * 获取地址详情
     */
    UserAddressVO getById(Long userId, Long id);

    /**
     * 新增地址
     */
    void add(Long userId, UserAddressDTO dto);

    /**
     * 更新地址
     */
    void update(Long userId, Long id, UserAddressDTO dto);

    /**
     * 删除地址
     */
    void delete(Long userId, Long id);

    /**
     * 设置默认地址
     */
    void setDefault(Long userId, Long id);

    /**
     * 获取默认地址
     */
    UserAddressVO getDefault(Long userId);
}