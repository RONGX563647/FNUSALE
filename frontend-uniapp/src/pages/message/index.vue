<template>
  <view class="message-page">
    <view class="empty-state" v-if="sessions.length === 0">
      <Empty description="暂无消息" />
    </view>

    <view class="session-list" v-else>
      <view
        class="session-item"
        v-for="session in sessions"
        :key="session.id"
        @click="goToChat(session.id)"
      >
        <view class="avatar-wrapper">
          <image
            class="avatar"
            :src="session.targetUserAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <view v-if="session.unreadCount > 0" class="unread-badge">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </view>
        </view>

        <view class="session-content">
          <view class="session-header">
            <text class="target-name">{{ session.targetUserName }}</text>
            <text class="last-time">{{ formatTime(session.lastMessageTime) }}</text>
          </view>
          <view class="product-info">
            <image
              v-if="session.productImage"
              class="product-thumb"
              :src="session.productImage"
              mode="aspectFill"
            />
            <text class="product-title">{{ session.productTitle }}</text>
          </view>
          <text class="last-message">{{ session.lastMessage }}</text>
        </view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { sessionApi } from '@/api'
import type { ImSessionVO } from '@/types/im'

const sessions = ref<ImSessionVO[]>([])

onShow(() => {
  fetchSessions()
})

const fetchSessions = async () => {
  try {
    const res = await sessionApi.getList()
    if (res.code === 200 && res.data) {
      sessions.value = res.data
    }
  } catch (error) {
    console.error('获取会话列表失败', error)
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}/${date.getDate()}`
}

const goToChat = (sessionId: number) => {
  uni.showToast({ title: '聊天功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.message-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding-bottom: 150rpx;
}

.empty-state {
  padding: 200rpx 0;
}

.session-list {
  background: $color-white;
}

.session-item {
  display: flex;
  padding: 32rpx;
  border-bottom: 1rpx solid $color-border-light;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
}

.unread-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $color-error;
  color: $color-white;
  font-size: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.session-content {
  flex: 1;
  margin-left: 24rpx;
  overflow: hidden;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.target-name {
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
}

.last-time {
  font-size: $text-xs;
  color: $color-text-placeholder;
}

.product-info {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.product-thumb {
  width: 48rpx;
  height: 48rpx;
  border-radius: $radius-sm;
  margin-right: 16rpx;
}

.product-title {
  font-size: $text-xs;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-message {
  display: block;
  font-size: $text-sm;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>