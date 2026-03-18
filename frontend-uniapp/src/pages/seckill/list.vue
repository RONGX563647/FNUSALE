<template>
  <view class="seckill-page">
    <!-- 时段选择 -->
    <view class="time-slots">
      <view
        v-for="slot in timeSlots"
        :key="slot.timeSlot"
        :class="['slot-item', { active: activeSlot === slot.timeSlot }]"
        @click="activeSlot = slot.timeSlot"
      >
        <text class="slot-time">{{ slot.timeSlot }}</text>
        <text class="slot-status">{{ getSlotStatus(slot.timeSlot) }}</text>
      </view>
    </view>

    <!-- 秒杀商品列表 -->
    <view class="seckill-list" v-if="currentActivities.length > 0">
      <view
        v-for="activity in currentActivities"
        :key="activity.id"
        class="seckill-item"
        @click="goToDetail(activity.id)"
      >
        <image
          class="product-image"
          :src="activity.productImage || '/static/default-product.png'"
          mode="aspectFill"
        />
        <view class="product-info">
          <text class="product-name">{{ activity.productName || activity.activityName }}</text>
          <view class="price-row">
            <text class="seckill-price">¥{{ activity.seckillPrice }}</text>
            <text class="original-price">¥{{ activity.originalPrice }}</text>
          </view>
          <view class="stock-bar">
            <view class="stock-progress" :style="{ width: getStockPercent(activity) + '%' }" />
          </view>
          <text class="stock-text">已抢{{ Math.round((1 - activity.remainStock / activity.totalStock) * 100) }}%</text>
        </view>
        <view class="action-btn" @click.stop="joinSeckill(activity)">
          <text class="action-text">{{ getActionText(activity.activityStatus) }}</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <Empty description="暂无秒杀活动" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { seckillApi } from '@/api'
import type { SeckillActivity, TodaySeckill } from '@/types/marketing'

const timeSlots = ref<TodaySeckill[]>([])
const activeSlot = ref('')
const allActivities = ref<SeckillActivity[]>([])

const currentActivities = computed(() => {
  const slot = timeSlots.value.find(s => s.timeSlot === activeSlot.value)
  return slot?.activities || []
})

onMounted(() => {
  fetchTodaySeckill()
})

const fetchTodaySeckill = async () => {
  try {
    const res = await seckillApi.getToday()
    if (res.code === 200 && res.data) {
      timeSlots.value = res.data
      if (res.data.length > 0) {
        activeSlot.value = res.data[0].timeSlot
        // 收集所有活动
        res.data.forEach(slot => {
          allActivities.value.push(...slot.activities)
        })
      }
    }
  } catch (error) {
    console.error('获取秒杀活动失败', error)
  }
}

const getSlotStatus = (timeSlot: string) => {
  const now = new Date()
  const [hour] = timeSlot.split(':').map(Number)
  const slotTime = new Date()
  slotTime.setHours(hour, 0, 0, 0)

  if (now < slotTime) {
    return '即将开始'
  } else if (now.getHours() === hour) {
    return '抢购中'
  } else {
    return '已结束'
  }
}

const getStockPercent = (activity: SeckillActivity) => {
  return Math.round((1 - activity.remainStock / activity.totalStock) * 100)
}

const getActionText = (status: string) => {
  switch (status) {
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

const goToDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/seckill/detail?id=${id}` })
}

const joinSeckill = (activity: SeckillActivity) => {
  if (activity.activityStatus === 'NOT_START') {
    uni.showToast({ title: '活动尚未开始', icon: 'none' })
    return
  }
  if (activity.activityStatus === 'END') {
    uni.showToast({ title: '活动已结束', icon: 'none' })
    return
  }
  goToDetail(activity.id)
}
</script>

<style lang="scss" scoped>
.seckill-page {
  min-height: 100vh;
  background: $color-bg-primary;
}

.time-slots {
  display: flex;
  background: $color-white;
  padding: 16rpx 32rpx;
  gap: 16rpx;
  overflow-x: auto;
}

.slot-item {
  flex-shrink: 0;
  padding: 16rpx 32rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
  text-align: center;
}

.slot-item.active {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
}

.slot-time {
  display: block;
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
}

.slot-item.active .slot-time {
  color: $color-white;
}

.slot-status {
  display: block;
  font-size: $text-xs;
  color: $color-text-secondary;
  margin-top: 4rpx;
}

.slot-item.active .slot-status {
  color: rgba(255, 255, 255, 0.9);
}

.seckill-list {
  padding: 32rpx;
}

.seckill-item {
  display: flex;
  background: $color-white;
  border-radius: $radius-lg;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.product-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: $radius-md;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-name {
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.seckill-price {
  font-size: $text-xl;
  font-weight: 700;
  color: $color-error;
}

.original-price {
  margin-left: 16rpx;
  font-size: $text-sm;
  color: $color-text-placeholder;
  text-decoration: line-through;
}

.stock-bar {
  height: 8rpx;
  background: $color-error-light;
  border-radius: 4rpx;
  margin-bottom: 8rpx;
}

.stock-progress {
  height: 100%;
  background: $color-error;
  border-radius: 4rpx;
}

.stock-text {
  font-size: $text-xs;
  color: $color-text-secondary;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24rpx;
}

.action-text {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, $color-error 0%, #ff6b6b 100%);
  color: $color-white;
  font-size: $text-sm;
  border-radius: $radius-full;
}

.empty-state {
  padding: 200rpx 0;
}
</style>