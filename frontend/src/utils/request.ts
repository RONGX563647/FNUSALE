import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { Result } from '@/types/common'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const { data } = response
    // 业务状态码判断
    if (data.code === 200 || data.code === 0) {
      return data as unknown as AxiosResponse
    }
    // 业务错误处理
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  async (error) => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401: {
          // Token 过期，尝试刷新
          const refreshToken = localStorage.getItem('refreshToken')
          if (refreshToken) {
            try {
              const res = await axios.post('/api/user/refresh-token', null, {
                params: { refreshToken }
              })
              const { accessToken, refreshToken: newRefreshToken } = res.data.data
              localStorage.setItem('accessToken', accessToken)
              localStorage.setItem('refreshToken', newRefreshToken)
              // 重试原请求
              response.config.headers.Authorization = `Bearer ${accessToken}`
              return request(response.config)
            } catch {
              // 刷新失败，清除 token 并跳转登录
              localStorage.removeItem('accessToken')
              localStorage.removeItem('refreshToken')
              window.location.href = '/login'
            }
          } else {
            window.location.href = '/login'
          }
          break
        }
        case 403:
          error.message = '没有权限访问'
          break
        case 404:
          error.message = '请求资源不存在'
          break
        case 500:
          error.message = '服务器错误'
          break
        default:
          error.message = response.data?.message || '请求失败'
      }
    } else if (error.code === 'ECONNABORTED') {
      error.message = '请求超时'
    } else {
      error.message = '网络异常'
    }
    return Promise.reject(error)
  }
)

// 封装请求方法
export const http = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    return request.get(url, config)
  },

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<Result<T>> {
    return request.post(url, data, config)
  },

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<Result<T>> {
    return request.put(url, data, config)
  },

  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    return request.delete(url, config)
  },

  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<Result<T>> {
    return request.patch(url, data, config)
  }
}

export default request