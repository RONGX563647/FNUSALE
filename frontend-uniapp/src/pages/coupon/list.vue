<template>
  <view class="coupon-list-page">
    <view class="coupon-list" v-if="coupons.length > 0">
      <view
        v-for="coupon in coupons"
        :key="coupon.id"
        class="coupon-item"
        @click="goToDetail(coupon.id)"
      >
        <view class="coupon-left">
          <text class="coupon-amount">¥{{ coupon.reduceAmount }}</text>
          <text class="coupon-condition">满{{ coupon.fullAmount }}可用</text>
        </view>
        <view class="coupon-right">
          <text class="coupon-name">{{ coupon.couponName }}</text>
          <text class="coupon-type">{{ getCouponTypeText(coupon.couponType) }}</text>
          <text class="coupon-time">{{ coupon.startTime }} - {{ coupon.endTime }}</text>
        </view>
        <view class="coupon-action" @click.stop="receiveCoupon(coupon)">
          <text class="action-text">{{ coupon.received ? '已领取' : '领取' }}</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <Empty description="暂无可领取优惠券" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { couponApi } from '@/api'
import type { Coupon } from '@/types/marketing'

const coupons = ref<Coupon[]>([])

onMounted(() => {
  fetchCoupons()
})

const fetchCoupons = async () => {
  try {
    const res = await couponApi.getAvailable()
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

const goToDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/coupon/detail?id=${id}` })
}

const receiveCoupon = async (coupon: Coupon) => {
  if (coupon.received) {
    uni.showToast({ title: '已领取过此优惠券', icon: 'none' })
    return
  }

  try {
    const res = await couponApi.receive(coupon.id)
    if (res.code === 200) {
      uni.showToast({ title: '领取成功', icon: 'success' })
      coupon.received = true
      coupon.receivedCount++
    }
  } catch (error) {
    console.error('领取失败', error)
  }
}
</script>

<style lang="scss" scoped>
.coupon-list-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
}

.coupon-list {
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

.coupon-action {
  width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-primary-light;
}

.action-text {
  font-size: $text-sm;
  color: $color-primary;
  font-weight: 500;
}

.empty-state {
  padding: 200rpx 0;
}
</style>