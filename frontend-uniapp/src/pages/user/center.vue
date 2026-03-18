<template>
  <view class="user-center">
    <!-- 用户信息卡片 -->
    <view class="user-card" v-if="userStore.isLoggedIn">
      <view class="user-info">
        <image
          class="avatar"
          :src="userStore.userInfo?.avatarUrl || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="info">
          <text class="username">{{ userStore.userInfo?.username || '用户' }}</text>
          <view class="auth-status">
            <uni-icons
              :type="userStore.isVerified ? 'checkbox-filled' : 'info'"
              size="16"
              :color="userStore.isVerified ? '#10B981' : '#F59E0B'"
            />
            <text :class="['status-text', userStore.isVerified ? 'verified' : '']">
              {{ getAuthStatusText() }}
            </text>
          </view>
        </view>
      </view>
      <view class="credit">
        <text class="label">信誉分</text>
        <text class="score">{{ userStore.userInfo?.creditScore || 100 }}</text>
      </view>
    </view>

    <!-- 未登录状态 -->
    <view class="login-prompt" v-else>
      <text class="prompt-text">登录后查看更多功能</text>
      <button class="btn-login" @click="goToLogin">去登录</button>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section" v-if="userStore.isLoggedIn">
      <view class="menu-group">
        <view class="menu-item" @click="goTo('/pages/user/profile')">
          <uni-icons type="person" size="24" color="#6366F1" />
          <text class="menu-text">个人资料</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
        <view class="menu-item" @click="goTo('/pages/user/auth')">
          <uni-icons type="checkbox" size="24" color="#6366F1" />
          <text class="menu-text">身份认证</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
        <view class="menu-item" @click="goTo('/pages/user/address')">
          <uni-icons type="location" size="24" color="#6366F1" />
          <text class="menu-text">地址管理</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @click="goTo('/pages/user/checkin')">
          <uni-icons type="calendar" size="24" color="#6366F1" />
          <text class="menu-text">每日签到</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
        <view class="menu-item" @click="goTo('/pages/user/leaderboard')">
          <uni-icons type="medal" size="24" color="#6366F1" />
          <text class="menu-text">排行榜</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
        <view class="menu-item" @click="goTo('/pages/coupon/my')">
          <uni-icons type="shop" size="24" color="#6366F1" />
          <text class="menu-text">我的优惠券</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @click="goTo('/pages/product/publish')">
          <uni-icons type="plusempty" size="24" color="#6366F1" />
          <text class="menu-text">发布商品</text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section" v-if="userStore.isLoggedIn">
      <button class="btn-logout" @click="handleLogout">退出登录</button>
    </view>

    <!-- 自定义 TabBar -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const getAuthStatusText = () => {
  const status = userStore.userInfo?.authStatus
  switch (status) {
    case 'UNAUTH':
      return '未认证'
    case 'UNDER_REVIEW':
      return '审核中'
    case 'AUTH_SUCCESS':
      return '已认证'
    case 'AUTH_FAILED':
      return '认证失败'
    default:
      return '未认证'
  }
}

const goTo = (url: string) => {
  uni.navigateTo({ url })
}

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const handleLogout = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await userStore.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.user-center {
  min-height: 100vh;
  background: $color-bg-primary;
  padding-bottom: 150rpx;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.info {
  margin-left: 24rpx;
}

.username {
  display: block;
  font-size: $text-xl;
  font-weight: 600;
  color: $color-white;
  margin-bottom: 8rpx;
}

.auth-status {
  display: flex;
  align-items: center;
}

.status-text {
  font-size: $text-sm;
  color: rgba(255, 255, 255, 0.9);
  margin-left: 8rpx;
}

.status-text.verified {
  color: $color-white;
}

.credit {
  text-align: right;
}

.credit .label {
  display: block;
  font-size: $text-xs;
  color: rgba(255, 255, 255, 0.8);
}

.credit .score {
  display: block;
  font-size: $text-2xl;
  font-weight: 600;
  color: $color-white;
}

.login-prompt {
  padding: 80rpx 32rpx;
  text-align: center;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
}

.prompt-text {
  display: block;
  font-size: $text-base;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 32rpx;
}

.btn-login {
  background: $color-white;
  color: $color-primary;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  width: 240rpx;
  height: 80rpx;
  line-height: 80rpx;
}

.menu-section {
  padding: 24rpx 32rpx;
}

.menu-group {
  background: $color-white;
  border-radius: $radius-lg;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid $color-border-light;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-text {
  flex: 1;
  margin-left: 24rpx;
  font-size: $text-base;
  color: $color-text-primary;
}

.logout-section {
  padding: 48rpx 32rpx;
}

.btn-logout {
  background: $color-white;
  color: $color-error;
  border: 2rpx solid $color-error;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 96rpx;
  line-height: 96rpx;
}
</style>