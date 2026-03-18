import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO, LoginVO } from '@/types/user'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<UserVO | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isVerified = computed(() => userInfo.value?.authStatus === 'APPROVED')

  // Actions
  async function login(phone: string, password: string) {
    const res = await userApi.login({ phone, password })
    if (res.data) {
      setToken(res.data)
      await fetchUserInfo()
    }
    return res
  }

  async function register(phone: string, password: string, nickname: string, code: string) {
    const res = await userApi.register({ phone, password, nickname, code })
    return res
  }

  async function logout() {
    try {
      await userApi.logout()
    } finally {
      clearToken()
    }
  }

  async function fetchUserInfo() {
    const res = await userApi.getCurrentUserInfo()
    if (res.data) {
      userInfo.value = res.data
    }
    return res
  }

  function setToken(data: LoginVO) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }

  function clearToken() {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return {
    // State
    token,
    refreshToken,
    userInfo,
    // Getters
    isLoggedIn,
    isVerified,
    // Actions
    login,
    register,
    logout,
    fetchUserInfo,
    setToken,
    clearToken
  }
})