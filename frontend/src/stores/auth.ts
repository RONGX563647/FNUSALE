import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AdminInfo, LoginRequest, LoginResponse } from '@/types/auth'
import { setToken, removeToken, getToken, isAuthenticated } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const adminInfo = ref<AdminInfo | null>(null)
  const isLoggedIn = computed(() => isAuthenticated() && !!adminInfo.value)

  // 登录
  async function login(data: LoginRequest): Promise<boolean> {
    try {
      // 临时 mock 数据（后端接口未实现）
      // const res = await authApi.login(data)

      // Mock 响应
      const mockResponse: LoginResponse = {
        accessToken: 'mock_access_token_' + Date.now(),
        refreshToken: 'mock_refresh_token_' + Date.now(),
        expiresIn: 7200,
        adminInfo: {
          id: 1,
          username: data.username,
          nickname: data.username === 'admin' ? '超级管理员' : '运营管理员',
          role: data.username === 'admin' ? 'SUPER_ADMIN' : 'OPERATOR',
          permissions: ['*'],
          createTime: new Date().toISOString()
        }
      }

      const { accessToken, refreshToken, expiresIn, adminInfo: info } = mockResponse
      setToken(accessToken, refreshToken, expiresIn)
      adminInfo.value = info
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('Login failed:', error)
      return false
    }
  }

  // 退出登录
  async function logout(): Promise<void> {
    try {
      // await authApi.logout()
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      removeToken()
      adminInfo.value = null
    }
  }

  // 获取管理员信息
  async function fetchAdminInfo(): Promise<void> {
    if (!getToken()) return

    try {
      // const res = await authApi.getAdminInfo()
      // adminInfo.value = res.data

      // Mock 数据
      adminInfo.value = {
        id: 1,
        username: 'admin',
        nickname: '超级管理员',
        role: 'SUPER_ADMIN',
        permissions: ['*'],
        createTime: '2024-01-01T00:00:00'
      }
    } catch (error) {
      console.error('Fetch admin info failed:', error)
      removeToken()
      adminInfo.value = null
    }
  }

  // 检查权限
  function hasPermission(permission: string): boolean {
    if (!adminInfo.value) return false
    if (adminInfo.value.permissions.includes('*')) return true
    return adminInfo.value.permissions.includes(permission)
  }

  return {
    adminInfo,
    isLoggedIn,
    login,
    logout,
    fetchAdminInfo,
    hasPermission
  }
})