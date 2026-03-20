package com.fnusale.agent.service;

import com.fnusale.agent.dto.ChatRequest;
import com.fnusale.agent.dto.ChatResponse;

/**
 * Agent对话服务接口
 *
 * 提供对话式购物助手功能：
 * - 意图理解：分析用户消息，识别购买意图
 * - 多轮对话：维护对话上下文，提供连贯交互
 * - 商品筛选：根据用户需求推荐商品
 */
public interface AgentChatService {

    /**
     * 处理用户对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 清除会话上下文
     *
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);
}