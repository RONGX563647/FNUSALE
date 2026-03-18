<template>
  <view class="login-container">
    <view class="header">
      <text class="title">FNUSALE</text>
      <text class="subtitle">校园二手交易平台</text>
    </view>

    <view class="form-container">
      <!-- 登录方式切换 -->
      <view class="login-tabs">
        <view
          :class="['tab-item', { active: loginType === 'phone' }]"
          @click="loginType = 'phone'"
        >
          手机号登录
        </view>
        <view
          :class="['tab-item', { active: loginType === 'email' }]"
          @click="loginType = 'email'"
        >
          邮箱登录
        </view>
      </view>

      <!-- 手机号登录表单 -->
      <view v-if="loginType === 'phone'" class="form">
        <uni-easyinput
          v-model="phone"
          placeholder="请输入手机号"
          type="number"
          :maxlength="11"
        />
        <uni-easyinput
          v-model="password"
          placeholder="请输入密码"
          type="password"
        />
      </view>

      <!-- 邮箱登录表单 -->
      <view v-else class="form">
        <uni-easyinput
          v-model="email"
          placeholder="请输入邮箱"
          type="text"
        />
        <uni-easyinput
          v-model="password"
          placeholder="请输入密码"
          type="password"
        />
      </view>

      <!-- 登录按钮 -->
      <button class="btn-login" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <!-- 注册入口 -->
      <view class="register-link" @click="goToRegister">
        <text>还没有账号？</text>
        <text class="link">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loginType = ref<'phone' | 'email'>('phone')
const phone = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (loginType.value === 'phone') {
    if (!phone.value || !password.value) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
  } else {
    if (!email.value || !password.value) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
  }

  loading.value = true
  try {
    let res
    if (loginType.value === 'phone') {
      res = await userStore.loginByPhone(phone.value, password.value)
    } else {
      res = await userStore.loginByEmail(email.value, password.value)
    }

    if (res.code === 200) {
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/user/center' })
      }, 1000)
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, $color-primary-light 0%, $color-bg-primary 100%);
}

.header {
  padding: 120rpx 0 80rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: 64rpx;
  font-weight: 700;
  color: $color-primary;
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  font-size: $text-base;
  color: $color-text-secondary;
}

.form-container {
  margin: 0 48rpx;
  padding: 48rpx;
  background: $color-white;
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;
}

.login-tabs {
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

.btn-login {
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

.btn-login[disabled] {
  opacity: 0.6;
}

.register-link {
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