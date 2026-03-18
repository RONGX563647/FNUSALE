<template>
  <view class="my-coupon-page">
    <!-- 状态切换 -->
    <view class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['tab-item', { active: activeStatus === tab.value }]"
        @click="activeStatus = tab.value"
      >
        {{ tab.label }}
      </view>
    </view>

    <view class="coupon-list" v-if="coupons.length > 0">
      <view
        v-for="coupon in coupons"
        :key="coupon.id"
        :class="['coupon-item', { disabled: coupon.couponStatus === 'EXPIRED' || coupon.couponStatus === 'USED' }]"
      >
        <view class="coupon-left">
          <text class="coupon-amount">¥{{ coupon.reduceAmount }}</text>
          <text class="coupon-condition">满{{ coupon.fullAmount }}可用</text>
        </view>
        <view class="coupon-right">
          <text class="coupon-name">{{ coupon.couponName }}</text>
          <text class="coupon-type">{{ getCouponTypeText(coupon.couponType) }}</text>
          <text class="coupon-time">有效期至 {{ coupon.expireTime }}</text>
        </view>
        <view class="coupon-status">
          <text class="status-text">{{ getStatusText(coupon.couponStatus) }}</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <Empty description="暂无优惠券" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { couponApi } from '@/api'
import type { UserCoupon, CouponStatus } from '@/types/marketing'

const statusTabs = [
  { label: '未使用', value: 'UNUSED' },
  { label: '已使用', value: 'USED' },
  { label: '已过期', value: 'EXPIRED' }
]

const activeStatus = ref<CouponStatus>('UNUSED')
const coupons = ref<UserCoupon[]>([])

onMounted(() => {
  fetchCoupons()
})

watch(activeStatus, () => {
  fetchCoupons()
})

const fetchCoupons = async () => {
  try {
    const res = await couponApi.getMy(activeStatus.value)
    if (res.code === 200 && res.data) {
      coupons.value = res.data
    }
  } catch (error) {
    console.error('获取优惠券列表失败', error)
  }
}

const getCouponTypeText = (type: string) => {
  switch (type) {
    case 'FULL_REDUCE':
      return '满减券'
    case 'DIRECT_REDUCE':
      return '直减券'
    case 'CATEGORY':
      return '品类券'
    default:
      return '优惠券'
  }
}

const getStatusText = (status: CouponStatus) => {
  switch (status) {
    case 'UNUSED':
      return '立即使用'
    case 'USED':
      return '已使用'
    case 'EXPIRED':
      return '已过期'
    default:
      return ''
  }
}
</script>

<style lang="scss" scoped>
.my-coupon-page {
  min-height: 100vh;
  background: $color-bg-primary;
}

.status-tabs {
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

.coupon-list {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.coupon-item {
  display: flex;
  background: $color-white;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.coupon-item.disabled {
  opacity: 0.6;
}

.coupon-left {
  width: 200rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.coupon-amount {
  font-size: 56rpx;
  font-weight: 700;
  color: $color-white;
}

.coupon-condition {
  font-size: $text-xs;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8rpx;
}

.coupon-right {
  flex: 1;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
  margin-bottom: 8rpx;
}

.coupon-type {
  font-size: $text-xs;
  color: $color-text-secondary;
  margin-bottom: 8rpx;
}

.coupon-time {
  font-size: $text-xs;
  color: $color-text-placeholder;
}

.coupon-status {
  width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-text {
  font-size: $text-sm;
  color: $color-primary;
  font-weight: 500;
}

.empty-state {
  padding: 200rpx 0;
}
</style>