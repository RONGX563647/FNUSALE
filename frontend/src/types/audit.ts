// 待审核商品
export interface PendingProduct {
  productId: number
  productName: string
  price: number
  categoryName: string
  publisherId: number
  publisherName: string
  publishTime: string
  mainImageUrl: string
  productDesc: string
}

// 审核记录
export interface AuditRecord {
  id: number
  productId: number
  adminId: number
  adminName: string
  auditResult: 'PASS' | 'REJECT'
  rejectReason?: string
  auditTime: string
}

// 审核统计
export interface AuditStatistics {
  pendingCount: number
  todayPassCount: number
  todayRejectCount: number
  totalPassCount: number
  totalRejectCount: number
}

// 批量审核请求
export interface BatchAuditRequest {
  productIds: number[]
}

// 审核操作结果
export interface BatchAuditResult {
  successCount: number
  failCount: number
}