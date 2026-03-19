// 用户相关 API
export { userApi, addressApi, pickPointApi, signApi, pointsApi, rankingApi } from './user'

// 商品相关 API
export { productApi, categoryApi } from './product'

// 交易相关 API
export { orderApi, paymentApi, evaluationApi as tradeEvaluationApi, disputeApi } from './trade'

// AI 相关 API
export { aiCategoryApi, aiRecommendApi, aiPriceApi, aiServiceApi } from './ai'

// IM 相关 API
export { sessionApi, messageApi } from './im'

// 营销相关 API
export { couponApi, seckillApi } from './marketing'

// 管理模块相关 API
export { 
  adminAuditApi, 
  adminUserApi, 
  adminStatisticsApi, 
  systemConfigApi,
  adminDisputeApi,
  systemLogApi,
  adminAuthApi
} from './admin'
