import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import type { Result } from '@/types/api'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data

    // 业务错误处理
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401 未授权
      if (res.code === 401) {
        ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          removeToken()
          window.location.href = '/login'
        })
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return response
  },
  (error) => {
    console.error('Response error:', error)

    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          removeToken()
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装请求方法
export const http = {
  get<T = unknown>(url: string, params?: object): Promise<AxiosResponse<Result<T>>> {
    return service.get(url, { params })
  },

  post<T = unknown>(url: string, data?: object): Promise<AxiosResponse<Result<T>>> {
    return service.post(url, data)
  },

  put<T = unknown>(url: string, data?: object, config?: object): Promise<AxiosResponse<Result<T>>> {
    return service.put(url, data, config)
  },

  delete<T = unknown>(url: string, params?: object): Promise<AxiosResponse<Result<T>>> {
    return service.delete(url, { params })
  }
}

export default service