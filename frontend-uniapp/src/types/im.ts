// IM 相关类型定义

// 聊天会话
export interface ImSessionVO {
  id: number
  targetUserId: number
  targetUsername: string
  targetAvatarUrl?: string
  productId: number
  productName: string
  productMainImage?: string
  productPrice?: number
  lastMessageContent: string
  lastMessageTime: string
  unreadCount: number
  sessionStatus: string
  isPinned?: boolean
}

// 聊天消息
export interface ImMessageVO {
  id: number
  messageId: number
  sessionId: number
  senderId: number
  receiverId: number
  messageType: string // TEXT/IMAGE/VOICE
  content: string
  messageContent: string
  isRead: number
  sendTime: string
  duration?: number
  isRecalled?: number
}

// 快捷回复项
export interface QuickReplyItem {
  id: number
  replyContent: string
  sort: number
}

// 快捷回复列表
export interface QuickReplyListVO {
  systemReplies: QuickReplyItem[]
  userReplies: QuickReplyItem[]
}

// 发送消息响应
export interface SendMessageResult {
  messageId: number
  sendTime: string
}
