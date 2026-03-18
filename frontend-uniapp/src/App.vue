<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/* 全局样式 */
page {
  background-color: $color-bg-primary;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Helvetica Neue', sans-serif;
  font-size: $text-base;
  color: $color-text-primary;
  line-height: 1.5;
}

/* 通用容器 */
.container {
  padding: $spacing-md;
}

/* 卡片样式 */
.card {
  background: $color-white;
  border-radius: $radius-lg;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 按钮样式 */
.btn-primary {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-md;
  padding: $spacing-sm $spacing-md;
  font-size: $text-base;
  font-weight: 500;
}

.btn-primary:active {
  opacity: 0.9;
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>