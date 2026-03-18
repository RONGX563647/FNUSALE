<template>
  <view class="empty-container">
    <view class="empty-icon">
      <uni-icons type="info" size="48" color="#94A3B8" />
    </view>
    <text class="empty-text">{{ description }}</text>
  </view>
</template>

<script setup lang="ts">
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  }
})
</script>

<style lang="scss" scoped>
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
}

.empty-icon {
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #94A3B8;
}
</style>