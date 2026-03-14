// 订单相关类型定义

// 订单创建请求
export interface OrderCreateDTO {
  productId: number
  quantity: number
  pickPointId: number
  couponId?: number
  remark?: string
}

// 订单信息
export interface OrderVO {
  id: number
  orderNo: string
  productId: number
  productTitle: string
  productImage: string
  productPrice: number
  quantity: number
  totalAmount: number
  payAmount: number
  couponId: number
  couponName: string
  discountAmount: number
  buyerId: number
  buyerName: string
  buyerAvatar: string
  sellerId: number
  sellerName: string
  sellerAvatar: string
  pickPointId: number
  pickPointName: string
  status: string // PENDING_PAID/PAID/READY/PENDING_RECEIVE/COMPLETED/CANCELLED/REFUNDING/REFUNDED
  remark: string
  payTime: string
  readyTime: string
  receiveTime: string
  createTime: string
}

// 订单评价请求
export interface OrderEvaluationDTO {
  orderId: number
  rating: number
  content: string
  images?: string[]
  isAnonymous: boolean
}

// 交易纠纷请求
export interface TradeDisputeDTO {
  orderId: number
  type: string // QUALITY/DESCRIPTION_MISMATCH/OTHER
  reason: string
  evidenceUrls?: string[]
}