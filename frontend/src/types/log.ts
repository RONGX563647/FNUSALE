// 系统日志
export interface SystemLog {
  logId: number
  operateUserId: number
  operateUsername: string
  moduleName: LogModule
  operateType: OperateType
  operateContent: string
  ipAddress: string
  deviceInfo: string
  exceptionInfo?: string
  logType: LogType
  createTime: string
}

// 日志模块
export type LogModule = 'USER' | 'PRODUCT' | 'ORDER' | 'MARKETING' | 'SYSTEM' | 'DISPUTE'

// 操作类型
export type OperateType = 'ADD' | 'UPDATE' | 'DELETE' | 'QUERY'

// 日志类型
export type LogType = 'OPERATE' | 'EXCEPTION'

// 日志查询参数
export interface LogQueryParams {
  pageNum: number
  pageSize: number
  moduleName?: LogModule
  operateType?: OperateType
  startDate?: string
  endDate?: string
}