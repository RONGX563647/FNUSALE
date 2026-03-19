package com.fnusale.im.service.impl;

import com.fnusale.common.entity.ImMessage;
import com.fnusale.common.entity.ImSession;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.im.dto.ImageMessageDTO;
import com.fnusale.im.dto.TextMessageDTO;
import com.fnusale.im.dto.VoiceMessageDTO;
import com.fnusale.im.mapper.ImMessageMapper;
import com.fnusale.im.mapper.ImSessionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImMessageServiceImplTest {

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private ImSessionMapper sessionMapper;

    @InjectMocks
    private ImMessageServiceImpl messageService;

    private ImSession testSession;
    private ImMessage testMessage;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        testSession = new ImSession();
        testSession.setId(1L);
        testSession.setUser1Id(1L);
        testSession.setUser2Id(2L);
        testSession.setProductId(100L);
        testSession.setUnreadCountU1(0);
        testSession.setUnreadCountU2(0);
        testSession.setSessionStatus("NORMAL");

        testMessage = new ImMessage();
        testMessage.setId(1L);
        testMessage.setSessionId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setMessageType("TEXT");
        testMessage.setMessageContent("测试消息");
        testMessage.setSendTime(LocalDateTime.now());
        testMessage.setIsRead(0);
        testMessage.setIsDeleted(0);
    }

    @AfterEach
    void tearDown() {
        UserContext.clearCurrentUserId();
    }

    @Nested
    @DisplayName("发送文字消息测试")
    class SendTextMessageTests {

        @Test
        @DisplayName("正常发送文字消息_成功")
        void sendTextMessage_success() {
            TextMessageDTO dto = new TextMessageDTO();
            dto.setSessionId(1L);
            dto.setContent("你好");

            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(messageMapper.insert(any(ImMessage.class))).thenAnswer(invocation -> {
                ImMessage msg = invocation.getArgument(0);
                msg.setId(1L);
                return 1;
            });
            when(sessionMapper.restoreU1(1L)).thenReturn(1);
            when(sessionMapper.restoreU2(1L)).thenReturn(1);
            when(sessionMapper.incrementUnreadU2(eq(1L), anyString(), any(LocalDateTime.class))).thenReturn(1);

            MessageVO result = messageService.sendTextMessage(dto);

            assertNotNull(result);
            assertEquals("TEXT", result.getMessageType());
            assertEquals("你好", result.getContent());
            verify(messageMapper).insert(any(ImMessage.class));
            verify(sessionMapper).restoreU1(1L);
            verify(sessionMapper).restoreU2(1L);
            verify(sessionMapper).incrementUnreadU2(eq(1L), anyString(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("会话不存在_抛出异常")
        void sendTextMessage_sessionNotFound_throwsException() {
            TextMessageDTO dto = new TextMessageDTO();
            dto.setSessionId(999L);
            dto.setContent("你好");

            when(sessionMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.sendTextMessage(dto));
            assertEquals("会话不存在", exception.getMessage());
        }

        @Test
        @DisplayName("非会话参与者_抛出异常")
        void sendTextMessage_notParticipant_throwsException() {
            UserContext.clearCurrentUserId();
            UserContext.setCurrentUserId(999L);

            TextMessageDTO dto = new TextMessageDTO();
            dto.setSessionId(1L);
            dto.setContent("你好");

            when(sessionMapper.selectById(1L)).thenReturn(testSession);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.sendTextMessage(dto));
            assertEquals("您不是该会话的参与者", exception.getMessage());
        }

        @Test
        @DisplayName("会话已关闭_抛出异常")
        void sendTextMessage_sessionClosed_throwsException() {
            testSession.setSessionStatus("CLOSED");

            TextMessageDTO dto = new TextMessageDTO();
            dto.setSessionId(1L);
            dto.setContent("你好");

            when(sessionMapper.selectById(1L)).thenReturn(testSession);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.sendTextMessage(dto));
            assertEquals("会话已关闭", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("发送图片消息测试")
    class SendImageMessageTests {

        @Test
        @DisplayName("正常发送图片消息_成功")
        void sendImageMessage_success() {
            ImageMessageDTO dto = new ImageMessageDTO();
            dto.setSessionId(1L);
            dto.setImageUrl("http://example.com/image.jpg");

            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(messageMapper.insert(any(ImMessage.class))).thenAnswer(invocation -> {
                ImMessage msg = invocation.getArgument(0);
                msg.setId(1L);
                return 1;
            });
            when(sessionMapper.restoreU1(1L)).thenReturn(1);
            when(sessionMapper.restoreU2(1L)).thenReturn(1);
            when(sessionMapper.incrementUnreadU2(eq(1L), anyString(), any(LocalDateTime.class))).thenReturn(1);

            MessageVO result = messageService.sendImageMessage(dto);

            assertNotNull(result);
            assertEquals("IMAGE", result.getMessageType());
            assertEquals("http://example.com/image.jpg", result.getContent());
        }
    }

    @Nested
    @DisplayName("发送语音消息测试")
    class SendVoiceMessageTests {

        @Test
        @DisplayName("正常发送语音消息_成功")
        void sendVoiceMessage_success() {
            VoiceMessageDTO dto = new VoiceMessageDTO();
            dto.setSessionId(1L);
            dto.setVoiceUrl("http://example.com/voice.amr");
            dto.setDuration(15);

            when(sessionMapper.selectById(1L)).thenReturn(testSession);
            when(messageMapper.insert(any(ImMessage.class))).thenAnswer(invocation -> {
                ImMessage msg = invocation.getArgument(0);
                msg.setId(1L);
                return 1;
            });
            when(sessionMapper.restoreU1(1L)).thenReturn(1);
            when(sessionMapper.restoreU2(1L)).thenReturn(1);
            when(sessionMapper.incrementUnreadU2(eq(1L), anyString(), any(LocalDateTime.class))).thenReturn(1);

            MessageVO result = messageService.sendVoiceMessage(dto);

            assertNotNull(result);
            assertEquals("VOICE", result.getMessageType());
            assertEquals(15, result.getDuration());
        }
    }

    @Nested
    @DisplayName("撤回消息测试")
    class RecallMessageTests {

        @Test
        @DisplayName("正常撤回消息_成功")
        void recallMessage_success() {
            testMessage.setSendTime(LocalDateTime.now().minusSeconds(30));

            when(messageMapper.selectById(1L)).thenReturn(testMessage);
            when(messageMapper.logicDeleteByIdAndSender(1L, 1L)).thenReturn(1);

            assertDoesNotThrow(() -> messageService.recallMessage(1L));
            verify(messageMapper).logicDeleteByIdAndSender(1L, 1L);
        }

        @Test
        @DisplayName("消息不存在_抛出异常")
        void recallMessage_notFound_throwsException() {
            when(messageMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.recallMessage(999L));
            assertEquals("消息不存在", exception.getMessage());
        }

        @Test
        @DisplayName("非消息发送者_抛出异常")
        void recallMessage_notSender_throwsException() {
            UserContext.clearCurrentUserId();
            UserContext.setCurrentUserId(999L);

            when(messageMapper.selectById(1L)).thenReturn(testMessage);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.recallMessage(1L));
            assertEquals("只能撤回自己发送的消息", exception.getMessage());
        }

        @Test
        @DisplayName("消息已撤回_抛出异常")
        void recallMessage_alreadyRecalled_throwsException() {
            testMessage.setIsDeleted(1);

            when(messageMapper.selectById(1L)).thenReturn(testMessage);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.recallMessage(1L));
            assertEquals("消息已撤回", exception.getMessage());
        }

        @Test
        @DisplayName("超过撤回时限_抛出异常")
        void recallMessage_timeout_throwsException() {
            testMessage.setSendTime(LocalDateTime.now().minusSeconds(180));

            when(messageMapper.selectById(1L)).thenReturn(testMessage);

            BusinessException exception = assertThrows(BusinessException.class, () -> messageService.recallMessage(1L));
            assertEquals("消息已超过撤回时限", exception.getMessage());
        }
    }
}