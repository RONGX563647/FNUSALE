import { http } from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { UserDetail, UserQueryParams, CreditAdjustResult } from '@/types/user'

// 用户管理 API
export const userApi = {
  // 获取用户列表
  async getPage(params: UserQueryParams): Promise<Result<PageResult<UserDetail>>> {
    const res = await http.get<PageResult<UserDetail>>('/admin/user/page', params)
    return res.data
  },

  // 获取用户详情
  async getDetail(userId: number): Promise<Result<UserDetail>> {
    const res = await http.get<UserDetail>(`/admin/user/${userId}`)
    return res.data
  },

  // 获取待审核认证列表
  async getPendingAuthList(pageNum = 1, pageSize = 10): Promise<Result<PageResult<UserDetail>>> {
    const res = await http.get<PageResult<UserDetail>>('/admin/user/auth/pending', { pageNum, pageSize })
    return res.data
  },

  // 审核通过认证
  async authPass(userId: number): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/user/auth/${userId}/pass`)
    return res.data
  },

  // 审核驳回认证
  async authReject(userId: number, reason: string): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/user/auth/${userId}/reject`, undefined, { params: { reason } })
    return res.data
  },

  // 封禁用户
  async ban(userId: number, reason: string): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/user/${userId}/ban`, undefined, { params: { reason } })
    return res.data
  },

  // 解封用户
  async unban(userId: number): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/user/${userId}/unban`)
    return res.data
  },

  // 调整信誉分
  async adjustCredit(userId: number, score: number, reason: string): Promise<Result<CreditAdjustResult>> {
    const res = await http.put<CreditAdjustResult>(`/admin/user/${userId}/credit`, undefined, { params: { score, reason } })
    return res.data
  },

  // 获取用户认证记录
  async getAuthRecords(userId: number): Promise<Result<UserDetail>> {
    const res = await http.get<UserDetail>(`/admin/user/auth/${userId}/records`)
    return res.data
  }
}