// 纠纷信息
export interface Dispute {
  disputeId: number
  orderId: number
  orderNo: string
  initiatorId: number
  initiatorName: string
  accusedId: number
  accusedName: string
  disputeType: DisputeType
  disputeStatus: DisputeStatus
  evidenceUrls?: string[]
  initiatorDescription?: string
  accusedDescription?: string
  createTime: string
  updateTime?: string
  processResult?: ProcessResult
  processRemark?: string
}

// 纠纷类型
export type DisputeType = 'PRODUCT_NOT_MATCH' | 'NO_DELIVERY' | 'PRODUCT_DAMAGED' | 'OTHER'

// 纠纷状态
export type DisputeStatus = 'PENDING' | 'PROCESSING' | 'RESOLVED'

// 处理结果
export type ProcessResult = 'BUYER_WIN' | 'SELLER_WIN' | 'NEGOTIATE'

// 纠纷处理请求
export interface DisputeProcessRequest {
  processResult: ProcessResult
  processRemark: string
  buyerCreditChange: number
  sellerCreditChange: number
}

// 纠纷当事人信息
export interface DisputeParty {
  userId: number
  username: string
  description?: string
}

// 订单信息（纠纷详情中）
export interface DisputeOrderInfo {
  productName: string
  productPrice: number
  createTime: string
}