import type { Result } from '@/types/common'

// API 基础路径 - 开发环境指向本地网关
const BASE_URL = 'http://localhost:8080/api'

// 请求配置
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown> | unknown[] | unknown
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
}

// 响应数据
interface ResponseData {
  code: number
  message: string
  data: any
}

// Token 刷新状态
let isRefreshing = false
let refreshPromise: Promise<string> | null = null

/**
 * 获取存储的 Token
 */
function getToken(): string | null {
  return uni.getStorageSync('accessToken') || null
}

/**
 * 获取刷新 Token
 */
function getRefreshToken(): string | null {
  return uni.getStorageSync('refreshToken') || null
}

/**
 * 设置 Token
 */
function setToken(accessToken: string, refreshToken: string): void {
  uni.setStorageSync('accessToken', accessToken)
  uni.setStorageSync('refreshToken', refreshToken)
}

/**
 * 清除 Token
 */
function clearToken(): void {
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}

/**
 * 刷新 Token
 */
async function refreshAccessToken(): Promise<string> {
  if (isRefreshing && refreshPromise) {
    return refreshPromise
  }

  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token')
  }

  isRefreshing = true
  refreshPromise = new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/refresh-token`,
      method: 'POST',
      data: { refreshToken },
      success: (res) => {
        const data = res.data as ResponseData
        if (data.code === 200 && data.data) {
          const { accessToken, refreshToken: newRefreshToken } = data.data
          setToken(accessToken, newRefreshToken)
          resolve(accessToken)
        } else {
          clearToken()
          reject(new Error('Token refresh failed'))
        }
      },
      fail: (err) => {
        clearToken()
        reject(err)
      },
      complete: () => {
        isRefreshing = false
        refreshPromise = null
      }
    })
  })

  return refreshPromise
}

/**
 * 显示错误提示
 */
function showErrorToast(message: string): void {
  uni.showToast({
    title: message || '请求失败',
    icon: 'none',
    duration: 2000
  })
}

/**
 * 跳转到登录页
 */
function navigateToLogin(): void {
  clearToken()
  uni.reLaunch({ url: '/pages/login/index' })
}

/**
 * 通用请求方法
 */
async function request<T>(config: RequestConfig): Promise<Result<T>> {
  const { url, method = 'GET', data, header = {}, showLoading = false, showError = true } = config

  // 显示加载提示
  if (showLoading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  // 添加 Token
  const token = getToken()
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  header['Content-Type'] = 'application/json'

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data: data as any,
      header,
      success: async (res) => {
        if (showLoading) {
          uni.hideLoading()
        }

        const responseData = res.data as ResponseData

        // HTTP 状态码判断
        if (res.statusCode === 401) {
          // Token 过期，尝试刷新
          try {
            const newToken = await refreshAccessToken()
            // 重试请求
            header['Authorization'] = `Bearer ${newToken}`
            uni.request({
              url: `${BASE_URL}${url}`,
              method,
              data: data as any,
              header,
              success: (retryRes) => {
                const retryData = retryRes.data as ResponseData
                if (retryData.code === 200 || retryData.code === 0) {
                  resolve(retryData as Result<T>)
                } else {
                  if (showError) showErrorToast(retryData.message)
                  reject(new Error(retryData.message))
                }
              },
              fail: reject
            })
          } catch {
            navigateToLogin()
            reject(new Error('登录已过期'))
          }
          return
        }

        if (res.statusCode === 403) {
          if (showError) showErrorToast('没有权限访问')
          reject(new Error('没有权限访问'))
          return
        }

        if (res.statusCode === 404) {
          if (showError) showErrorToast('请求资源不存在')
          reject(new Error('请求资源不存在'))
          return
        }

        if (res.statusCode >= 500) {
          if (showError) showErrorToast('服务器错误')
          reject(new Error('服务器错误'))
          return
        }

        // 业务状态码判断
        if (responseData.code === 200 || responseData.code === 0) {
          resolve(responseData as Result<T>)
        } else {
          if (showError) showErrorToast(responseData.message)
          reject(new Error(responseData.message))
        }
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        if (showError) showErrorToast('网络异常')
        reject(err)
      }
    })
  })
}

/**
 * HTTP 请求封装
 */
export const http = {
  get<T = unknown>(url: string, data?: Record<string, unknown> | unknown, showLoading = false): Promise<Result<T>> {
    return request<T>({ url, method: 'GET', data: data as Record<string, unknown>, showLoading })
  },

  post<T = unknown>(url: string, data?: Record<string, unknown> | unknown[] | unknown, showLoading = false): Promise<Result<T>> {
    return request<T>({ url, method: 'POST', data, showLoading })
  },

  put<T = unknown>(url: string, data?: Record<string, unknown> | unknown[] | unknown, showLoading = false): Promise<Result<T>> {
    return request<T>({ url, method: 'PUT', data, showLoading })
  },

  delete<T = unknown>(url: string, data?: Record<string, unknown> | unknown[] | unknown, showLoading = false): Promise<Result<T>> {
    return request<T>({ url, method: 'DELETE', data, showLoading })
  },

  upload<T = unknown>(url: string, file: { file: any }, showLoading = false): Promise<Result<T>> {
    return new Promise((resolve, reject) => {
      const token = getToken()
      const header: Record<string, string> = {}
      if (token) {
        header['Authorization'] = `Bearer ${token}`
      }

      uni.uploadFile({
        url: `${BASE_URL}${url}`,
        filePath: file.file.tempFilePath || file.file.path,
        name: 'file',
        header,
        success: async (res) => {
          if (showLoading) {
            uni.hideLoading()
          }

          try {
            const responseData = JSON.parse(res.data) as ResponseData
            if (responseData.code === 200 || responseData.code === 0) {
              resolve(responseData as Result<T>)
            } else {
              if (showLoading) showErrorToast(responseData.message)
              reject(new Error(responseData.message))
            }
          } catch (e) {
            reject(new Error('响应解析失败'))
          }
        },
        fail: (err) => {
          if (showLoading) {
            uni.hideLoading()
          }
          showErrorToast('上传失败')
          reject(err)
        }
      })
    })
  }
}

// 导出 Token 操作方法
export { getToken, setToken, clearToken }