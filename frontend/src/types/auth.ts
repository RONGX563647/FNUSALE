// 管理员信息
export interface AdminInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: AdminRole
  permissions: string[]
  createTime: string
}

// 管理员角色
export type AdminRole = 'SUPER_ADMIN' | 'OPERATOR' | 'SERVICE'

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  adminInfo: AdminInfo
}

// Token 信息
export interface TokenInfo {
  accessToken: string
  refreshToken: string
  expiresIn: number
  expireTime: number
}