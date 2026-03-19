import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'

// ==================== 类型定义 ====================

// 商品审核相关类型
export interface ProductAuditVO {
  id: number
  productId: number
  productName: string
  productDesc: string
  price: number
  images: string[]
  publishTime: string
  sellerName: string
  sellerId: number
}

export interface ProductAuditRecordVO {
  id: number
  productId: number
  adminId: number
  auditResult: string
  rejectReason?: string
  auditTime: string
}

export interface AuditStatisticsVO {
  pendingCount: number
  passCount: number
  rejectCount: number
}

export interface BatchAuditDTO {
  productIds: number[]
}

export interface BatchAuditResultVO {
  successCount: number
  failCount: number
}

// 用户管理相关类型
export interface AdminUserVO {
  id: number
  username: string
  phone: string
  email?: string
  identityType?: string
  authStatus: string
  creditScore: number
  status: number
  createTime: string
}

export interface AdminUserDetailVO {
  id: number
  username: string
  phone: string
  email?: string
  identityType?: string
  authStatus: string
  creditScore: number
  status: number
  createTime: string
  lastLoginTime?: string
}

export interface UserAuthPendingVO {
  id: number
  userId: number
  username: string
  identityType: string
  schoolName: string
  submitTime: string
}

export interface UserAuthRecordVO {
  id: number
  userId: number
  authStatus: string
  authTime: string
  operatorName?: string
}

export interface CreditAdjustDTO {
  score: number
  reason: string
}

export interface CreditAdjustResultVO {
  newScore: number
}

export interface UserBanDTO {
  reason: string
}

export interface UserRejectDTO {
  reason: string
}

// 数据统计相关类型
export interface TodayStatisticsVO {
  newUserCount: number
  productPublishCount: number
  orderSuccessCount: number
  transactionAmount: number
  seckillParticipateCount: number
  couponUseCount: number
}

export interface RangeStatisticsVO {
  totalUserCount: number
  totalProductCount: number
  totalOrderCount: number
  totalTransactionAmount: number
}

export interface TrendDataVO {
  date: string
  count: number
}

export interface OrderTrendDataVO {
  date: string
  orderCount: number
  transactionAmount: number
}

export interface UserGrowthDataVO {
  date: string
  newUserCount: number
}

export interface CategoryStatisticsVO {
  categoryId: number
  categoryName: string
  productCount: number
  orderCount: number
}

export interface SeckillStatisticsVO {
  totalParticipateCount: number
  successCount: number
  successRate: number
}

export interface CouponStatisticsVO {
  totalIssuedCount: number
  totalUsedCount: number
  useRate: number
}

export interface AIAccuracyStatisticsVO {
  totalRecognizeCount: number
  correctCount: number
  accuracyRate: number
}

export interface UserActivityStatisticsVO {
  dailyActiveUsers: number
  weeklyActiveUsers: number
  monthlyActiveUsers: number
}

// 系统配置相关类型
export interface SystemConfigVO {
  id: number
  configKey: string
  configValue: string
  configDesc?: string
  updateTime?: string
  adminId?: number
}

export interface ConfigUpdateDTO {
  configValue: string
}

export interface ConfigBatchUpdateDTO {
  configKey: string
  configValue: string
}

export interface CampusFenceConfigVO {
  longitude: string
  latitude: string
  radius: number
}

export interface CampusFenceUpdateDTO {
  longitude: string
  latitude: string
  radius: number
}

export interface SeckillConfigVO {
  qpsLimit: number
  maxStockPerUser: number
}

export interface SeckillConfigUpdateDTO {
  qpsLimit: number
  maxStockPerUser: number
}

export interface ConfigHistoryVO {
  id: number
  configKey: string
  oldValue: string
  newValue: string
  adminId: number
  updateTime: string
}

// 纠纷处理相关类型
export interface AdminDisputeVO {
  id: number
  orderId: number
  initiatorId: number
  initiatorName: string
  accusedId: number
  accusedName: string
  disputeType: string
  evidenceUrl?: string
  disputeStatus: string
  createTime: string
}

export interface AdminDisputeDetailVO {
  id: number
  orderId: number
  initiatorId: number
  initiatorName: string
  accusedId: number
  accusedName: string
  disputeType: string
  evidenceUrl?: string
  disputeStatus: string
  processResult?: string
  adminId?: number
  processRemark?: string
  createTime: string
  updateTime?: string
}

export interface DisputeProcessDTO {
  processResult: string
  processRemark?: string
}

// 操作日志相关类型
export interface SystemLogVO {
  id: number
  operateUserId?: number
  operateUserName?: string
  moduleName: string
  operateType: string
  operateContent: string
  ipAddress: string
  deviceInfo?: string
  exceptionInfo?: string
  logType: string
  createTime: string
}

