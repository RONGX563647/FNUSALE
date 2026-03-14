import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type { CouponDTO, SeckillActivityDTO } from '@/types/marketing'

// 优惠券 API
export const couponApi = {
  // 获取可领取优惠券列表
  getAvailable(): Promise<Result<unknown[]>> {
    return http.get('/coupon/available')
  },

  // 领取优惠券
  receive(couponId: number): Promise<Result<void>> {
    return http.post(`/coupon/${couponId}/receive`)
  },

  // 获取我的优惠券列表
  getMy(status?: string): Promise<Result<unknown[]>> {
    return http.get('/coupon/my', { params: { status } })
  },

  // 获取可用优惠券
  getUsable(productId: number, price: number): Promise<Result<unknown[]>> {
    return http.get('/coupon/usable', { params: { productId, price } })
  },

  // 获取优惠券详情
  getById(couponId: number): Promise<Result<unknown>> {
    return http.get(`/coupon/${couponId}`)
  },

  // 新增优惠券
  create(data: CouponDTO): Promise<Result<void>> {
    return http.post('/coupon', data)
  },

  // 更新优惠券
  update(couponId: number, data: CouponDTO): Promise<Result<void>> {
    return http.put(`/coupon/${couponId}`, data)
  },

  // 删除优惠券
  delete(couponId: number): Promise<Result<void>> {
    return http.delete(`/coupon/${couponId}`)
  },

  // 启用/禁用优惠券
  updateStatus(couponId: number, status: number): Promise<Result<void>> {
    return http.put(`/coupon/${couponId}/status`, null, { params: { status } })
  },

  // 分页查询优惠券
  getPage(params: PageParams & { name?: string; type?: string; status?: number }): Promise<Result<PageResult<unknown>>> {
    return http.get('/coupon/page', { params })
  },

  // 发放优惠券
  grant(couponId: number, userIds: number[]): Promise<Result<void>> {
    return http.post(`/coupon/${couponId}/grant`, userIds)
  }
}

// 秒杀活动 API
export const seckillApi = {
  // 获取秒杀活动列表
  getList(): Promise<Result<unknown[]>> {
    return http.get('/seckill/list')
  },

  // 获取秒杀活动详情
  getActivityDetail(activityId: number): Promise<Result<unknown>> {
    return http.get(`/seckill/${activityId}`)
  },

  // 获取秒杀商品详情
  getProductDetail(productId: number): Promise<Result<unknown>> {
    return http.get(`/seckill/product/${productId}`)
  },

  // 参与秒杀
  join(activityId: number): Promise<Result<number>> {
    return http.post(`/seckill/${activityId}/join`)
  },

  // 获取秒杀结果
  getResult(activityId: number): Promise<Result<unknown>> {
    return http.get(`/seckill/${activityId}/result`)
  },

  // 创建秒杀活动
  create(data: SeckillActivityDTO): Promise<Result<void>> {
    return http.post('/seckill', data)
  },

  // 更新秒杀活动
  update(activityId: number, data: SeckillActivityDTO): Promise<Result<void>> {
    return http.put(`/seckill/${activityId}`, data)
  },

  // 删除秒杀活动
  delete(activityId: number): Promise<Result<void>> {
    return http.delete(`/seckill/${activityId}`)
  },

  // 分页查询秒杀活动
  getPage(params: PageParams & { status?: string }): Promise<Result<PageResult<unknown>>> {
    return http.get('/seckill/page', { params })
  },

  // 获取今日秒杀
  getToday(): Promise<Result<unknown[]>> {
    return http.get('/seckill/today')
  },

  // 获取秒杀时段
  getTimeSlots(): Promise<Result<unknown[]>> {
    return http.get('/seckill/time-slots')
  },

  // 设置秒杀提醒
  setReminder(activityId: number): Promise<Result<void>> {
    return http.post(`/seckill/${activityId}/reminder`)
  },

  // 取消秒杀提醒
  cancelReminder(activityId: number): Promise<Result<void>> {
    return http.delete(`/seckill/${activityId}/reminder`)
  }
}