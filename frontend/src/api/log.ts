import { http } from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { SystemLog, LogQueryParams } from '@/types/log'

// 系统日志 API
export const logApi = {
  // 获取日志列表
  async getPage(params: LogQueryParams): Promise<Result<PageResult<SystemLog>>> {
    const res = await http.get<PageResult<SystemLog>>('/admin/log/page', params)
    return res.data
  },

  // 导出日志
  async export(startDate?: string, endDate?: string): Promise<Result<string>> {
    const res = await http.get<string>('/admin/log/export', { startDate, endDate })
    return res.data
  }
}