// API 响应通用类型
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  total: number
  list: T[]
}

// 分页参数
export interface PageParams {
  pageNum: number
  pageSize: number
}

// 通用查询参数
export interface QueryParams extends PageParams {
  [key: string]: string | number | undefined
}