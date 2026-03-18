import { http } from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { Dispute, DisputeProcessRequest } from '@/types/dispute'

// 纠纷处理 API
export const disputeApi = {
  // 获取纠纷列表
  async getPage(disputeStatus: string, pageNum = 1, pageSize = 10): Promise<Result<PageResult<Dispute>>> {
    const res = await http.get<PageResult<Dispute>>('/admin/dispute/page', { disputeStatus, pageNum, pageSize })
    return res.data
  },

  // 获取纠纷详情
  async getDetail(disputeId: number): Promise<Result<Dispute>> {
    const res = await http.get<Dispute>(`/admin/dispute/${disputeId}`)
    return res.data
  },

  // 处理纠纷
  async process(disputeId: number, data: DisputeProcessRequest): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/dispute/${disputeId}/process`, data)
    return res.data
  }
}