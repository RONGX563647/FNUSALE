import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO, LoginVO } from '@/types/user'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(uni.getStorageSync('accessToken') || null)
  const refreshToken = ref<string | null>(uni.getStorageSync('refreshToken') || null)
  const userInfo = ref<UserVO | null>(uni.getStorageSync('userInfo') || null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isVerified = computed(() => userInfo.value?.authStatus === 'AUTH_SUCCESS')

  // Actions
  async function loginByPhone(phone: string, password: string) {
    const res = await userApi.loginByPhone(phone, password)
    if (res.data) {
      setToken(res.data)
      await fetchUserInfo()
    }
    return res
  }

  async function loginByEmail(email: string, password: string) {
    const res = await userApi.loginByEmail(email, password)
    if (res.data) {
      setToken(res.data)
      await fetchUserInfo()
    }
    return res
  }

  async function registerByPhone(username: string, phone: string, password: string, identityType?: 'STUDENT' | 'TEACHER') {
    const res = await userApi.registerByPhone({ username, phone, password, identityType })
    return res
  }

  async function registerByEmail(username: string, email: string, password: string, identityType?: 'STUDENT' | 'TEACHER') {
    const res = await userApi.registerByEmail({ username, email, password, identityType })
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
      uni.setStorageSync('userInfo', res.data)
    }
    return res
  }

  async function updateUserInfo(data: any) {
    const res = await userApi.updateUserInfo(data)
    if (res.code === 200) {
      await fetchUserInfo()
    }
    return res
  }

  async function updatePassword(oldPassword: string, newPassword: string) {
    const res = await userApi.updatePassword(oldPassword, newPassword)
    return res
  }

  async function submitAuth(data: any) {
    const res = await userApi.submitAuth(data)
    if (res.code === 200) {
      await fetchUserInfo()
    }
    return res
  }

  function setToken(data: LoginVO) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    uni.setStorageSync('accessToken', data.accessToken)
    uni.setStorageSync('refreshToken', data.refreshToken)
  }

  function clearToken() {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
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
    loginByPhone,
    loginByEmail,
    registerByPhone,
    registerByEmail,
    logout,
    fetchUserInfo,
    updateUserInfo,
    updatePassword,
    submitAuth,
    setToken,
    clearToken
  }
})