// 订单相关类型定义

// 订单创建请求
export interface OrderCreateDTO {
  productId: number
  couponId?: number
  pickPointId?: number
  payType?: string
  [key: string]: unknown
}

// 订单信息
export interface OrderVO {
  id: number
  orderNo: string
  productId: number
  productName: string
  productImage: string
  productPrice: number
  couponDeductAmount: number
  actualPayAmount: number
  pickPointName: string
  payType: string
  payStatus: string
  orderStatus: string
  sellerId: number
  sellerName: string
  cancelReason?: string
  readyTime?: string
  extendReceiveDays?: number
  successTime?: string
  createTime: string
}

// 订单统计信息
export interface OrderStatistics {
  unpaidCount: number
  waitPickCount: number
  successCount: number
  cancelCount: number
  refundCount: number
}

// 订单评价请求
export interface OrderEvaluationDTO {
  orderId: number
  score: number
  evaluationTag?: string
  evaluationContent?: string
  evaluationImageUrl?: string
  [key: string]: unknown
}

// 订单评价信息
export interface OrderEvaluationVO {
  id: number
  orderId: number
  evaluatorId: number
  evaluatorName: string
  evaluatedId: number
  evaluatedName: string
  score: number
  evaluationTag?: string
  evaluationContent?: string
  evaluationImageUrl?: string
  replyContent?: string
  replyTime?: string
  appendContent?: string
  appendImageUrl?: string
  appendTime?: string
  createTime: string
}

// 交易纠纷请求
export interface TradeDisputeDTO {
  orderId: number
  disputeType: string
  evidenceUrl?: string
  [key: string]: unknown
}

// 交易纠纷信息
export interface TradeDisputeVO {
  id: number
  orderId: number
  orderNo: string
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

// 纠纷处理记录
export interface DisputeRecordVO {
  id: number
  disputeId: number
  operatorId: number
  operatorName: string
  operateType: string
  operateDesc: string
  createTime: string
}