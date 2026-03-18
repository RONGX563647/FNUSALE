package com.fnusale.user.service.impl;

import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.UserAddressDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.entity.UserAddress;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.UserAddressVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import com.fnusale.user.mapper.UserAddressMapper;
import com.fnusale.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户地址服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;
    private final CampusPickPointMapper campusPickPointMapper;

    @Override
    public List<UserAddressVO> getList(Long userId) {
        List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::buildUserAddressVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressVO getById(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        return buildUserAddressVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, UserAddressDTO dto) {
        // 检查地址数量限制
        int count = userAddressMapper.countByUserId(userId);
        if (count >= UserConstants.MAX_ADDRESS_COUNT) {
            throw new BusinessException("地址数量已达上限（最多" + UserConstants.MAX_ADDRESS_COUNT + "个）");
        }

        // 验证地址类型
        validateAddressDTO(dto);

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setAddressType(dto.getAddressType());
        address.setPickPointId(dto.getPickPointId());
        address.setCustomAddress(dto.getCustomAddress());

        // 设置经纬度
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            address.setLongitude(new BigDecimal(dto.getLongitude()));
            address.setLatitude(new BigDecimal(dto.getLatitude()));
        } else if ("PICK_POINT".equals(dto.getAddressType()) && dto.getPickPointId() != null) {
            // 从自提点获取经纬度
            CampusPickPoint pickPoint = campusPickPointMapper.selectById(dto.getPickPointId());
            if (pickPoint != null) {
                address.setLongitude(pickPoint.getLongitude());
                address.setLatitude(pickPoint.getLatitude());
            }
        }

        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认，先清除其他默认地址
        if (address.getIsDefault() == 1) {
            userAddressMapper.clearDefaultByUserId(userId);
        }

        userAddressMapper.insert(address);
        log.info("新增用户地址成功, userId: {}, addressId: {}", userId, address.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, UserAddressDTO dto) {
        UserAddress existAddress = userAddressMapper.selectById(id);
        if (existAddress == null || !existAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        // 验证地址类型
        validateAddressDTO(dto);

        UserAddress address = new UserAddress();
        address.setId(id);
        address.setAddressType(dto.getAddressType());
        address.setPickPointId(dto.getPickPointId());
        address.setCustomAddress(dto.getCustomAddress());

        // 设置经纬度
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            address.setLongitude(new BigDecimal(dto.getLongitude()));
            address.setLatitude(new BigDecimal(dto.getLatitude()));
        } else if ("PICK_POINT".equals(dto.getAddressType()) && dto.getPickPointId() != null) {
            CampusPickPoint pickPoint = campusPickPointMapper.selectById(dto.getPickPointId());
            if (pickPoint != null) {
                address.setLongitude(pickPoint.getLongitude());
                address.setLatitude(pickPoint.getLatitude());
            }
        }

        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : existAddress.getIsDefault());
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认，先清除其他默认地址
        if (address.getIsDefault() == 1) {
            userAddressMapper.clearDefaultByUserId(userId);
        }

        userAddressMapper.updateById(address);
        log.info("更新用户地址成功, userId: {}, addressId: {}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        userAddressMapper.deleteById(id);
        log.info("删除用户地址成功, userId: {}, addressId: {}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        // 清除其他默认地址
        userAddressMapper.clearDefaultByUserId(userId);

        // 设置当前地址为默认
        UserAddress updateAddress = UserAddress.builder()
                .id(id)
                .isDefault(1)
                .updateTime(LocalDateTime.now())
                .build();

        userAddressMapper.updateById(updateAddress);
        log.info("设置默认地址成功, userId: {}, addressId: {}", userId, id);
    }

    @Override
    public UserAddressVO getDefault(Long userId) {
        UserAddress address = userAddressMapper.selectDefaultByUserId(userId);
        if (address == null) {
            return null;
        }
        return buildUserAddressVO(address);
    }

    /**
     * 验证地址DTO
     */
    private void validateAddressDTO(UserAddressDTO dto) {
        if ("PICK_POINT".equals(dto.getAddressType())) {
            if (dto.getPickPointId() == null) {
                throw new BusinessException("自提点ID不能为空");
            }
            // 验证自提点是否存在
            CampusPickPoint pickPoint = campusPickPointMapper.selectById(dto.getPickPointId());
            if (pickPoint == null || pickPoint.getEnableStatus() != 1) {
                throw new BusinessException("自提点不存在或已禁用");
            }
        } else if ("CUSTOM".equals(dto.getAddressType())) {
            if (dto.getCustomAddress() == null || dto.getCustomAddress().isEmpty()) {
                throw new BusinessException("自定义地址不能为空");
            }
        } else {
            throw new BusinessException("地址类型不正确");
        }
    }

    /**
     * 构建地址VO
     */
    private UserAddressVO buildUserAddressVO(UserAddress address) {
        UserAddressVO.UserAddressVOBuilder builder = UserAddressVO.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .pickPointId(address.getPickPointId())
                .customAddress(address.getCustomAddress())
                .isDefault(address.getIsDefault() == 1)
                .longitude(address.getLongitude())
                .latitude(address.getLatitude());

        // 如果是自提点地址，填充自提点名称
        if ("PICK_POINT".equals(address.getAddressType()) && address.getPickPointId() != null) {
            CampusPickPoint pickPoint = campusPickPointMapper.selectById(address.getPickPointId());
            if (pickPoint != null) {
                builder.pickPointName(pickPoint.getPickPointName())
                        .campusArea(pickPoint.getCampusArea())
                        .detailAddress(pickPoint.getDetailAddress());
            }
        }

        return builder.build();
    }
}