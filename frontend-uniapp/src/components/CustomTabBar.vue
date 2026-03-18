<template>
  <view class="custom-tabbar safe-area-bottom">
    <view
      v-for="(item, index) in tabList"
      :key="index"
      class="tabbar-item"
      :class="{ active: currentIndex === index }"
      @click="switchTab(index, item)"
    >
      <image
        class="tabbar-icon"
        :src="currentIndex === index ? item.activeIcon : item.icon"
        mode="aspectFit"
      />
      <text class="tabbar-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// Base64 encoded SVG icons
const homeIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`)
const homeActiveIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366F1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`)
const messageIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`)
const messageActiveIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366F1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`)
const userIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`)
const userActiveIcon = 'data:image/svg+xml;base64,' + btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366F1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`)

const currentIndex = ref(0)

const tabList = [
  {
    pagePath: '/pages/home/index',
    text: '首页',
    icon: homeIcon,
    activeIcon: homeActiveIcon
  },
  {
    pagePath: '/pages/message/index',
    text: '消息',
    icon: messageIcon,
    activeIcon: messageActiveIcon
  },
  {
    pagePath: '/pages/user/center',
    text: '我的',
    icon: userIcon,
    activeIcon: userActiveIcon
  }
]

const switchTab = (index: number, item: typeof tabList[0]) => {
  currentIndex.value = index
  uni.switchTab({ url: item.pagePath })
}

const updateCurrentIndex = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentPath = '/' + currentPage.route
  const index = tabList.findIndex(item => item.pagePath === currentPath)
  if (index !== -1) {
    currentIndex.value = index
  }
}

onMounted(() => {
  updateCurrentIndex()
})

defineExpose({
  updateCurrentIndex
})
</script>

<style lang="scss" scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #ffffff;
  border-top: 1rpx solid #e2e8f0;
  z-index: 999;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
  color: #64748b;
  transition: all 0.2s;
}

.tabbar-item.active {
  color: #6366f1;
}

.tabbar-icon {
  width: 48rpx;
  height: 48rpx;
}

.tabbar-text {
  font-size: 24rpx;
  margin-top: 8rpx;
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>