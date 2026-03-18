// 今日统计数据
export interface TodayStatistics {
  newUserCount: number
  activeUserCount: number
  productPublishCount: number
  orderSuccessCount: number
  orderSuccessAmount: number
  pendingAuditCount: number
  pendingAuthCount: number
  pendingDisputeCount?: number
}

// 趋势数据
export interface TrendData {
  xAxis: string[]
  series: TrendSeries[]
}

// 趋势系列
export interface TrendSeries {
  name: string
  data: number[]
}

// 品类统计
export interface CategoryStatistics {
  categoryId: number
  categoryName: string
  productCount: number
  orderCount: number
  orderAmount: number
  percentage: number
}

// 日期范围统计
export interface RangeStatistics {
  totalUserCount: number
  totalProductCount: number
  totalOrderCount: number
  totalOrderAmount: number
  dailyData: DailyStatistics[]
}

// 每日统计
export interface DailyStatistics {
  date: string
  newUserCount: number
  productPublishCount: number
  orderSuccessCount: number
  orderSuccessAmount: number
}