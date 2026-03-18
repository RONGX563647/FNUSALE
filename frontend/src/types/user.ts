// 用户详情
export interface UserDetail {
  userId: number
  username: string
  phone: string
  email?: string
  studentTeacherId?: string
  identityType?: 'STUDENT' | 'TEACHER'
  authStatus: AuthStatus
  creditScore: number
  status: UserStatus
  registerTime: string
  avatar?: string
  campusCardUrl?: string
  authRejectReason?: string
}

// 认证状态
export type AuthStatus = 'UNAUTH' | 'UNDER_REVIEW' | 'AUTH_SUCCESS' | 'AUTH_FAILED'

// 用户状态
export type UserStatus = 'NORMAL' | 'BANNED'

// 用户查询参数
export interface UserQueryParams {
  pageNum: number
  pageSize: number
  username?: string
  authStatus?: AuthStatus
  identityType?: 'STUDENT' | 'TEACHER'
}

// 信誉分调整结果
export interface CreditAdjustResult {
  beforeScore: number
  afterScore: number
}