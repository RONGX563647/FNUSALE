import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type { OrderCreateDTO, OrderVO, OrderEvaluationDTO, TradeDisputeDTO } from '@/types/trade'

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
  getStatistics(): Promise<Result<unknown>> {
    return http.get(`${BASE_URL}/statistics`)
  },

  // 获取卖家订单列表
  getSellerOrders(params: PageParams & { status?: string }): Promise<Result<PageResult<OrderVO>>> {
    return http.get(`${BASE_URL}/seller`, params)
  },

  // 发货（备好）
  markReady(orderId: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${orderId}/ready`)
  }
}

// 支付 API
export const paymentApi = {
  // 发起支付
  createPayment(orderId: number, payType: string): Promise<Result<unknown>> {
    return http.post('/payment/create', { orderId, payType })
  },

  // 查询支付状态
  queryStatus(orderId: number): Promise<Result<unknown>> {
    return http.get(`/payment/status/${orderId}`)
  },

  // 申请退款
  applyRefund(orderId: number, reason: string): Promise<Result<void>> {
    return http.post('/payment/refund', { orderId, reason })
  },

  // 查询退款状态
  queryRefundStatus(orderId: number): Promise<Result<unknown>> {
    return http.get(`/payment/refund/status/${orderId}`)
  },

  // 获取支付方式列表
  getPaymentMethods(): Promise<Result<unknown>> {
    return http.get('/payment/methods')
  }
}

// 订单评价 API
export const evaluationApi = {
  // 提交评价
  submit(data: OrderEvaluationDTO): Promise<Result<void>> {
    return http.post('/evaluation', data)
  },

  // 获取订单评价
  getByOrderId(orderId: number): Promise<Result<unknown>> {
    return http.get(`/evaluation/order/${orderId}`)
  },

  // 获取商品评价列表
  getByProductId(productId: number, params: PageParams): Promise<Result<unknown>> {
    return http.get(`/evaluation/product/${productId}`, params)
  },

  // 卖家回复评价
  reply(evaluationId: number, content: string): Promise<Result<void>> {
    return http.post(`/evaluation/${evaluationId}/reply`, { content })
  },

  // 获取我的评价列表
  getMy(params: PageParams): Promise<Result<unknown>> {
    return http.get('/evaluation/my', params)
  },

  // 获取收到的评价
  getReceived(params: PageParams): Promise<Result<unknown>> {
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
  getById(disputeId: number): Promise<Result<unknown>> {
    return http.get(`/dispute/${disputeId}`)
  },

  // 获取我的纠纷列表
  getMy(params: PageParams & { status?: string }): Promise<Result<PageResult<unknown>>> {
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
  getRecords(disputeId: number): Promise<Result<unknown>> {
    return http.get(`/dispute/${disputeId}/records`)
  }
}