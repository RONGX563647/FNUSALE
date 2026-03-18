// IM 相关类型定义

// 聊天会话
export interface ImSessionVO {
  id: number
  targetUserId: number
  targetUserName: string
  targetUserAvatar: string
  productId: number
  productTitle: string
  productImage: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
  isPinned: boolean
}

// 聊天消息
export interface ImMessageVO {
  id: number
  sessionId: number
  senderId: number
  senderName: string
  senderAvatar: string
  type: string // TEXT/IMAGE/VOICE
  content: string
  imageUrl: string
  voiceUrl: string
  voiceDuration: number
  status: string // SENDING/SENT/READ/RECALLED
  createTime: string
}

// 快捷回复
export interface QuickReplyVO {
  id: number
  content: string
  isSystem: boolean
}