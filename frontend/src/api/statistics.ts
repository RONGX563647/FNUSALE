import { http } from '@/utils/request'
import type { Result } from '@/types/api'
import type { TodayStatistics, TrendData, CategoryStatistics } from '@/types/statistics'

// 数据统计 API
export const statisticsApi = {
  // 获取今日数据概览
  async getToday(): Promise<Result<TodayStatistics>> {
    const res = await http.get<TodayStatistics>('/admin/statistics/today')
    return res.data
  },

  // 获取商品发布趋势
  async getProductTrend(days = 30): Promise<Result<TrendData>> {
    const res = await http.get<TrendData>('/admin/statistics/product/trend', { days })
    return res.data
  },

  // 获取成交趋势
  async getOrderTrend(days = 30): Promise<Result<TrendData>> {
    const res = await http.get<TrendData>('/admin/statistics/order/trend', { days })
    return res.data
  },

  // 获取热门品类统计
  async getHotCategory(): Promise<Result<CategoryStatistics[]>> {
    const res = await http.get<CategoryStatistics[]>('/admin/statistics/category/hot')
    return res.data
  },

  // 获取用户增长趋势
  async getUserGrowth(days = 30): Promise<Result<TrendData>> {
    const res = await http.get<TrendData>('/admin/statistics/user/growth', { days })
    return res.data
  },

  // 导出统计报表
  async exportReport(startDate: string, endDate: string): Promise<Result<string>> {
    const res = await http.get<string>('/admin/statistics/export', { startDate, endDate })
    return res.data
  }
}