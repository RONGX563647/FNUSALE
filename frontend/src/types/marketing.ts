// 营销相关类型定义

// 优惠券
export interface CouponDTO {
  name: string
  type: string // DISCOUNT/REDUCTION
  value: number
  minAmount: number
  startTime: string
  endTime: string
  totalCount: number
  limitPerUser: number
  categoryIds?: number[]
}

// 秒杀活动
export interface SeckillActivityDTO {
  name: string
  startTime: string
  endTime: string
  productId: number
  seckillPrice: number
  seckillStock: number
  limitPerUser: number
}