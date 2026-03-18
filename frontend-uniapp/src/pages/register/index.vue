<template>
  <view class="register-container">
    <view class="header">
      <text class="title">注册账号</text>
      <text class="subtitle">加入 FNUSALE 开启校园交易之旅</text>
    </view>

    <view class="form-container">
      <!-- 注册方式切换 -->
      <view class="register-tabs">
        <view
          :class="['tab-item', { active: registerType === 'phone' }]"
          @click="registerType = 'phone'"
        >
          手机号注册
        </view>
        <view
          :class="['tab-item', { active: registerType === 'email' }]"
          @click="registerType = 'email'"
        >
          邮箱注册
        </view>
      </view>

      <!-- 表单 -->
      <view class="form">
        <uni-easyinput
          v-model="username"
          placeholder="请输入用户名"
          type="text"
        />

        <uni-easyinput
          v-if="registerType === 'phone'"
          v-model="phone"
          placeholder="请输入手机号"
          type="number"
          :maxlength="11"
        />

        <uni-easyinput
          v-else
          v-model="email"
          placeholder="请输入邮箱"
          type="text"
        />

        <uni-easyinput
          v-model="password"
          placeholder="请输入密码（6-20位）"
          type="password"
        />

        <uni-easyinput
          v-model="confirmPassword"
          placeholder="请确认密码"
          type="password"
        />

        <!-- 身份类型选择 -->
        <view class="identity-selector">
          <text class="label">身份类型</text>
          <view class="identity-options">
            <view
              :class="['identity-option', { active: identityType === 'STUDENT' }]"
              @click="identityType = 'STUDENT'"
            >
              学生
            </view>
            <view
              :class="['identity-option', { active: identityType === 'TEACHER' }]"
              @click="identityType = 'TEACHER'"
            >
              教师
            </view>
          </view>
        </view>
      </view>

      <!-- 注册按钮 -->
      <button class="btn-register" :disabled="loading" @click="handleRegister">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <!-- 登录入口 -->
      <view class="login-link" @click="goToLogin">
        <text>已有账号？</text>
        <text class="link">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const registerType = ref<'phone' | 'email'>('phone')
const username = ref('')
const phone = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const identityType = ref<'STUDENT' | 'TEACHER'>('STUDENT')
const loading = ref(false)

const handleRegister = async () => {
  // 表单验证
  if (!username.value) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }

  if (registerType.value === 'phone' && !phone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }

  if (registerType.value === 'email' && !email.value) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }

  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '密码长度至少6位', icon: 'none' })
    return
  }

  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return
  }

  loading.value = true
  try {
    let res
    if (registerType.value === 'phone') {
      res = await userStore.registerByPhone(username.value, phone.value, password.value, identityType.value)
    } else {
      res = await userStore.registerByEmail(username.value, email.value, password.value, identityType.value)
    }

    if (res.code === 200) {
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateTo({ url: '/pages/login/index' })
      }, 1000)
    }
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(180deg, $color-primary-light 0%, $color-bg-primary 100%);
}

.header {
  padding: 80rpx 0 48rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: $text-2xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.form-container {
  margin: 0 48rpx;
  padding: 48rpx;
  background: $color-white;
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;
}

.register-tabs {
  display: flex;
  margin-bottom: 48rpx;
  border-bottom: 2rpx solid $color-border;
}

.tab-item {
  flex: 1;
  padding: 24rpx 0;
  text-align: center;
  font-size: $text-base;
  color: $color-text-secondary;
  position: relative;
}

.tab-item.active {
  color: $color-primary;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: $color-primary;
  border-radius: 2rpx;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.identity-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.label {
  font-size: $text-base;
  color: $color-text-primary;
}

.identity-options {
  display: flex;
  gap: 24rpx;
}

.identity-option {
  padding: 16rpx 32rpx;
  border: 2rpx solid $color-border;
  border-radius: $radius-md;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.identity-option.active {
  border-color: $color-primary;
  color: $color-primary;
  background: $color-primary-light;
}

.btn-register {
  margin-top: 48rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-lg;
  font-weight: 500;
  height: 96rpx;
  line-height: 96rpx;
}

.btn-register[disabled] {
  opacity: 0.6;
}

.login-link {
  margin-top: 32rpx;
  text-align: center;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.link {
  color: $color-primary;
  margin-left: 8rpx;
}
</style>