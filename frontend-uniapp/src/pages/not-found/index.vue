<template>
  <view class="not-found-page">
    <image class="error-image" src="/static/404.png" mode="aspectFit" />
    <text class="error-title">页面不存在</text>
    <text class="error-desc">您访问的页面可能已被删除或暂时无法访问</text>
    <button class="btn-home" @click="goHome">返回首页</button>
  </view>
</template>

<script setup lang="ts">
const goHome = () => {
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style lang="scss" scoped>
.not-found-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 48rpx;
}

.error-image {
  width: 400rpx;
  height: 400rpx;
  margin-bottom: 48rpx;
}

.error-title {
  font-size: $text-2xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.error-desc {
  font-size: $text-base;
  color: $color-text-secondary;
  margin-bottom: 48rpx;
  text-align: center;
}

.btn-home {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  padding: 0 64rpx;
  height: 88rpx;
  line-height: 88rpx;
}
</style>