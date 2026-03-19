import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type {
  OrderCreateDTO,
  OrderVO,
  OrderStatistics,
  OrderEvaluationDTO,
  OrderEvaluationVO,
  TradeDisputeDTO,
  TradeDisputeVO,
  DisputeRecordVO
} from '@/types/trade'

const BASE_URL = '/order'

export const orderApi = {
  // 创建订单
  create(data: OrderCreateDTO): Promise<Result<number>> {
    return http.post(BASE_URL, data)
  },

  // 获取订单详情
  getById(orderId: number): Promise<Result<OrderVO>> {
    return http.get(`${BASE_URL}/${orderId}`)
  },

  // 根据订单号查询
  getByNo(orderNo: string): Promise<Result<OrderVO>> {
    return http.get(`${BASE_URL}/no/${orderNo}`)
  },

  // 获取我的订单列表
  getMyOrders(params: PageParams & { status?: string }): Promise<Result<PageResult<OrderVO>>> {
    return http.get(`${BASE_URL}/my`, params)
  },

  // 取消订单
  cancel(orderId: number, reason?: string): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${orderId}/cancel`, { reason })
  },

  // 确认收货
  confirmReceipt(orderId: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${orderId}/confirm`)
  },

  // 申请退款
  applyRefund(orderId: number, reason: string): Promise<Result<void>> {
    return http.post(`${BASE_URL}/${orderId}/refund`, { reason })
  },

  // 延长收货时间
  extendReceiveTime(orderId: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${orderId}/extend`)
  },

  // 获取订单统计
  getStatistics(): Promise<Result<OrderStatistics>> {
    return http.get(`${BASE_URL}/statistics`)
  },

  // 获取卖家订单列表
  getSellerOrders(params: PageParams & { status?: string }): Promise<Result<PageResult<OrderVO>>> {
    return http.get(`${BASE_URL}/seller`, params)
  },

  // 标记备好
  markReady(orderId: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${orderId}/ready`)
  }
}

// 支付 API
export const paymentApi = {
  // 发起支付
  createPayment(data: {
    orderId: number
    payType: string
    clientIp?: string
    userAgent?: string
  }): Promise<Result<PaymentCreateResult>> {
    return http.post('/payment/create', data)
  },

  // 支付回调（内部使用）
  callback(payType: string, data: Record<string, unknown>): Promise<Result<void>> {
    return http.post(`/payment/callback/${payType}`, data)
  },

  // 查询支付状态
  queryStatus(orderId: number): Promise<Result<PaymentStatus>> {
    return http.get(`/payment/status/${orderId}`)
  },

  // 申请退款
  applyRefund(data: { orderId: number; reason: string }): Promise<Result<void>> {
    return http.post('/payment/refund', data)
  },

  // 退款回调（内部使用）
  refundCallback(payType: string, data: Record<string, unknown>): Promise<Result<void>> {
    return http.post(`/payment/refund/callback/${payType}`, data)
  },

  // 查询退款状态
  queryRefundStatus(orderId: number): Promise<Result<RefundStatus>> {
    return http.get(`/payment/refund/status/${orderId}`)
  },

  // 获取支付方式列表
  getPaymentMethods(): Promise<Result<PaymentMethod[]>> {
    return http.get('/payment/methods')
  },

  // 获取模拟支付信息
  getMockPayInfo(payToken: string): Promise<Result<MockPayInfo>> {
    return http.get(`/payment/mock/info/${payToken}`)
  },

  // 模拟支付确认
  mockPayConfirm(payToken: string, success: boolean = true): Promise<Result<void>> {
    return http.post('/payment/mock/confirm', { payToken, success })
  }
}

// 支付创建结果
export interface PaymentCreateResult {
  payToken: string
  orderNo: string
  amount: number
  payType: string
  payTypeName: string
  payUrl: string
  expireMinutes: number
}

// 支付状态
export interface PaymentStatus {
  orderId: number
  orderNo: string
  payStatus: string
  orderStatus: string
  payStatusDesc: string
}

// 退款状态
export interface RefundStatus {
  orderId: number
  refundStatus: string
  refundStatusDesc: string
}

// 支付方式
export interface PaymentMethod {
  payType: string
  payName: string
  icon?: string
}

// 模拟支付信息
export interface MockPayInfo {
  orderNo: string
  amount: number
  payType: string
  payTypeName?: string
  orderStatus: string
  payStatus: string
  expireTime?: string
}

// 订单评价 API
export const evaluationApi = {
  // 提交评价
  submit(data: OrderEvaluationDTO): Promise<Result<void>> {
    return http.post('/evaluation', data)
  },

  // 获取订单评价
  getByOrderId(orderId: number): Promise<Result<OrderEvaluationVO>> {
    return http.get(`/evaluation/order/${orderId}`)
  },

  // 获取商品评价列表
  getByProductId(
    productId: number,
    params: PageParams
  ): Promise<Result<PageResult<OrderEvaluationVO>>> {
    return http.get(`/evaluation/product/${productId}`, params)
  },

  // 卖家回复评价
  reply(evaluationId: number, content: string): Promise<Result<void>> {
    return http.post(`/evaluation/${evaluationId}/reply`, { content })
  },

  // 获取我的评价列表
  getMy(params: PageParams): Promise<Result<PageResult<OrderEvaluationVO>>> {
    return http.get('/evaluation/my', params)
  },

  // 获取收到的评价
  getReceived(params: PageParams): Promise<Result<PageResult<OrderEvaluationVO>>> {
    return http.get('/evaluation/received', params)
  }
}

// 交易纠纷 API
export const disputeApi = {
  // 申请纠纷
  create(data: TradeDisputeDTO): Promise<Result<void>> {
    return http.post('/dispute', data)
  },

  // 获取纠纷详情
  getById(disputeId: number): Promise<Result<TradeDisputeVO>> {
    return http.get(`/dispute/${disputeId}`)
  },

  // 获取我的纠纷列表
  getMy(
    params: PageParams & { status?: string }
  ): Promise<Result<PageResult<TradeDisputeVO>>> {
    return http.get('/dispute/my', params)
  },

  // 撤销纠纷
  cancel(disputeId: number): Promise<Result<void>> {
    return http.delete(`/dispute/${disputeId}`)
  },

  // 补充证据
  addEvidence(disputeId: number, evidenceUrl: string): Promise<Result<void>> {
    return http.post(`/dispute/${disputeId}/evidence`, { evidenceUrl })
  },

  // 获取纠纷处理记录
  getRecords(disputeId: number): Promise<Result<DisputeRecordVO[]>> {
    return http.get(`/dispute/${disputeId}/records`)
  }
}