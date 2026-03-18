<template>
  <view class="coupon-detail-page" v-if="coupon">
    <!-- 优惠券信息 -->
    <view class="coupon-card">
      <view class="coupon-header">
        <text class="coupon-amount">¥{{ coupon.reduceAmount }}</text>
        <text class="coupon-condition">满{{ coupon.fullAmount }}可用</text>
      </view>
      <view class="coupon-body">
        <text class="coupon-name">{{ coupon.couponName }}</text>
        <text class="coupon-type">{{ getCouponTypeText(coupon.couponType) }}</text>
      </view>
    </view>

    <!-- 使用规则 -->
    <view class="rules-section">
      <text class="section-title">使用规则</text>
      <view class="rules-list">
        <view class="rule-item">
          <text class="rule-label">有效期</text>
          <text class="rule-value">{{ coupon.startTime }} 至 {{ coupon.endTime }}</text>
        </view>
        <view class="rule-item" v-if="coupon.categoryName">
          <text class="rule-label">适用品类</text>
          <text class="rule-value">{{ coupon.categoryName }}</text>
        </view>
        <view class="rule-item">
          <text class="rule-label">剩余数量</text>
          <text class="rule-value">{{ coupon.remainCount }} / {{ coupon.totalCount }}</text>
        </view>
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="description-section">
      <text class="section-title">使用说明</text>
      <text class="description-text">
        1. 优惠券仅限本平台使用\n
        2. 每笔订单只能使用一张优惠券\n
        3. 优惠券不可兑换现金\n
        4. 优惠券过期作废
      </text>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <button
        class="btn-receive"
        :disabled="coupon.received"
        @click="receiveCoupon"
      >
        {{ coupon.received ? '已领取' : '立即领取' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { couponApi } from '@/api'
import type { Coupon } from '@/types/marketing'

const couponId = ref<number>(0)
const coupon = ref<Coupon | null>(null)

onLoad((options) => {
  if (options?.id) {
    couponId.value = parseInt(options.id)
    fetchCouponDetail()
  }
})

const fetchCouponDetail = async () => {
  try {
    const res = await couponApi.getById(couponId.value)
    if (res.code === 200 && res.data) {
      coupon.value = res.data
    }
  } catch (error) {
    console.error('获取优惠券详情失败', error)
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

const receiveCoupon = async () => {
  if (!coupon.value || coupon.value.received) return

  try {
    const res = await couponApi.receive(coupon.value.id)
    if (res.code === 200) {
      uni.showToast({ title: '领取成功', icon: 'success' })
      if (coupon.value) {
        coupon.value.received = true
      }
    }
  } catch (error) {
    console.error('领取失败', error)
  }
}
</script>

<style lang="scss" scoped>
.coupon-detail-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
  padding-bottom: 160rpx;
}

.coupon-card {
  background: $color-white;
  border-radius: $radius-xl;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.coupon-header {
  padding: 48rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  text-align: center;
}

.coupon-amount {
  display: block;
  font-size: 96rpx;
  font-weight: 700;
  color: $color-white;
}

.coupon-condition {
  display: block;
  font-size: $text-base;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 16rpx;
}

.coupon-body {
  padding: 32rpx;
  text-align: center;
}

.coupon-name {
  display: block;
  font-size: $text-xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 8rpx;
}

.coupon-type {
  font-size: $text-sm;
  color: $color-text-secondary;
}

.rules-section,
.description-section {
  background: $color-white;
  border-radius: $radius-xl;
  padding: 32rpx;
  margin-bottom: 32rpx;
}

.section-title {
  display: block;
  font-size: $text-lg;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 24rpx;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.rule-item {
  display: flex;
  justify-content: space-between;
}

.rule-label {
  font-size: $text-sm;
  color: $color-text-secondary;
}

.rule-value {
  font-size: $text-sm;
  color: $color-text-primary;
}

.description-text {
  font-size: $text-sm;
  color: $color-text-secondary;
  line-height: 1.8;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background: $color-white;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.btn-receive {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 88rpx;
  line-height: 88rpx;
}

.btn-receive[disabled] {
  opacity: 0.6;
}
</style>