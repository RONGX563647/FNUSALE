package com.fnusale.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.entity.ImMessage;
import com.fnusale.common.entity.ImSession;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.common.vo.im.SessionVO;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.im.client.ProductClient;
import com.fnusale.im.client.UserClient;
import com.fnusale.im.dto.SessionCreateDTO;
import com.fnusale.im.mapper.ImMessageMapper;
import com.fnusale.im.mapper.ImSessionMapper;
import com.fnusale.im.service.ImSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImSessionServiceImpl implements ImSessionService {

    private final ImSessionMapper sessionMapper;
    private final ImMessageMapper messageMapper;
    private final UserClient userClient;
    private final ProductClient productClient;

    private static final int MAX_PINNED_SESSIONS = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(SessionCreateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();
        Long targetUserId = dto.getTargetUserId();
        Long productId = dto.getProductId();

        // 校验不能与自己创建会话
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能与自己创建会话");
        }

        // 校验用户认证状态
        validateUserAuth(userId);
        validateUserAuth(targetUserId);

        // 校验商品存在性
        ProductVO product = validateProduct(productId, targetUserId);

        // 检查是否已存在会话
        ImSession existSession = sessionMapper.selectByUsersAndProduct(userId, targetUserId, productId);
        if (existSession != null) {
            return existSession.getId();
        }

        // 创建新会话
        ImSession session = new ImSession();
        session.setUser1Id(userId);
        session.setUser2Id(targetUserId);
        session.setProductId(productId);
        session.setUnreadCountU1(0);
        session.setUnreadCountU2(0);
        session.setSessionStatus("NORMAL");
        session.setIsPinnedU1(0);
        session.setIsPinnedU2(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        sessionMapper.insert(session);
        log.info("创建会话成功，sessionId: {}, user1: {}, user2: {}, product: {}",
                session.getId(), userId, targetUserId, productId);

        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getOrCreateSession(SessionCreateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();
        Long targetUserId = dto.getTargetUserId();
        Long productId = dto.getProductId();

        // 检查是否已存在会话
        ImSession existSession = sessionMapper.selectByUsersAndProduct(userId, targetUserId, productId);
        if (existSession != null) {
            return existSession.getId();
        }

        // 不存在则创建
        return createSession(dto);
    }

    @Override
    public List<SessionVO> getSessionList() {
        Long userId = UserContext.getUserIdOrThrow();
        List<ImSession> sessions = sessionMapper.selectByUserId(userId);

        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有需要查询的用户ID和商品ID
        Set<Long> userIds = sessions.stream()
                .flatMap(s -> List.of(s.getUser1Id(), s.getUser2Id()).stream())
                .collect(Collectors.toSet());
        Set<Long> productIds = sessions.stream()
                .map(ImSession::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量获取用户信息（使用批量接口优化N+1问题）
        Result<Map<Long, UserVO>> userMapResult = userClient.getUsersByIds(new ArrayList<>(userIds));
        final Map<Long, UserVO> finalUserMap = (userMapResult != null && userMapResult.getData() != null)
                ? userMapResult.getData() : Map.of();

        // 批量获取商品信息（使用批量接口优化N+1问题）
        final Map<Long, ProductVO> finalProductMap;
        if (!productIds.isEmpty()) {
            Result<Map<Long, ProductVO>> productMapResult = productClient.getProductsByIds(new ArrayList<>(productIds));
            finalProductMap = (productMapResult != null && productMapResult.getData() != null)
                    ? productMapResult.getData() : Map.of();
        } else {
            finalProductMap = Map.of();
        }

        // 转换为VO
        return sessions.stream()
                .map(session -> buildSessionVO(session, userId, finalUserMap, finalProductMap))
                .collect(Collectors.toList());
    }

    @Override
    public SessionVO getSessionById(Long sessionId) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 验证用户是否是会话参与者
        validateSessionParticipant(session, userId);

        // 获取用户信息
        UserVO targetUser = getTargetUser(session, userId);

        // 获取商品信息
        ProductVO product = null;
        if (session.getProductId() != null) {
            Result<ProductVO> result = productClient.getProductById(session.getProductId());
            product = result.getData();
        }

        return buildSessionVO(session, userId, targetUser, product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        validateSessionParticipant(session, userId);

        // 软删除：仅标记当前用户删除，不影响对方用户
        if (userId.equals(session.getUser1Id())) {
            sessionMapper.softDeleteU1(sessionId);
        } else {
            sessionMapper.softDeleteU2(sessionId);
        }
        log.info("用户删除会话，userId: {}, sessionId: {}", userId, sessionId);
    }

    @Override
    public Integer getUnreadCount() {
        Long userId = UserContext.getUserIdOrThrow();
        return messageMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long sessionId) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        validateSessionParticipant(session, userId);

        // 标记消息已读
        messageMapper.markAsReadBySession(sessionId, userId);

        // 原子更新：重置未读数
        if (userId.equals(session.getUser1Id())) {
            sessionMapper.resetUnreadU1(sessionId);
        } else {
            sessionMapper.resetUnreadU2(sessionId);
        }

        log.info("标记会话已读，userId: {}, sessionId: {}", userId, sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinSession(Long sessionId) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        validateSessionParticipant(session, userId);

        // 检查置顶数量限制
        int pinnedCount = sessionMapper.countPinnedByUserId(userId);
        if (pinnedCount >= MAX_PINNED_SESSIONS) {
            throw new BusinessException("最多只能置顶" + MAX_PINNED_SESSIONS + "个会话");
        }

        // 更新置顶状态
        LocalDateTime now = LocalDateTime.now();
        if (userId.equals(session.getUser1Id())) {
            session.setIsPinnedU1(1);
            session.setPinnedTimeU1(now);
        } else {
            session.setIsPinnedU2(1);
            session.setPinnedTimeU2(now);
        }
        session.setUpdateTime(now);
        sessionMapper.updateById(session);

        log.info("置顶会话，userId: {}, sessionId: {}", userId, sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpinSession(Long sessionId) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        validateSessionParticipant(session, userId);

        // 取消置顶
        if (userId.equals(session.getUser1Id())) {
            session.setIsPinnedU1(0);
            session.setPinnedTimeU1(null);
        } else {
            session.setIsPinnedU2(0);
            session.setPinnedTimeU2(null);
        }
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        log.info("取消置顶会话，userId: {}, sessionId: {}", userId, sessionId);
    }

    @Override
    public PageResult<MessageVO> getMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        ImSession session = sessionMapper.selectById(sessionId);

        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        validateSessionParticipant(session, userId);

        // 分页查询消息
        Page<ImMessage> page = new Page<>(pageNum, pageSize);
        IPage<ImMessage> messagePage = messageMapper.selectBySessionId(page, sessionId);

        List<MessageVO> messageVOList = messagePage.getRecords().stream()
                .map(this::buildMessageVO)
                .collect(Collectors.toList());

        return new PageResult<>(pageNum, pageSize, messagePage.getTotal(), messageVOList);
    }

    // ==================== 私有方法 ====================

    private void validateUserAuth(Long userId) {
        Result<String> result = userClient.getAuthStatus(userId);
        if (result == null || result.getData() == null) {
            throw new BusinessException("用户信息获取失败");
        }
        String authStatus = result.getData();
        if (!AuthStatus.AUTH_SUCCESS.getCode().equals(authStatus)) {
            throw new BusinessException("用户未完成校园认证，无法发起会话");
        }
    }

    private ProductVO validateProduct(Long productId, Long sellerId) {
        Result<ProductVO> result = productClient.getProductById(productId);
        if (result == null || result.getData() == null) {
            throw new BusinessException("商品不存在");
        }
        ProductVO product = result.getData();
        if (!product.getUserId().equals(sellerId)) {
            throw new BusinessException("商品发布者信息不匹配");
        }
        return product;
    }

    private void validateSessionParticipant(ImSession session, Long userId) {
        if (!userId.equals(session.getUser1Id()) && !userId.equals(session.getUser2Id())) {
            throw new BusinessException("您不是该会话的参与者");
        }
    }

    private UserVO getTargetUser(ImSession session, Long currentUserId) {
        Long targetUserId = currentUserId.equals(session.getUser1Id())
                ? session.getUser2Id()
                : session.getUser1Id();
        Result<UserVO> result = userClient.getUserById(targetUserId);
        if (result == null || result.getData() == null) {
            throw new BusinessException("用户信息获取失败");
        }
        return result.getData();
    }

    private SessionVO buildSessionVO(ImSession session, Long currentUserId,
                                      Map<Long, UserVO> userMap, Map<Long, ProductVO> productMap) {
        Long targetUserId = currentUserId.equals(session.getUser1Id())
                ? session.getUser2Id()
                : session.getUser1Id();

        UserVO targetUser = userMap.get(targetUserId);
        ProductVO product = session.getProductId() != null ? productMap.get(session.getProductId()) : null;

        return buildSessionVO(session, currentUserId, targetUser, product);
    }

    private SessionVO buildSessionVO(ImSession session, Long currentUserId, UserVO targetUser, ProductVO product) {
        SessionVO vo = new SessionVO();
        vo.setSessionId(session.getId());
        vo.setTargetUserId(targetUser != null ? targetUser.getId() : null);
        vo.setTargetUsername(targetUser != null ? targetUser.getUsername() : null);
        vo.setTargetAvatarUrl(targetUser != null ? targetUser.getAvatarUrl() : null);

        if (product != null) {
            vo.setProductId(product.getId());
            vo.setProductName(product.getProductName());
            vo.setProductMainImage(product.getMainImageUrl());
            vo.setProductPrice(product.getPrice());
        }

        vo.setLastMessageContent(session.getLastMessageContent());
        vo.setLastMessageTime(session.getLastMessageTime());
        vo.setSessionStatus(session.getSessionStatus());
        vo.setCreateTime(session.getCreateTime());

        // 未读数
        if (currentUserId.equals(session.getUser1Id())) {
            vo.setUnreadCount(session.getUnreadCountU1() != null ? session.getUnreadCountU1() : 0);
            vo.setIsPinned(session.getIsPinnedU1() != null && session.getIsPinnedU1() == 1);
        } else {
            vo.setUnreadCount(session.getUnreadCountU2() != null ? session.getUnreadCountU2() : 0);
            vo.setIsPinned(session.getIsPinnedU2() != null && session.getIsPinnedU2() == 1);
        }

        return vo;
    }

    private MessageVO buildMessageVO(ImMessage message) {
        MessageVO vo = new MessageVO();
        vo.setMessageId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getMessageContent());
        vo.setSendTime(message.getSendTime());
        vo.setIsRead(message.getIsRead() != null && message.getIsRead() == 1);
        vo.setIsRecalled(message.getIsDeleted() != null && message.getIsDeleted() == 1);
        return vo;
    }
}