package com.fnusale.im.service;

import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.im.dto.ImageMessageDTO;
import com.fnusale.im.dto.TextMessageDTO;
import com.fnusale.im.dto.VoiceMessageDTO;

/**
 * 消息服务接口
 */
public interface ImMessageService {

    /**
     * 发送文字消息
     */
    MessageVO sendTextMessage(TextMessageDTO dto);

    /**
     * 发送图片消息
     */
    MessageVO sendImageMessage(ImageMessageDTO dto);

    /**
     * 发送语音消息
     */
    MessageVO sendVoiceMessage(VoiceMessageDTO dto);

    /**
     * 撤回消息
     */
    void recallMessage(Long messageId);
}