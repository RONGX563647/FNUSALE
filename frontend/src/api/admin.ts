import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'

// 商品审核 API
export const adminAuditApi = {
  // 获取待审核商品列表
  getPendingList(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get('/admin/audit/pending', { params })
  },

  // 审核通过
  pass(productId: number): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/pass`)
  },

  // 审核驳回
  reject(productId: number, reason: string): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/reject`, null, { params: { reason } })
  },

  // 批量审核通过
  batchPass(productIds: number[]): Promise<Result<void>> {
    return http.put('/admin/audit/batch/pass', productIds)
  },

  // 获取审核记录
  getRecords(productId: number): Promise<Result<unknown>> {
    return http.get(`/admin/audit/${productId}/records`)
  },

  // 强制下架
  forceOff(productId: number, reason: string): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/force-off`, null, { params: { reason } })
  },

  // 获取审核统计
  getStatistics(): Promise<Result<unknown>> {
    return http.get('/admin/audit/statistics')
  }
}

// 用户管理 API
export const adminUserApi = {
  // 获取用户列表
  getPage(params: PageParams & { username?: string; authStatus?: string; identityType?: string }): Promise<Result<PageResult<unknown>>> {
    return http.get('/admin/user/page', { params })
  },

  // 获取用户详情
  getUserDetail(userId: number): Promise<Result<unknown>> {
    return http.get(`/admin/user/${userId}`)
  },

  // 获取待审核认证列表
  getPendingAuthList(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get('/admin/user/auth/pending', { params })
  },

  // 审核通过认证
  authPass(userId: number): Promise<Result<void>> {
    return http.put(`/admin/user/auth/${userId}/pass`)
  },

  // 审核驳回认证
  authReject(userId: number, reason: string): Promise<Result<void>> {
    return http.put(`/admin/user/auth/${userId}/reject`, null, { params: { reason } })
  },

  // 封禁用户
  ban(userId: number, reason: string): Promise<Result<void>> {
    return http.put(`/admin/user/${userId}/ban`, null, { params: { reason } })
  },

  // 解封用户
  unban(userId: number): Promise<Result<void>> {
    return http.put(`/admin/user/${userId}/unban`)
  },

  // 调整信誉分
  adjustCredit(userId: number, score: number, reason: string): Promise<Result<void>> {
    return http.put(`/admin/user/${userId}/credit`, null, { params: { score, reason } })
  },

  // 获取用户认证记录
  getAuthRecords(userId: number): Promise<Result<unknown>> {
    return http.get(`/admin/user/auth/${userId}/records`)
  }
}

// 数据统计 API
export const adminStatisticsApi = {
  // 获取今日数据概览
  getToday(): Promise<Result<unknown>> {
    return http.get('/admin/statistics/today')
  },

  // 获取日期范围统计
  getRange(startDate: string, endDate: string): Promise<Result<unknown>> {
    return http.get('/admin/statistics/range', { params: { startDate, endDate } })
  },

  // 获取商品发布趋势
  getProductTrend(days = 30): Promise<Result<unknown>> {
    return http.get('/admin/statistics/product/trend', { params: { days } })
  },

  // 获取成交趋势
  getOrderTrend(days = 30): Promise<Result<unknown>> {
    return http.get('/admin/statistics/order/trend', { params: { days } })
  },

  // 获取热门品类统计
  getHotCategory(): Promise<Result<unknown>> {
    return http.get('/admin/statistics/category/hot')
  },

  // 获取用户增长趋势
  getUserGrowth(days = 30): Promise<Result<unknown>> {
    return http.get('/admin/statistics/user/growth', { params: { days } })
  },

  // 获取秒杀活动统计
  getSeckill(activityId?: number): Promise<Result<unknown>> {
    return http.get('/admin/statistics/seckill', { params: { activityId } })
  },

  // 获取优惠券统计
  getCoupon(): Promise<Result<unknown>> {
    return http.get('/admin/statistics/coupon')
  },

  // 获取AI分类准确率统计
  getAiAccuracy(): Promise<Result<unknown>> {
    return http.get('/admin/statistics/ai/accuracy')
  },

  // 导出统计报表
  export(startDate: string, endDate: string): Promise<Result<string>> {
    return http.get('/admin/statistics/export', { params: { startDate, endDate } })
  },

  // 获取用户活跃度统计
  getUserActivity(): Promise<Result<unknown>> {
    return http.get('/admin/statistics/user/activity')
  }
}

// 系统配置 API
export const systemConfigApi = {
  // 获取配置列表
  getList(): Promise<Result<unknown[]>> {
    return http.get('/admin/config/list')
  },

  // 获取配置详情
  getByKey(configKey: string): Promise<Result<unknown>> {
    return http.get(`/admin/config/${configKey}`)
  },

  // 更新配置
  update(configKey: string, configValue: string): Promise<Result<void>> {
    return http.put(`/admin/config/${configKey}`, null, { params: { configValue } })
  },

  // 批量更新配置
  batchUpdate(data: unknown): Promise<Result<void>> {
    return http.put('/admin/config/batch', data)
  },

  // 获取校园围栏配置
  getCampusFence(): Promise<Result<unknown>> {
    return http.get('/admin/config/campus-fence')
  },

  // 更新校园围栏配置
  updateCampusFence(data: unknown): Promise<Result<void>> {
    return http.put('/admin/config/campus-fence', data)
  },

  // 获取秒杀配置
  getSeckill(): Promise<Result<unknown>> {
    return http.get('/admin/config/seckill')
  },

  // 更新秒杀配置
  updateSeckill(data: unknown): Promise<Result<void>> {
    return http.put('/admin/config/seckill', data)
  },

  // 刷新缓存
  refreshCache(): Promise<Result<void>> {
    return http.post('/admin/config/refresh-cache')
  },

  // 获取配置修改记录
  getHistory(params: PageParams & { configKey?: string }): Promise<Result<unknown>> {
    return http.get('/admin/config/history', { params })
  }
}