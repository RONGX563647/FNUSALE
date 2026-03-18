import { http } from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { PendingProduct, AuditRecord, AuditStatistics, BatchAuditResult } from '@/types/audit'

// 商品审核 API
export const auditApi = {
  // 获取待审核商品列表
  async getPendingList(pageNum = 1, pageSize = 10): Promise<Result<PageResult<PendingProduct>>> {
    const res = await http.get<PageResult<PendingProduct>>('/admin/audit/pending', { pageNum, pageSize })
    return res.data
  },

  // 审核通过
  async pass(productId: number): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/audit/${productId}/pass`)
    return res.data
  },

  // 审核驳回
  async reject(productId: number, reason: string): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/audit/${productId}/reject`, undefined, { params: { reason } })
    return res.data
  },

  // 批量审核通过
  async batchPass(productIds: number[]): Promise<Result<BatchAuditResult>> {
    const res = await http.put<BatchAuditResult>('/admin/audit/batch/pass', { productIds })
    return res.data
  },

  // 获取审核记录
  async getRecords(productId: number): Promise<Result<AuditRecord[]>> {
    const res = await http.get<AuditRecord[]>(`/admin/audit/${productId}/records`)
    return res.data
  },

  // 强制下架
  async forceOff(productId: number, reason: string): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/audit/${productId}/force-off`, undefined, { params: { reason } })
    return res.data
  },

  // 获取审核统计
  async getStatistics(): Promise<Result<AuditStatistics>> {
    const res = await http.get<AuditStatistics>('/admin/audit/statistics')
    return res.data
  }
}