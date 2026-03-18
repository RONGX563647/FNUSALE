// 营销相关类型定义

// 优惠券类型
export type CouponType = 'FULL_REDUCE' | 'DIRECT_REDUCE' | 'CATEGORY'

// 优惠券状态
export type CouponStatus = 'UNUSED' | 'USED' | 'EXPIRED'

// 秒杀活动状态
export type SeckillStatus = 'NOT_START' | 'ON_GOING' | 'END'

// 优惠券实体
export interface Coupon {
  id: number
  couponName: string
  couponType: CouponType
  fullAmount: number
  reduceAmount: number
  categoryId?: number
  categoryName?: string
  totalCount: number
  receivedCount: number
  remainCount: number
  startTime: string
  endTime: string
  enableStatus: number
  received?: boolean
  createTime: string
}

// 用户优惠券
export interface UserCoupon {
  id: number
  couponId: number
  couponName: string
  couponType: CouponType
  fullAmount: number
  reduceAmount: number
  categoryId?: number
  categoryName?: string
  couponStatus: CouponStatus
  receiveTime: string
  useTime?: string
  expireTime: string
  orderId?: number
  usable?: boolean
}

// 优惠券DTO（创建/更新用）
export interface CouponDTO {
  couponName: string
  couponType: CouponType
  fullAmount: number
  reduceAmount: number
  categoryId?: number
  totalCount: number
  startTime: string
  endTime: string
  enableStatus: number
  [key: string]: unknown
}

// 秒杀活动
export interface SeckillActivity {
  id: number
  activityName: string
  productId: number
  productName?: string
  productImage?: string
  originalPrice?: number
  seckillPrice: number
  totalStock: number
  remainStock: number
  startTime: string
  endTime: string
  activityStatus: SeckillStatus
  createTime: string
  updateTime?: string
}

// 秒杀活动DTO
export interface SeckillActivityDTO {
  activityName: string
  productId: number
  seckillPrice: number
  totalStock: number
  startTime: string
  endTime: string
  [key: string]: unknown
}

// 秒杀时段
export interface SeckillTimeSlot {
  timeSlot: string
  activities: SeckillActivity[]
}

// 秒杀提醒
export interface SeckillReminder {
  id: number
  userId: number
  activityId: number
  remindTime: string
  isReminded: number
  createTime: string
}

// 秒杀结果
export interface SeckillResult {
  success: boolean
  orderId?: number
  message: string
}

// 今日秒杀数据
export interface TodaySeckill {
  timeSlot: string
  activities: SeckillActivity[]
}

// 分页查询参数
export interface CouponPageParams {
  pageNum: number
  pageSize: number
  name?: string
  type?: string
  status?: number
  [key: string]: unknown
}

// 分页查询参数
export interface SeckillPageParams {
  pageNum: number
  pageSize: number
  status?: string
  [key: string]: unknown
}