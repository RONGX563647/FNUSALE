package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.AdminUserService;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.UserQueryDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.enums.LogModule;
import com.fnusale.common.enums.OperateType;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.UserDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final SystemLogService systemLogService;

    @Override
    public PageResult<UserDetailVO> getUserPage(UserQueryDTO query) {
        Page<UserDetailVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<UserDetailVO> result = userMapper.selectUserPage(page,
                query.getUsername(), query.getAuthStatus(), query.getIdentityType());
        return new PageResult<>(query.getPageNum(), query.getPageSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public PageResult<UserDetailVO> getPendingAuthList(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAuthStatus, AuthStatus.UNDER_REVIEW.getCode());
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, wrapper);

        java.util.List<UserDetailVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return new PageResult<>(pageNum, pageSize, result.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void authPass(Long userId, Long adminId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!AuthStatus.UNDER_REVIEW.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException("用户不在审核中");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAuthStatus(AuthStatus.AUTH_SUCCESS.getCode());
        userMapper.updateById(updateUser);

        systemLogService.log(adminId, LogModule.USER.getCode(), OperateType.UPDATE.getCode(),
                "认证通过用户ID:" + userId, null, null);

        log.info("用户认证通过, userId: {}, adminId: {}", userId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void authReject(Long userId, Long adminId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!AuthStatus.UNDER_REVIEW.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException("用户不在审核中");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAuthStatus(AuthStatus.AUTH_FAILED.getCode());
        updateUser.setAuthResultRemark(reason);
        userMapper.updateById(updateUser);

        systemLogService.log(adminId, LogModule.USER.getCode(), OperateType.UPDATE.getCode(),
                "认证驳回用户ID:" + userId + ", 原因:" + reason, null, null);

        log.info("用户认证驳回, userId: {}, adminId: {}, reason: {}", userId, adminId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId, Long adminId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 使用信誉分=0表示封禁状态
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setCreditScore(0);
        userMapper.updateById(updateUser);

        systemLogService.log(adminId, LogModule.USER.getCode(), OperateType.UPDATE.getCode(),
                "封禁用户ID:" + userId + ", 原因:" + reason, null, null);

        log.info("封禁用户, userId: {}, adminId: {}, reason: {}", userId, adminId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId, Long adminId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 恢复信誉分到60，允许正常使用
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setCreditScore(60);
        userMapper.updateById(updateUser);

        systemLogService.log(adminId, LogModule.USER.getCode(), OperateType.UPDATE.getCode(),
                "解封用户ID:" + userId, null, null);

        log.info("解封用户, userId: {}, adminId: {}", userId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer adjustCredit(Long userId, Integer score, String reason, Long adminId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        int newScore = Math.max(0, Math.min(100, user.getCreditScore() + score));
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setCreditScore(newScore);
        userMapper.updateById(updateUser);

        systemLogService.log(adminId, LogModule.USER.getCode(), OperateType.UPDATE.getCode(),
                "调整用户信誉分ID:" + userId + ", 变化:" + score + ", 原因:" + reason, null, null);

        log.info("调整用户信誉分, userId: {}, score: {}, newScore: {}", userId, score, newScore);
        return newScore;
    }

    private UserDetailVO convertToVO(User user) {
        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setIdentityType(user.getIdentityType());
        vo.setAuthStatus(user.getAuthStatus());
        vo.setAuthResultRemark(user.getAuthResultRemark());
        vo.setAuthImageUrl(user.getAuthImageUrl());
        vo.setCreditScore(user.getCreditScore());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBirthday(user.getBirthday());
        vo.setRegisterTime(user.getCreateTime());

        // 脱敏处理
        if (user.getPhone() != null) {
            vo.setPhone(maskPhone(user.getPhone()));
        }
        if (user.getStudentTeacherId() != null) {
            vo.setStudentTeacherId(maskId(user.getStudentTeacherId()));
        }
        vo.setCampusEmail(user.getCampusEmail());

        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskId(String id) {
        if (id == null || id.length() < 4) {
            return id;
        }
        return "****" + id.substring(id.length() - 4);
    }
}