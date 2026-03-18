import { http } from '@/utils/request'
import type { Result } from '@/types/api'
import type { LoginRequest, LoginResponse, AdminInfo } from '@/types/auth'

// 登录认证 API
export const authApi = {
  // 管理员登录
  async login(data: LoginRequest): Promise<Result<LoginResponse>> {
    const res = await http.post<LoginResponse>('/admin/auth/login', data)
    return res.data
  },

  // 退出登录
  async logout(): Promise<Result<void>> {
    const res = await http.post<void>('/admin/auth/logout')
    return res.data
  },

  // 获取当前管理员信息
  async getAdminInfo(): Promise<Result<AdminInfo>> {
    const res = await http.get<AdminInfo>('/admin/auth/info')
    return res.data
  },

  // 刷新 Token
  async refreshToken(refreshToken: string): Promise<Result<LoginResponse>> {
    const res = await http.post<LoginResponse>('/admin/auth/refresh-token', { refreshToken })
    return res.data
  }
}