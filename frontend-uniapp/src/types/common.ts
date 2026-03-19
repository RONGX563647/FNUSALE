// 通用响应类型
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T = unknown> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// 分页请求参数
export interface PageParams {
  pageNum?: number
  pageSize?: number
  [key: string]: unknown
}