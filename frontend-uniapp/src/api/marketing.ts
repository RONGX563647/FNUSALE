import { http } from '@/utils/request'
import type { Result, PageResult } from '@/types/common'
import type {
  Coupon,
  UserCoupon,
  CouponDTO,
  SeckillActivity,
  SeckillActivityDTO,
  SeckillTimeSlot,
  SeckillResult,
  TodaySeckill,
  CouponPageParams,
  SeckillPageParams
} from '@/types/marketing'

// 优惠券 API
export const couponApi = {
  // 获取可领取优惠券列表
  getAvailable(): Promise<Result<Coupon[]>> {
    return http.get('/marketing/coupon/available')
  },

  // 领取优惠券
  receive(couponId: number): Promise<Result<void>> {
    return http.post(`/marketing/coupon/${couponId}/receive`)
  },

  // 获取我的优惠券列表
  getMy(status?: string): Promise<Result<UserCoupon[]>> {
    return http.get('/marketing/coupon/my', { status })
  },

  // 获取可用优惠券（下单时）
  getUsable(productId: number, price: number): Promise<Result<Coupon[]>> {
    return http.get('/marketing/coupon/usable', { productId, price })
  },

  // 获取优惠券详情
  getById(couponId: number): Promise<Result<Coupon>> {
    return http.get(`/marketing/coupon/${couponId}`)
  },

  // 新增优惠券（管理员）
  create(data: CouponDTO): Promise<Result<void>> {
    return http.post('/marketing/coupon', data)
  },

  // 更新优惠券（管理员）
  update(couponId: number, data: CouponDTO): Promise<Result<void>> {
    return http.put(`/marketing/coupon/${couponId}`, data)
  },

  // 删除优惠券（管理员）
  delete(couponId: number): Promise<Result<void>> {
    return http.delete(`/marketing/coupon/${couponId}`)
  },

  // 启用/禁用优惠券（管理员）
  updateStatus(couponId: number, status: number): Promise<Result<void>> {
    return http.put(`/marketing/coupon/${couponId}/status`, { status })
  },

  // 分页查询优惠券（管理员）
  getPage(params: CouponPageParams): Promise<Result<PageResult<Coupon>>> {
    return http.get('/marketing/coupon/page', params)
  },

  // 发放优惠券（管理员）
  grant(couponId: number, userIds: number[]): Promise<Result<void>> {
    return http.post(`/marketing/coupon/${couponId}/grant`, userIds)
  }
}

// 秒杀活动 API
export const seckillApi = {
  // 获取秒杀活动列表（进行中+即将开始）
  getList(): Promise<Result<SeckillActivity[]>> {
    return http.get('/marketing/seckill/list')
  },

  // 获取秒杀活动详情
  getActivityDetail(activityId: number): Promise<Result<SeckillActivity>> {
    return http.get(`/marketing/seckill/${activityId}`)
  },

  // 获取秒杀商品详情
  getProductDetail(productId: number): Promise<Result<SeckillActivity>> {
    return http.get(`/marketing/seckill/product/${productId}`)
  },

  // 参与秒杀
  join(activityId: number): Promise<Result<number>> {
    return http.post(`/marketing/seckill/${activityId}/join`)
  },

  // 获取秒杀结果
  getResult(activityId: number): Promise<Result<SeckillResult>> {
    return http.get(`/marketing/seckill/${activityId}/result`)
  },

  // 创建秒杀活动（管理员）
  create(data: SeckillActivityDTO): Promise<Result<void>> {
    return http.post('/marketing/seckill', data)
  },

  // 更新秒杀活动（管理员）
  update(activityId: number, data: SeckillActivityDTO): Promise<Result<void>> {
    return http.put(`/marketing/seckill/${activityId}`, data)
  },

  // 删除秒杀活动（管理员）
  delete(activityId: number): Promise<Result<void>> {
    return http.delete(`/marketing/seckill/${activityId}`)
  },

  // 分页查询秒杀活动（管理员）
  getPage(params: SeckillPageParams): Promise<Result<PageResult<SeckillActivity>>> {
    return http.get('/marketing/seckill/page', params)
  },

  // 获取今日秒杀
  getToday(): Promise<Result<TodaySeckill[]>> {
    return http.get('/marketing/seckill/today')
  },

  // 获取秒杀时段
  getTimeSlots(): Promise<Result<SeckillTimeSlot[]>> {
    return http.get('/marketing/seckill/time-slots')
  },

  // 设置秒杀提醒
  setReminder(activityId: number): Promise<Result<void>> {
    return http.post(`/marketing/seckill/${activityId}/reminder`)
  },

  // 取消秒杀提醒
  cancelReminder(activityId: number): Promise<Result<void>> {
    return http.delete(`/marketing/seckill/${activityId}/reminder`)
  }
}