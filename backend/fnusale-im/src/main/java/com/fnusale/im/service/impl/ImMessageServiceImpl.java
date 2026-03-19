package com.fnusale.im.service.impl;

import com.fnusale.common.entity.ImMessage;
import com.fnusale.common.entity.ImSession;
import com.fnusale.common.enums.MessageType;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.im.dto.ImageMessageDTO;
import com.fnusale.im.dto.TextMessageDTO;
import com.fnusale.im.dto.VoiceMessageDTO;
import com.fnusale.im.mapper.ImMessageMapper;
import com.fnusale.im.mapper.ImSessionMapper;
import com.fnusale.im.service.ImMessageService;
import com.fnusale.im.websocket.ImWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImMessageServiceImpl implements ImMessageService {

    private final ImMessageMapper messageMapper;
    private final ImSessionMapper sessionMapper;
    private final ImWebSocketHandler webSocketHandler;

    private static final int RECALL_TIMEOUT_SECONDS = 120;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendTextMessage(TextMessageDTO dto) {
        return sendMessage(dto.getSessionId(), MessageType.TEXT.getCode(), dto.getContent(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendImageMessage(ImageMessageDTO dto) {
        return sendMessage(dto.getSessionId(), MessageType.IMAGE.getCode(), dto.getImageUrl(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendVoiceMessage(VoiceMessageDTO dto) {
        // 将时长信息拼接到内容中，格式: voiceUrl|duration
        String content = dto.getVoiceUrl() + "|" + dto.getDuration();
        return sendMessage(dto.getSessionId(), MessageType.VOICE.getCode(), content, dto.getDuration());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId) {
        Long userId = UserContext.getUserIdOrThrow();

        // 检查消息是否存在
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查是否是发送者
        if (!userId.equals(message.getSenderId())) {
            throw new BusinessException("只能撤回自己发送的消息");
        }

        // 检查是否已撤回
        if (message.getIsDeleted() != null && message.getIsDeleted() == 1) {
            throw new BusinessException("消息已撤回");
        }

        // 检查撤回时限
        LocalDateTime sendTime = message.getSendTime();
        if (sendTime != null) {
            long seconds = ChronoUnit.SECONDS.between(sendTime, LocalDateTime.now());
            if (seconds > RECALL_TIMEOUT_SECONDS) {
                throw new BusinessException("消息已超过撤回时限");
            }
        }

        // 执行撤回
        int rows = messageMapper.logicDeleteByIdAndSender(messageId, userId);
        if (rows == 0) {
            throw new BusinessException("撤回失败");
        }

        log.info("消息撤回成功，userId: {}, messageId: {}", userId, messageId);
    }

    private MessageVO sendMessage(Long sessionId, String messageType, String content, Integer duration) {
        Long userId = UserContext.getUserIdOrThrow();

        // 检查会话是否存在
        ImSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 检查是否是会话参与者
        if (!userId.equals(session.getUser1Id()) && !userId.equals(session.getUser2Id())) {
            throw new BusinessException("您不是该会话的参与者");
        }

        // 检查会话状态
        if ("CLOSED".equals(session.getSessionStatus())) {
            throw new BusinessException("会话已关闭");
        }

        // 确定接收者
        Long receiverId = userId.equals(session.getUser1Id()) ? session.getUser2Id() : session.getUser1Id();

        // 创建消息
        ImMessage message = new ImMessage();
        message.setSessionId(sessionId);
        message.setSenderId(userId);
        message.setReceiverId(receiverId);
        message.setMessageType(messageType);
        message.setMessageContent(content);
        message.setIsRead(0);
        message.setSensitiveCheckResult("PASS");
        message.setSendTime(LocalDateTime.now());
        message.setIsDeleted(0);

        messageMapper.insert(message);

        // 恢复会话（如果用户删除了会话，收到新消息时恢复）
        // 发送者恢复（确保发送者能看到自己发的消息）
        if (userId.equals(session.getUser1Id())) {
            sessionMapper.restoreU1(sessionId);
        } else {
            sessionMapper.restoreU2(sessionId);
        }

        // 原子更新：更新会话最后消息和未读数
        String lastMessageContent = buildLastMessageContent(messageType, content);
        if (userId.equals(session.getUser1Id())) {
            // 接收者恢复会话
            sessionMapper.restoreU2(sessionId);
            sessionMapper.incrementUnreadU2(sessionId, lastMessageContent, message.getSendTime());
        } else {
            // 接收者恢复会话
            sessionMapper.restoreU1(sessionId);
            sessionMapper.incrementUnreadU1(sessionId, lastMessageContent, message.getSendTime());
        }

        log.info("发送消息成功，messageId: {}, sessionId: {}, senderId: {}", message.getId(), sessionId, userId);

        // 构建消息VO并通过WebSocket推送给接收者
        MessageVO messageVO = buildMessageVO(message, duration);
        pushMessageToReceiver(receiverId, messageVO);

        return messageVO;
    }

    /**
     * 通过WebSocket推送消息给接收者
     */
    private void pushMessageToReceiver(Long receiverId, MessageVO messageVO) {
        try {
            java.util.Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("type", "message");
            pushData.put("data", messageVO);
            webSocketHandler.sendMessageToUser(receiverId, pushData);
            log.debug("消息推送成功，receiverId: {}, messageId: {}", receiverId, messageVO.getMessageId());
        } catch (Exception e) {
            // 推送失败不影响消息发送，仅记录日志
            log.warn("消息推送失败，receiverId: {}, messageId: {}", receiverId, messageVO.getMessageId(), e);
        }
    }

    private String buildLastMessageContent(String messageType, String content) {
        switch (messageType) {
            case "IMAGE":
                return "[图片]";
            case "VOICE":
                return "[语音]";
            default:
                return content.length() > 50 ? content.substring(0, 50) + "..." : content;
        }
    }

    private MessageVO buildMessageVO(ImMessage message, Integer duration) {
        MessageVO vo = new MessageVO();
        vo.setMessageId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getMessageContent());
        vo.setDuration(duration);
        vo.setSendTime(message.getSendTime());
        vo.setIsRead(false);
        vo.setIsRecalled(false);
        return vo;
    }
}