// 管理员认证相关类型
export interface AdminLoginDTO {
  username: string
  password: string
}

export interface AdminLoginVO {
  token: string
  refreshToken: string
  adminId: number
  username: string
  nickname?: string
  role: string
}

export interface AdminInfoVO {
  id: number
  username: string
  nickname?: string
  avatarUrl?: string
  role: string
  status: number
  lastLoginTime?: string
  lastLoginIp?: string
}

export interface RefreshTokenDTO {
  refreshToken: string
}

// ==================== API 定义 ====================

// 商品审核 API
export const adminAuditApi = {
  // 获取待审核商品列表
  getPendingList(params: PageParams): Promise<Result<PageResult<ProductAuditVO>>> {
    return http.get('/admin/audit/pending', { params })
  },

  // 审核通过
  pass(productId: number): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/pass`)
  },

  // 审核驳回
  reject(productId: number, data: UserRejectDTO): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/reject`, data)
  },

  // 批量审核通过
  batchPass(data: BatchAuditDTO): Promise<Result<BatchAuditResultVO>> {
    return http.put('/admin/audit/batch/pass', data)
  },

  // 获取审核记录
  getRecords(productId: number): Promise<Result<ProductAuditRecordVO[]>> {
    return http.get(`/admin/audit/${productId}/records`)
  },

  // 强制下架
  forceOff(productId: number, data: UserRejectDTO): Promise<Result<void>> {
    return http.put(`/admin/audit/${productId}/force-off`, data)
  },

  // 获取审核统计
  getStatistics(): Promise<Result<AuditStatisticsVO>> {
    return http.get('/admin/audit/statistics')
  }
}

// 用户管理 API
export const adminUserApi = {
  // 获取用户列表
  getPage(params: {
    username?: string
    authStatus?: string
    identityType?: string
    pageNum?: number
    pageSize?: number
  }): Promise<Result<PageResult<AdminUserVO>>> {
    return http.get('/admin/user/page', { params })
  },

  // 获取用户详情
  getDetail(userId: number): Promise<Result<AdminUserDetailVO>> {
    return http.get(`/admin/user/${userId}`)
  },

  // 获取待审核认证列表
  getAuthPending(params: PageParams): Promise<Result<PageResult<UserAuthPendingVO>>> {
    return http.get('/admin/user/auth/pending', { params })
  },

  // 审核通过认证
  passAuth(userId: number): Promise<Result<void>> {
    return http.put(`/admin/user/auth/${userId}/pass`)
  },

  // 审核驳回认证
  rejectAuth(userId: number, data: UserRejectDTO): Promise<Result<void>> {
    return http.put(`/admin/user/auth/${userId}/reject`, data)
  },

  // 封禁用户
  ban(userId: number, data: UserBanDTO): Promise<Result<void>> {
    return http.put(`/admin/user/${userId}/ban`, data)
  },

  // 解封用户
  unban(userId: number): Promise<Result<void>> {
    return http.put(`/admin/user/${userId}/unban`)
  },

  // 调整信誉分
  adjustCredit(userId: number, data: CreditAdjustDTO): Promise<Result<CreditAdjustResultVO>> {
    return http.put(`/admin/user/${userId}/credit`, data)
  },

  // 获取用户认证记录
  getAuthRecords(userId: number): Promise<Result<UserAuthRecordVO[]>> {
    return http.get(`/admin/user/auth/${userId}/records`)
  }
}

// 数据统计 API
export const adminStatisticsApi = {
  // 获取今日数据概览
  getToday(): Promise<Result<TodayStatisticsVO>> {
    return http.get('/admin/statistics/today')
  },

  // 获取日期范围统计
  getRange(params: {
    startDate: string
    endDate: string
  }): Promise<Result<RangeStatisticsVO>> {
    return http.get('/admin/statistics/range', { params })
  },

  // 获取商品发布趋势
  getProductTrend(params?: { days?: number }): Promise<Result<TrendDataVO[]>> {
    return http.get('/admin/statistics/product/trend', { params })
  },

  // 获取成交趋势
  getOrderTrend(params?: { days?: number }): Promise<Result<OrderTrendDataVO[]>> {
    return http.get('/admin/statistics/order/trend', { params })
  },

  // 获取热门品类统计
  getCategoryHot(): Promise<Result<CategoryStatisticsVO[]>> {
    return http.get('/admin/statistics/category/hot')
  },

  // 获取用户增长趋势
  getUserGrowth(params?: { days?: number }): Promise<Result<UserGrowthDataVO[]>> {
    return http.get('/admin/statistics/user/growth', { params })
  },

  // 获取秒杀活动统计
  getSeckill(params?: { activityId?: number }): Promise<Result<SeckillStatisticsVO>> {
    return http.get('/admin/statistics/seckill', { params })
  },

  // 获取优惠券统计
  getCoupon(): Promise<Result<CouponStatisticsVO>> {
    return http.get('/admin/statistics/coupon')
  },

  // 获取AI分类准确率统计
  getAIAccuracy(): Promise<Result<AIAccuracyStatisticsVO>> {
    return http.get('/admin/statistics/ai/accuracy')
  },

  // 导出统计报表
  exportReport(params: {
    startDate: string
    endDate: string
  }): Promise<Result<string>> {
    return http.get('/admin/statistics/export', { params })
  },

  // 获取用户活跃度统计
  getUserActivity(): Promise<Result<UserActivityStatisticsVO>> {
    return http.get('/admin/statistics/user/activity')
  }
}

