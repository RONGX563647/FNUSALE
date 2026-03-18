// 系统配置
export interface SystemConfig {
  configKey: string
  configValue: string
  configDesc: string
  updateTime: string
  adminId?: number
}

// 校园围栏点
export interface FencePoint {
  lng: number
  lat: number
}

// 校园围栏配置
export interface CampusFenceConfig {
  fencePoints: FencePoint[]
  centerPoint?: FencePoint
  radius?: number
}

// 秒杀配置
export interface SeckillConfig {
  qpsLimit: number
  stockPreloadMinutes: number
  maxBuyPerUser: number
}

// 配置更新请求
export interface ConfigUpdateRequest {
  configKey: string
  configValue: string
}

// 批量配置更新
export interface BatchConfigUpdateRequest {
  configs: ConfigUpdateRequest[]
}