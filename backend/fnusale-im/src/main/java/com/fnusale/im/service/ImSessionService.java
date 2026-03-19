package com.fnusale.im.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.common.vo.im.SessionVO;
import com.fnusale.im.dto.SessionCreateDTO;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ImSessionService {

    /**
     * 创建会话
     */
    Long createSession(SessionCreateDTO dto);

    /**
     * 获取或创建会话
     */
    Long getOrCreateSession(SessionCreateDTO dto);

    /**
     * 获取会话列表
     */
    List<SessionVO> getSessionList();

    /**
     * 获取会话详情
     */
    SessionVO getSessionById(Long sessionId);

    /**
     * 删除会话
     */
    void deleteSession(Long sessionId);

    /**
     * 获取未读消息数
     */
    Integer getUnreadCount();

    /**
     * 标记会话已读
     */
    void markAsRead(Long sessionId);

    /**
     * 置顶会话
     */
    void pinSession(Long sessionId);

    /**
     * 取消置顶
     */
    void unpinSession(Long sessionId);

    /**
     * 获取会话消息列表
     */
    PageResult<MessageVO> getMessages(Long sessionId, Integer pageNum, Integer pageSize);
}