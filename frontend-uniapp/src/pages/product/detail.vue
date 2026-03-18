<template>
  <view class="product-detail">
    <!-- 商品图片轮播 -->
    <swiper
      class="product-swiper"
      :indicator-dots="product.images?.length > 1"
      :autoplay="true"
      :circular="true"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#fff"
    >
      <swiper-item v-for="(image, index) in product.images" :key="index">
        <image class="product-image" :src="image" mode="aspectFill" @click="previewImage(index)" />
      </swiper-item>
    </swiper>

    <!-- 商品信息 -->
    <view class="product-info">
      <view class="price-row">
        <text class="price">¥{{ product.price }}</text>
        <text v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</text>
      </view>

      <text class="title">{{ product.title }}</text>
      <text class="description">{{ product.description }}</text>

      <view class="tags">
        <text class="tag">{{ product.categoryName }}</text>
        <text class="tag">{{ product.newDegree }}</text>
      </view>
    </view>

    <!-- 卖家信息 -->
    <view class="seller-card" v-if="product.sellerId">
      <view class="seller-info">
        <image
          class="seller-avatar"
          :src="product.sellerAvatar || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="seller-detail">
          <text class="seller-name">{{ product.sellerName }}</text>
          <text class="seller-credit">信誉分: {{ sellerCredit }}</text>
        </view>
      </view>
      <button class="btn-contact" @click="contactSeller">联系卖家</button>
    </view>

    <!-- 自提点信息 -->
    <view class="pick-point-card" v-if="product.pickPointName">
      <uni-icons type="location" size="20" color="#6366F1" />
      <text class="pick-point-name">自提点: {{ product.pickPointName }}</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="actions">
        <view class="action-item" @click="toggleFavorite">
          <uni-icons
            :type="product.isFavorite ? 'star-filled' : 'star'"
            size="24"
            :color="product.isFavorite ? '#F59E0B' : '#64748B'"
          />
          <text class="action-text">{{ product.isFavorite ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="action-item" @click="toggleLike">
          <uni-icons
            :type="product.isLiked ? 'heart-filled' : 'heart'"
            size="24"
            :color="product.isLiked ? '#EF4444' : '#64748B'"
          />
          <text class="action-text">{{ product.likeCount || 0 }}</text>
        </view>
      </view>
      <button class="btn-buy" @click="buyProduct">立即购买</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { productApi } from '@/api'
import type { ProductVO } from '@/types/product'

const productId = ref<number>(0)
const product = ref<Partial<ProductVO>>({})
const sellerCredit = ref(100)

onLoad((options) => {
  if (options?.id) {
    productId.value = parseInt(options.id)
    fetchProductDetail()
  }
})

const fetchProductDetail = async () => {
  try {
    const res = await productApi.getById(productId.value)
    if (res.code === 200 && res.data) {
      product.value = res.data
    }
  } catch (error) {
    console.error('获取商品详情失败', error)
  }
}

const previewImage = (index: number) => {
  uni.previewImage({
    urls: product.value.images || [],
    current: index
  })
}

const toggleFavorite = async () => {
  try {
    if (product.value.isFavorite) {
      await productApi.removeFavorite(productId.value)
      product.value.isFavorite = false
    } else {
      await productApi.addFavorite(productId.value)
      product.value.isFavorite = true
    }
    uni.showToast({ title: product.value.isFavorite ? '已收藏' : '已取消收藏', icon: 'success' })
  } catch (error) {
    console.error('操作失败', error)
  }
}

const toggleLike = async () => {
  try {
    if (product.value.isLiked) {
      await productApi.removeLike(productId.value)
      product.value.isLiked = false
      product.value.likeCount = (product.value.likeCount || 1) - 1
    } else {
      await productApi.addLike(productId.value)
      product.value.isLiked = true
      product.value.likeCount = (product.value.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('操作失败', error)
  }
}

const contactSeller = () => {
  uni.showToast({ title: 'IM功能开发中', icon: 'none' })
}

const buyProduct = () => {
  uni.showToast({ title: '购买功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.product-detail {
  padding-bottom: 160rpx;
}

.product-swiper {
  width: 100%;
  height: 750rpx;
}

.product-image {
  width: 100%;
  height: 100%;
}

.product-info {
  padding: 32rpx;
  background: $color-white;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.price {
  font-size: 48rpx;
  font-weight: 700;
  color: $color-error;
}

.original-price {
  margin-left: 16rpx;
  font-size: $text-base;
  color: $color-text-secondary;
  text-decoration: line-through;
}

.title {
  display: block;
  font-size: $text-xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.description {
  display: block;
  font-size: $text-sm;
  color: $color-text-secondary;
  line-height: 1.6;
  margin-bottom: 24rpx;
}

.tags {
  display: flex;
  gap: 16rpx;
}

.tag {
  padding: 8rpx 16rpx;
  background: $color-primary-light;
  color: $color-primary;
  font-size: $text-xs;
  border-radius: $radius-sm;
}

.seller-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24rpx;
  padding: 32rpx;
  background: $color-white;
}

.seller-info {
  display: flex;
  align-items: center;
}

.seller-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.seller-detail {
  margin-left: 24rpx;
}

.seller-name {
  display: block;
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
}

.seller-credit {
  display: block;
  font-size: $text-xs;
  color: $color-text-secondary;
  margin-top: 8rpx;
}

.btn-contact {
  background: $color-primary-light;
  color: $color-primary;
  border: none;
  border-radius: $radius-md;
  font-size: $text-sm;
  padding: 16rpx 32rpx;
  height: auto;
  line-height: normal;
}

.pick-point-card {
  display: flex;
  align-items: center;
  margin-top: 24rpx;
  padding: 32rpx;
  background: $color-white;
}

.pick-point-name {
  margin-left: 16rpx;
  font-size: $text-sm;
  color: $color-text-primary;
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

.actions {
  display: flex;
  gap: 48rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-text {
  font-size: $text-xs;
  color: $color-text-secondary;
  margin-top: 4rpx;
}

.btn-buy {
  flex: 1;
  margin-left: 48rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  font-weight: 500;
  height: 88rpx;
  line-height: 88rpx;
}
</style>