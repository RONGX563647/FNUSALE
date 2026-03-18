<template>
  <view class="leaderboard-page">
    <!-- 排行榜类型切换 -->
    <view class="tab-header">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab-item', { active: activeTab === tab.value }]"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 我的排名 -->
    <view class="my-rank-card" v-if="myRanking">
      <text class="card-title">我的排名</text>
      <view class="rank-info">
        <text class="rank-number">{{ getCurrentRank() }}</text>
        <text class="rank-label">{{ getCurrentRankLabel() }}</text>
      </view>
    </view>

    <!-- 排行榜列表 -->
    <view class="leaderboard-list">
      <view
        v-for="(user, index) in leaderboard"
        :key="user.userId"
        :class="['leaderboard-item', { 'is-me': user.isCurrentUser }]"
      >
        <view class="rank-badge" v-if="index < 3">
          <image
            v-if="index === 0"
            class="rank-icon"
            src="/static/rank-1.png"
            mode="aspectFit"
          />
          <image
            v-else-if="index === 1"
            class="rank-icon"
            src="/static/rank-2.png"
            mode="aspectFit"
          />
          <image
            v-else
            class="rank-icon"
            src="/static/rank-3.png"
            mode="aspectFit"
          />
        </view>
        <view class="rank-number" v-else>
          <text>{{ user.rank }}</text>
        </view>

        <image
          class="user-avatar"
          :src="user.avatarUrl || '/static/default-avatar.png'"
          mode="aspectFill"
        />

        <view class="user-info">
          <text class="user-name">{{ user.username }}</text>
          <view class="user-stats">
            <uni-icons type="star" size="14" color="#F59E0B" />
            <text class="stat-value">{{ user.creditScore }}</text>
          </view>
        </view>

        <view class="user-score">
          <text class="score-value">{{ user.score }}</text>
          <text class="score-label">积分</text>
        </view>
      </view>

      <Empty v-if="leaderboard.length === 0" description="暂无数据" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { rankingApi } from '@/api'
import type { RankingUserVO, MyRankingVO } from '@/types/user'

const tabs = [
  { label: '活跃榜', value: 'ACTIVITY' },
  { label: '信誉榜', value: 'CREDIT' },
  { label: '好评榜', value: 'RATING' }
]

const activeTab = ref('ACTIVITY')
const leaderboard = ref<RankingUserVO[]>([])
const myRanking = ref<MyRankingVO | null>(null)

onMounted(() => {
  fetchLeaderboard()
  fetchMyRanking()
})

watch(activeTab, () => {
  fetchLeaderboard()
})

const fetchLeaderboard = async () => {
  try {
    let res
    if (activeTab.value === 'CREDIT') {
      res = await rankingApi.getCredit()
    } else if (activeTab.value === 'RATING') {
      res = await rankingApi.getRating()
    } else {
      res = await rankingApi.getActivity('daily')
    }
    if (res.code === 200 && res.data) {
      leaderboard.value = res.data
    }
  } catch (error) {
    console.error('获取排行榜失败', error)
  }
}

const fetchMyRanking = async () => {
  try {
    const res = await rankingApi.getMyRanking()
    if (res.code === 200 && res.data) {
      myRanking.value = res.data
    }
  } catch (error) {
    console.error('获取我的排名失败', error)
  }
}

const getCurrentRank = () => {
  if (!myRanking.value) return '-'
  switch (activeTab.value) {
    case 'ACTIVITY':
      return myRanking.value.activity?.inList ? myRanking.value.activity.rank : '未上榜'
    case 'CREDIT':
      return myRanking.value.credit?.inList ? myRanking.value.credit.rank : '未上榜'
    case 'RATING':
      return myRanking.value.rating?.inList ? myRanking.value.rating.rank : '未上榜'
    default:
      return '-'
  }
}

const getCurrentRankLabel = () => {
  switch (activeTab.value) {
    case 'ACTIVITY':
      return '活跃度排名'
    case 'CREDIT':
      return '信誉排名'
    case 'RATING':
      return '好评排名'
    default:
      return ''
  }
}
</script>

<style lang="scss" scoped>
.leaderboard-page {
  min-height: 100vh;
  background: $color-bg-primary;
}

.tab-header {
  display: flex;
  background: $color-white;
  padding: 16rpx 32rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
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
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 4rpx;
  background: $color-primary;
  border-radius: 2rpx;
}

.my-rank-card {
  margin: 32rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  border-radius: $radius-xl;
}

.card-title {
  display: block;
  font-size: $text-sm;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16rpx;
}

.rank-info {
  display: flex;
  align-items: baseline;
}

.rank-number {
  font-size: 48rpx;
  font-weight: 700;
  color: $color-white;
}

.rank-label {
  margin-left: 16rpx;
  font-size: $text-sm;
  color: rgba(255, 255, 255, 0.9);
}

.leaderboard-list {
  padding: 0 32rpx 32rpx;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: $color-white;
  border-radius: $radius-lg;
  margin-bottom: 16rpx;
}

.leaderboard-item.is-me {
  background: $color-primary-light;
  border: 2rpx solid $color-primary;
}

.rank-badge {
  width: 48rpx;
  height: 48rpx;
}

.rank-icon {
  width: 100%;
  height: 100%;
}

.rank-number {
  width: 48rpx;
  text-align: center;
  font-size: $text-base;
  font-weight: 600;
  color: $color-text-primary;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin: 0 24rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
  margin-bottom: 8rpx;
}

.user-stats {
  display: flex;
  align-items: center;
}

.stat-value {
  font-size: $text-xs;
  color: $color-text-secondary;
  margin-left: 8rpx;
}

.user-score {
  text-align: right;
}

.score-value {
  display: block;
  font-size: $text-lg;
  font-weight: 600;
  color: $color-primary;
}

.score-label {
  display: block;
  font-size: $text-xs;
  color: $color-text-secondary;
}
</style>