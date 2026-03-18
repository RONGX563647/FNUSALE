<template>
  <view class="checkin-page">
    <!-- 签到状态卡片 -->
    <view class="status-card">
      <view class="points-info">
        <text class="points-label">我的积分</text>
        <text class="points-value">{{ points.availablePoints || 0 }}</text>
      </view>
      <view class="sign-info">
        <view class="sign-item">
          <text class="sign-value">{{ signStatus.continuousDays || 0 }}</text>
          <text class="sign-label">连续签到</text>
        </view>
        <view class="sign-item">
          <text class="sign-value">{{ signStatus.totalDays || 0 }}</text>
          <text class="sign-label">累计签到</text>
        </view>
      </view>
    </view>

    <!-- 签到按钮 -->
    <view class="checkin-section">
      <view
        :class="['checkin-btn', { checked: signStatus.hasSigned }]"
        @click="handleCheckin"
      >
        <uni-icons
          :type="signStatus.hasSigned ? 'checkbox-filled' : 'calendar'"
          size="48"
          :color="signStatus.hasSigned ? '#10B981' : '#fff'"
        />
        <text class="checkin-text">
          {{ signStatus.hasSigned ? '今日已签到' : '立即签到' }}
        </text>
      </view>
      <text class="reward-tip" v-if="!signStatus.hasSigned">
        今日签到可获得 {{ signStatus.todayReward || 10 }} 积分
      </text>
    </view>

    <!-- 签到日历 -->
    <view class="calendar-section">
      <text class="section-title">签到日历</text>
      <view class="calendar">
        <view class="calendar-header">
          <text v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day" class="week-day">
            {{ day }}
          </text>
        </view>
        <view class="calendar-body">
          <view
            v-for="(date, index) in calendarDates"
            :key="index"
            :class="['calendar-item', { checked: isSigned(date), today: isToday(date) }]"
          >
            <text v-if="date" class="date-text">{{ date }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { signApi, pointsApi } from '@/api'
import type { SignStatusVO, UserPointsVO } from '@/types/user'

const signStatus = ref<SignStatusVO>({
  hasSigned: false,
  continuousDays: 0,
  totalDays: 0,
  todayReward: 10,
  nextRewardDays: 7,
  nextRewardPoints: 50
})

const points = ref<UserPointsVO>({
  totalPoints: 0,
  availablePoints: 0,
  usedPoints: 0,
  continuousSignDays: 0,
  totalSignDays: 0
})

const signedDates = ref<string[]>([])

const calendarDates = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()

  const dates: (number | null)[] = []
  for (let i = 0; i < firstDay; i++) {
    dates.push(null)
  }
  for (let i = 1; i <= daysInMonth; i++) {
    dates.push(i)
  }
  return dates
})

onMounted(() => {
  fetchSignStatus()
  fetchPoints()
  fetchCalendar()
})

const fetchSignStatus = async () => {
  try {
    const res = await signApi.getStatus()
    if (res.code === 200 && res.data) {
      signStatus.value = res.data
    }
  } catch (error) {
    console.error('获取签到状态失败', error)
  }
}

const fetchPoints = async () => {
  try {
    const res = await pointsApi.getMyPoints()
    if (res.code === 200 && res.data) {
      points.value = res.data
    }
  } catch (error) {
    console.error('获取积分失败', error)
  }
}

const fetchCalendar = async () => {
  try {
    const now = new Date()
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    const res = await signApi.getCalendar(month)
    if (res.code === 200 && res.data) {
      signedDates.value = res.data
    }
  } catch (error) {
    console.error('获取签到日历失败', error)
  }
}

const handleCheckin = async () => {
  if (signStatus.value.hasSigned) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }

  try {
    const res = await signApi.sign()
    if (res.code === 200 && res.data) {
      uni.showToast({
        title: `签到成功，获得 ${res.data.rewardPoints} 积分`,
        icon: 'success'
      })
      fetchSignStatus()
      fetchPoints()
      fetchCalendar()
    }
  } catch (error) {
    console.error('签到失败', error)
  }
}

const isSigned = (date: number | null) => {
  if (!date) return false
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`
  return signedDates.value.includes(dateStr)
}

const isToday = (date: number | null) => {
  if (!date) return false
  return date === new Date().getDate()
}
</script>

<style lang="scss" scoped>
.checkin-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
}

.status-card {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  border-radius: $radius-xl;
  padding: 48rpx 32rpx;
  color: $color-white;
}

.points-info {
  text-align: center;
  margin-bottom: 32rpx;
}

.points-label {
  display: block;
  font-size: $text-sm;
  opacity: 0.9;
}

.points-value {
  display: block;
  font-size: 72rpx;
  font-weight: 700;
  margin-top: 8rpx;
}

.sign-info {
  display: flex;
  justify-content: center;
  gap: 96rpx;
}

.sign-item {
  text-align: center;
}

.sign-value {
  display: block;
  font-size: $text-2xl;
  font-weight: 600;
}

.sign-label {
  display: block;
  font-size: $text-xs;
  opacity: 0.9;
  margin-top: 8rpx;
}

.checkin-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64rpx 0;
}

.checkin-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  box-shadow: 0 8rpx 32rpx rgba(99, 102, 241, 0.4);
}

.checkin-btn.checked {
  background: $color-success-light;
  box-shadow: none;
}

.checkin-text {
  margin-top: 16rpx;
  font-size: $text-base;
  font-weight: 500;
  color: $color-white;
}

.checkin-btn.checked .checkin-text {
  color: $color-success;
}

.reward-tip {
  margin-top: 24rpx;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.calendar-section {
  background: $color-white;
  border-radius: $radius-xl;
  padding: 32rpx;
}

.section-title {
  display: block;
  font-size: $text-lg;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 24rpx;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 16rpx;
}

.week-day {
  text-align: center;
  font-size: $text-xs;
  color: $color-text-secondary;
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8rpx;
}

.calendar-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.calendar-item.today {
  background: $color-primary-light;
}

.calendar-item.checked {
  background: $color-success;
}

.date-text {
  font-size: $text-sm;
  color: $color-text-primary;
}

.calendar-item.checked .date-text {
  color: $color-white;
}
</style>