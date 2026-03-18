import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

// 格式化日期时间
export function formatDateTime(date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化日期
export function formatDate(date: string | Date): string {
  return formatDateTime(date, 'YYYY-MM-DD')
}

// 格式化时间
export function formatTime(date: string | Date): string {
  return formatDateTime(date, 'HH:mm:ss')
}

// 相对时间
export function formatRelativeTime(date: string | Date): string {
  if (!date) return ''
  const now = dayjs()
  const target = dayjs(date)
  const diffMinutes = now.diff(target, 'minute')
  const diffHours = now.diff(target, 'hour')
  const diffDays = now.diff(target, 'day')

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return formatDate(date)
}

// 格式化金额
export function formatMoney(amount: number, decimals = 2): string {
  if (amount === null || amount === undefined) return '0.00'
  return amount.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化数字（千分位）
export function formatNumber(num: number): string {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化百分比
export function formatPercent(value: number, decimals = 1): string {
  if (value === null || value === undefined) return '0%'
  return `${value.toFixed(decimals)}%`
}

// 手机号脱敏
export function maskPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

// 身份证/学号脱敏
export function maskId(id: string): string {
  if (!id || id.length < 8) return id
  return `${id.slice(0, 4)}****${id.slice(-4)}`
}

// 认证状态文本
export function authStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    UNAUTH: '未认证',
    UNDER_REVIEW: '审核中',
    AUTH_SUCCESS: '认证成功',
    AUTH_FAILED: '认证失败'
  }
  return statusMap[status] || status
}

// 用户状态文本
export function userStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    NORMAL: '正常',
    BANNED: '已封禁'
  }
  return statusMap[status] || status
}

// 纠纷类型文本
export function disputeTypeText(type: string): string {
  const typeMap: Record<string, string> = {
    PRODUCT_NOT_MATCH: '商品不符',
    NO_DELIVERY: '未发货',
    PRODUCT_DAMAGED: '商品损坏',
    OTHER: '其他'
  }
  return typeMap[type] || type
}

// 纠纷状态文本
export function disputeStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决'
  }
  return statusMap[status] || status
}

// 处理结果文本
export function processResultText(result: string): string {
  const resultMap: Record<string, string> = {
    BUYER_WIN: '买家胜诉',
    SELLER_WIN: '卖家胜诉',
    NEGOTIATE: '协商解决'
  }
  return resultMap[result] || result
}

// 身份类型文本
export function identityTypeText(type: string): string {
  const typeMap: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教职工'
  }
  return typeMap[type] || type
}