// 系统配置 API
export const systemConfigApi = {
  // 获取配置列表
  getList(): Promise<Result<SystemConfigVO[]>> {
    return http.get('/admin/config/list')
  },

  // 获取配置详情
  getDetail(configKey: string): Promise<Result<SystemConfigVO>> {
    return http.get(`/admin/config/${configKey}`)
  },

  // 更新配置
  update(configKey: string, data: ConfigUpdateDTO): Promise<Result<void>> {
    return http.put(`/admin/config/${configKey}`, data)
  },

  // 批量更新配置
  batchUpdate(data: ConfigBatchUpdateDTO[]): Promise<Result<void>> {
    return http.put('/admin/config/batch', data)
  },

  // 获取校园围栏配置
  getCampusFence(): Promise<Result<CampusFenceConfigVO>> {
    return http.get('/admin/config/campus-fence')
  },

  // 更新校园围栏配置
  updateCampusFence(data: CampusFenceUpdateDTO): Promise<Result<void>> {
    return http.put('/admin/config/campus-fence', data)
  },

  // 获取秒杀配置
  getSeckill(): Promise<Result<SeckillConfigVO>> {
    return http.get('/admin/config/seckill')
  },

  // 更新秒杀配置
  updateSeckill(data: SeckillConfigUpdateDTO): Promise<Result<void>> {
    return http.put('/admin/config/seckill', data)
  },

  // 刷新缓存
  refreshCache(): Promise<Result<void>> {
    return http.post('/admin/config/refresh-cache')
  },

  // 获取配置修改记录
  getHistory(params: {
    configKey?: string
    pageNum?: number
    pageSize?: number
  }): Promise<Result<PageResult<ConfigHistoryVO>>> {
    return http.get('/admin/config/history', { params })
  }
}

// 纠纷处理 API
export const adminDisputeApi = {
  // 获取纠纷列表
  getPage(params: {
    disputeStatus?: string
    pageNum?: number
    pageSize?: number
  }): Promise<Result<PageResult<AdminDisputeVO>>> {
    return http.get('/admin/dispute/page', { params })
  },

  // 获取纠纷详情
  getDetail(disputeId: number): Promise<Result<AdminDisputeDetailVO>> {
    return http.get(`/admin/dispute/${disputeId}`)
  },

  // 处理纠纷
  process(disputeId: number, data: DisputeProcessDTO): Promise<Result<void>> {
    return http.put(`/admin/dispute/${disputeId}/process`, data)
  }
}

// 操作日志 API
export const systemLogApi = {
  // 获取日志列表
  getPage(params: {
    moduleName?: string
    operateType?: string
    startDate?: string
    endDate?: string
    pageNum?: number
    pageSize?: number
  }): Promise<Result<PageResult<SystemLogVO>>> {
    return http.get('/admin/log/page', { params })
  },

  // 导出日志
  export(params: {
    startDate: string
    endDate: string
  }): Promise<Result<string>> {
    return http.get('/admin/log/export', { params })
  }
}

// 管理员认证 API
export const adminAuthApi = {
  // 管理员登录
  login(data: AdminLoginDTO): Promise<Result<AdminLoginVO>> {
    return http.post('/admin/auth/login', data)
  },

  // 管理员登出
  logout(): Promise<Result<void>> {
    return http.post('/admin/auth/logout')
  },

  // 获取管理员信息
  getInfo(): Promise<Result<AdminInfoVO>> {
    return http.get('/admin/auth/info')
  },

  // 刷新Token
  refreshToken(data: RefreshTokenDTO): Promise<Result<AdminLoginVO>> {
    return http.post('/admin/auth/refresh-token', data)
  }
}
