<template>
  <view class="address-page">
    <view class="address-list" v-if="addresses.length > 0">
      <view
        class="address-item"
        v-for="address in addresses"
        :key="address.id"
        @click="selectAddress(address)"
      >
        <view class="address-content">
          <view class="address-header">
            <text class="address-type">
              {{ address.addressType === 'PICK_POINT' ? '自提点' : '自定义地址' }}
            </text>
            <view v-if="address.isDefault" class="default-tag">默认</view>
          </view>
          <text class="address-detail">
            {{ address.addressType === 'PICK_POINT' ? address.pickPointName : address.customAddress }}
          </text>
        </view>
        <view class="address-actions">
          <uni-icons type="compose" size="20" color="#6366F1" @click.stop="editAddress(address)" />
          <uni-icons type="trash" size="20" color="#EF4444" @click.stop="deleteAddress(address.id)" />
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <Empty description="暂无地址" />
    </view>

    <view class="bottom-bar">
      <button class="btn-add" @click="addAddress">添加地址</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { addressApi } from '@/api'
import type { UserAddressVO } from '@/types/user'

const addresses = ref<UserAddressVO[]>([])

onShow(() => {
  fetchAddresses()
})

const fetchAddresses = async () => {
  try {
    const res = await addressApi.getList()
    if (res.code === 200 && res.data) {
      addresses.value = res.data
    }
  } catch (error) {
    console.error('获取地址列表失败', error)
  }
}

const selectAddress = (address: UserAddressVO) => {
  // TODO: 设置默认地址
}

const editAddress = (address: UserAddressVO) => {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

const deleteAddress = (id: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await addressApi.delete(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchAddresses()
        } catch (error) {
          console.error('删除失败', error)
        }
      }
    }
  })
}

const addAddress = () => {
  uni.showToast({ title: '添加地址功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.address-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding-bottom: 160rpx;
}

.address-list {
  padding: 32rpx;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: $color-white;
  border-radius: $radius-lg;
  margin-bottom: 24rpx;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.address-type {
  font-size: $text-base;
  font-weight: 500;
  color: $color-text-primary;
}

.default-tag {
  margin-left: 16rpx;
  padding: 4rpx 12rpx;
  background: $color-primary-light;
  color: $color-primary;
  font-size: $text-xs;
  border-radius: $radius-sm;
}

.address-detail {
  display: block;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.address-actions {
  display: flex;
  gap: 32rpx;
}

.empty-state {
  padding: 200rpx 0;
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

.btn-add {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 88rpx;
  line-height: 88rpx;
}
</style>