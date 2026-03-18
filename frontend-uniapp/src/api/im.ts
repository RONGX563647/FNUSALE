import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type { ImSessionVO, ImMessageVO, QuickReplyVO } from '@/types/im'

// 聊天会话 API
export const sessionApi = {
  // 获取会话列表
  getList(): Promise<Result<ImSessionVO[]>> {
    return http.get('/session/list')
  },

  // 获取会话详情
  getById(sessionId: number): Promise<Result<ImSessionVO>> {
    return http.get(`/session/${sessionId}`)
  },

  // 创建会话
  create(targetUserId: number, productId: number): Promise<Result<number>> {
    return http.post('/session/create', { targetUserId, productId })
  },

  // 获取或创建会话
  getOrCreate(targetUserId: number, productId: number): Promise<Result<number>> {
    return http.get('/session/get-or-create', { targetUserId, productId })
  },

  // 删除会话
  delete(sessionId: number): Promise<Result<void>> {
    return http.delete(`/session/${sessionId}`)
  },

  // 获取未读消息数
  getUnreadCount(): Promise<Result<number>> {
    return http.get('/session/unread-count')
  },

  // 标记会话已读
  markAsRead(sessionId: number): Promise<Result<void>> {
    return http.put(`/session/${sessionId}/read`)
  },

  // 获取会话消息列表
  getMessages(sessionId: number, params: PageParams): Promise<Result<PageResult<ImMessageVO>>> {
    return http.get(`/session/${sessionId}/messages`, params)
  },

  // 置顶会话
  pin(sessionId: number): Promise<Result<void>> {
    return http.put(`/session/${sessionId}/pin`)
  },

  // 取消置顶
  unpin(sessionId: number): Promise<Result<void>> {
    return http.delete(`/session/${sessionId}/pin`)
  }
}

// 聊天消息 API
export const messageApi = {
  // 发送文字消息
  sendText(sessionId: number, content: string): Promise<Result<void>> {
    return http.post('/message/text', { sessionId, content })
  },

  // 发送图片消息
  sendImage(sessionId: number, imageUrl: string): Promise<Result<void>> {
    return http.post('/message/image', { sessionId, imageUrl })
  },

  // 发送语音消息
  sendVoice(sessionId: number, voiceUrl: string, duration: number): Promise<Result<void>> {
    return http.post('/message/voice', { sessionId, voiceUrl, duration })
  },

  // 撤回消息
  recall(messageId: number): Promise<Result<void>> {
    return http.delete(`/message/${messageId}`)
  },

  // 获取快捷回复列表
  getQuickReplyList(): Promise<Result<QuickReplyVO[]>> {
    return http.get('/message/quick-reply/list')
  },

  // 添加快捷回复
  addQuickReply(content: string): Promise<Result<void>> {
    return http.post('/message/quick-reply', { content })
  },

  // 删除快捷回复
  deleteQuickReply(id: number): Promise<Result<void>> {
    return http.delete(`/message/quick-reply/${id}`)
  },

  // 导出聊天记录
  exportHistory(sessionId: number): Promise<Result<string>> {
    return http.get(`/message/${sessionId}/export`)
  },

  // 搜索消息
  search(sessionId: number, keyword: string): Promise<Result<unknown>> {
    return http.get('/message/search', { sessionId, keyword })
  }
}