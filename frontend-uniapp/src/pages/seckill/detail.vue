<template>
  <view class="seckill-detail-page" v-if="activity">
    <!-- 商品图片 -->
    <image
      class="product-image"
      :src="activity.productImage || '/static/default-product.png'"
      mode="aspectFill"
    />

    <!-- 倒计时 -->
    <view class="countdown-section" v-if="activity.activityStatus === 'ON_GOING'">
      <text class="countdown-label">距结束</text>
      <view class="countdown-timer">
        <text class="time-block">{{ countdown.hours }}</text>
        <text class="time-sep">:</text>
        <text class="time-block">{{ countdown.minutes }}</text>
        <text class="time-sep">:</text>
        <text class="time-block">{{ countdown.seconds }}</text>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="product-info">
      <text class="product-name">{{ activity.productName || activity.activityName }}</text>
      <view class="price-row">
        <text class="seckill-price">¥{{ activity.seckillPrice }}</text>
        <text class="original-price">¥{{ activity.originalPrice }}</text>
      </view>
      <view class="stock-info">
        <text class="stock-text">库存: {{ activity.remainStock }}/{{ activity.totalStock }}</text>
      </view>
    </view>

    <!-- 活动规则 -->
    <view class="rules-section">
      <text class="section-title">活动规则</text>
      <text class="rules-text">
        1. 活动时间: {{ activity.startTime }} 至 {{ activity.endTime }}\n
        2. 每人限购一件\n
        3. 秒杀商品不支持退换\n
        4. 请在规定时间内完成支付
      </text>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="price-display">
        <text class="price-label">秒杀价</text>
        <text class="price-value">¥{{ activity.seckillPrice }}</text>
      </view>
      <button
        class="btn-seckill"
        :disabled="activity.activityStatus !== 'ON_GOING' || activity.remainStock <= 0"
        @click="joinSeckill"
      >
        {{ getButtonText() }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { seckillApi } from '@/api'
import type { SeckillActivity } from '@/types/marketing'

const activityId = ref<number>(0)
const activity = ref<SeckillActivity | null>(null)

const countdown = reactive({
  hours: '00',
  minutes: '00',
  seconds: '00'
})

let timer: number | null = null

onLoad((options) => {
  if (options?.id) {
    activityId.value = parseInt(options.id)
    fetchActivityDetail()
  }
})

onUnload(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const fetchActivityDetail = async () => {
  try {
    const res = await seckillApi.getActivityDetail(activityId.value)
    if (res.code === 200 && res.data) {
      activity.value = res.data
      if (res.data.activityStatus === 'ON_GOING') {
        startCountdown()
      }
    }
  } catch (error) {
    console.error('获取秒杀详情失败', error)
  }
}

const startCountdown = () => {
  const updateCountdown = () => {
    if (!activity.value) return

    const endTime = new Date(activity.value.endTime).getTime()
    const now = Date.now()
    const diff = endTime - now

    if (diff <= 0) {
      countdown.hours = '00'
      countdown.minutes = '00'
      countdown.seconds = '00'
      if (timer) {
        clearInterval(timer)
      }
      return
    }

    const hours = Math.floor(diff / 3600000)
    const minutes = Math.floor((diff % 3600000) / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)

    countdown.hours = String(hours).padStart(2, '0')
    countdown.minutes = String(minutes).padStart(2, '0')
    countdown.seconds = String(seconds).padStart(2, '0')
  }

  updateCountdown()
  timer = setInterval(updateCountdown, 1000) as unknown as number
}

const getButtonText = () => {
  if (!activity.value) return ''

  if (activity.value.remainStock <= 0) {
    return '已抢光'
  }

  switch (activity.value.activityStatus) {
    case 'NOT_START':
      return '即将开始'
    case 'ON_GOING':
      return '立即抢购'
    case 'END':
      return '已结束'
    default:
      return '立即抢购'
  }
}

const joinSeckill = async () => {
  if (!activity.value || activity.value.activityStatus !== 'ON_GOING') {
    return
  }

  try {
    uni.showLoading({ title: '正在抢购...' })
    const res = await seckillApi.join(activity.value.id)
    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({ title: '抢购成功', icon: 'success' })
      // TODO: 跳转到订单页面
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '抢购失败，请重试', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.seckill-detail-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding-bottom: 160rpx;
}

.product-image {
  width: 100%;
  height: 600rpx;
}

.countdown-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: linear-gradient(135deg, $color-error 0%, #ff6b6b 100%);
}

.countdown-label {
  font-size: $text-base;
  color: $color-white;
  margin-right: 16rpx;
}

.countdown-timer {
  display: flex;
  align-items: center;
}

.time-block {
  padding: 8rpx 16rpx;
  background: $color-white;
  color: $color-error;
  font-size: $text-base;
  font-weight: 600;
  border-radius: $radius-sm;
}

.time-sep {
  margin: 0 8rpx;
  color: $color-white;
  font-weight: 600;
}

.product-info {
  padding: 32rpx;
  background: $color-white;
}

.product-name {
  display: block;
  font-size: $text-xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.seckill-price {
  font-size: 56rpx;
  font-weight: 700;
  color: $color-error;
}

.original-price {
  margin-left: 16rpx;
  font-size: $text-base;
  color: $color-text-placeholder;
  text-decoration: line-through;
}

.stock-info {
  padding: 16rpx;
  background: $color-error-light;
  border-radius: $radius-md;
}

.stock-text {
  font-size: $text-sm;
  color: $color-error;
}

.rules-section {
  margin: 32rpx;
  padding: 32rpx;
  background: $color-white;
  border-radius: $radius-xl;
}

.section-title {
  display: block;
  font-size: $text-lg;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 24rpx;
}

.rules-text {
  font-size: $text-sm;
  color: $color-text-secondary;
  line-height: 2;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 16rpx 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background: $color-white;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.price-display {
  flex: 1;
}

.price-label {
  display: block;
  font-size: $text-xs;
  color: $color-text-secondary;
}

.price-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: $color-error;
}

.btn-seckill {
  background: linear-gradient(135deg, $color-error 0%, #ff6b6b 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  font-weight: 500;
  padding: 0 64rpx;
  height: 88rpx;
  line-height: 88rpx;
}

.btn-seckill[disabled] {
  opacity: 0.6;
}
</style>