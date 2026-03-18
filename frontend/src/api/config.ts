import { http } from '@/utils/request'
import type { Result } from '@/types/api'
import type { SystemConfig, CampusFenceConfig, SeckillConfig, ConfigUpdateRequest } from '@/types/config'

// 系统配置 API
export const configApi = {
  // 获取配置列表
  async getList(): Promise<Result<SystemConfig[]>> {
    const res = await http.get<SystemConfig[]>('/admin/config/list')
    return res.data
  },

  // 获取配置详情
  async getByKey(configKey: string): Promise<Result<SystemConfig>> {
    const res = await http.get<SystemConfig>(`/admin/config/${configKey}`)
    return res.data
  },

  // 更新配置
  async update(configKey: string, configValue: string): Promise<Result<void>> {
    const res = await http.put<void>(`/admin/config/${configKey}`, undefined, { params: { configValue } })
    return res.data
  },

  // 批量更新配置
  async batchUpdate(configs: ConfigUpdateRequest[]): Promise<Result<void>> {
    const res = await http.put<void>('/admin/config/batch', configs)
    return res.data
  },

  // 获取校园围栏配置
  async getCampusFence(): Promise<Result<CampusFenceConfig>> {
    const res = await http.get<CampusFenceConfig>('/admin/config/campus-fence')
    return res.data
  },

  // 更新校园围栏配置
  async updateCampusFence(data: CampusFenceConfig): Promise<Result<void>> {
    const res = await http.put<void>('/admin/config/campus-fence', data)
    return res.data
  },

  // 获取秒杀配置
  async getSeckill(): Promise<Result<SeckillConfig>> {
    const res = await http.get<SeckillConfig>('/admin/config/seckill')
    return res.data
  },

  // 更新秒杀配置
  async updateSeckill(data: SeckillConfig): Promise<Result<void>> {
    const res = await http.put<void>('/admin/config/seckill', data)
    return res.data
  },

  // 刷新缓存
  async refreshCache(): Promise<Result<void>> {
    const res = await http.post<void>('/admin/config/refresh-cache')
    return res.data
  }
}