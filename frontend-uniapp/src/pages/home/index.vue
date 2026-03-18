<template>
  <view class="home-container">
    <!-- 头部 -->
    <view class="header">
      <text class="title">校园二手交易平台</text>
      <text class="subtitle">欢迎来到 FNUSALE</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <uni-search-bar
        v-model="searchKeyword"
        placeholder="搜索商品"
        @confirm="onSearch"
      />
    </view>

    <!-- 营销入口 -->
    <view class="marketing-entry">
      <view class="entry-card coupon" @click="goToCoupon">
        <uni-icons type="staff" size="24" color="#fff" />
        <view class="entry-info">
          <text class="entry-title">优惠券</text>
          <text class="entry-desc">领取专属优惠</text>
        </view>
        <uni-icons type="forward" size="18" color="#fff" />
      </view>

      <view class="entry-card seckill" @click="goToSeckill">
        <uni-icons type="flash" size="24" color="#fff" />
        <view class="entry-info">
          <text class="entry-title">今日秒杀</text>
          <text class="entry-desc">限时抢购</text>
        </view>
        <uni-icons type="forward" size="18" color="#fff" />
      </view>
    </view>

    <!-- 热门商品 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门商品</text>
      </view>

      <view v-if="products.length > 0" class="product-list">
        <view
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToProduct(product.id)"
        >
          <image
            class="product-image"
            :src="product.images?.[0] || '/static/default-product.png'"
            mode="aspectFill"
          />
          <view class="product-info">
            <text class="product-name">{{ product.title }}</text>
            <text class="product-desc">{{ product.description }}</text>
            <view class="product-price">
              <text class="price">¥{{ product.price }}</text>
            </view>
          </view>
        </view>
      </view>

      <Empty v-else description="暂无商品" />
    </view>

    <!-- 自定义 TabBar -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { productApi } from '@/api'

const searchKeyword = ref('')
const products = ref<any[]>([])

const onSearch = () => {
  uni.showToast({ title: `搜索: ${searchKeyword.value}`, icon: 'none' })
}

const goToCoupon = () => {
  uni.navigateTo({ url: '/pages/coupon/list' })
}

const goToSeckill = () => {
  uni.navigateTo({ url: '/pages/seckill/list' })
}

const goToProduct = (id: number) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

const fetchProducts = async () => {
  try {
    const res = await productApi.getPage({ pageNum: 1, pageSize: 10, status: 'ON_SHELF' })
    if (res.code === 200 && res.data) {
      products.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取商品列表失败', error)
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background: $color-bg-primary;
  padding-bottom: 150rpx;
}

.header {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  padding: 60rpx 40rpx;
  text-align: center;
}

.title {
  display: block;
  color: $color-white;
  font-size: $text-2xl;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  color: rgba(255, 255, 255, 0.9);
  font-size: $text-sm;
}

.search-bar {
  padding: 24rpx 32rpx;
  background: $color-white;
}

.marketing-entry {
  display: flex;
  gap: 24rpx;
  padding: 32rpx;
  background: $color-white;
}

.entry-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-radius: $radius-lg;
}

.entry-card.coupon {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
}

.entry-card.seckill {
  background: linear-gradient(135deg, #facc14 0%, #f5a623 100%);
}

.entry-info {
  flex: 1;
  margin-left: 24rpx;
}

.entry-title {
  display: block;
  font-size: $text-base;
  font-weight: 600;
  color: $color-white;
  margin-bottom: 8rpx;
}

.entry-desc {
  display: block;
  font-size: $text-xs;
  color: rgba(255, 255, 255, 0.9);
}

.section {
  padding: 32rpx;
}

.section-header {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: $text-lg;
  font-weight: 600;
  color: $color-text-primary;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.product-card {
  background: $color-white;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.product-image {
  width: 100%;
  height: 280rpx;
}

.product-info {
  padding: 24rpx;
}

.product-name {
  display: block;
  font-size: $text-sm;
  font-weight: 500;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.product-desc {
  display: block;
  font-size: $text-xs;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.product-price {
  display: flex;
  align-items: baseline;
}

.price {
  font-size: $text-lg;
  font-weight: 600;
  color: $color-error;
}
</style>