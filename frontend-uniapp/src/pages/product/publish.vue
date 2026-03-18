<template>
  <view class="publish-page">
    <view class="form-section">
      <!-- 商品标题 -->
      <view class="form-item">
        <text class="label">商品标题</text>
        <input
          class="input"
          v-model="formData.title"
          placeholder="请输入商品标题"
          maxlength="50"
        />
      </view>

      <!-- 商品描述 -->
      <view class="form-item">
        <text class="label">商品描述</text>
        <textarea
          class="textarea"
          v-model="formData.description"
          placeholder="请详细描述商品信息"
          maxlength="500"
        />
      </view>

      <!-- 商品图片 -->
      <view class="form-item">
        <text class="label">商品图片</text>
        <view class="image-upload">
          <view
            v-for="(image, index) in formData.images"
            :key="index"
            class="image-item"
          >
            <image class="preview-image" :src="image" mode="aspectFill" />
            <view class="remove-btn" @click="removeImage(index)">
              <uni-icons type="closeempty" size="16" color="#fff" />
            </view>
          </view>
          <view class="upload-btn" @click="chooseImage" v-if="formData.images.length < 9">
            <uni-icons type="plusempty" size="32" color="#94A3B8" />
            <text class="upload-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 商品品类 -->
      <view class="form-item" @click="showCategoryPicker = true">
        <text class="label">商品品类</text>
        <view class="picker-value">
          <text :class="{ placeholder: !formData.categoryId }">
            {{ selectedCategoryName || '请选择品类' }}
          </text>
          <uni-icons type="forward" size="16" color="#94A3B8" />
        </view>
      </view>

      <!-- 价格 -->
      <view class="form-item">
        <text class="label">出售价格</text>
        <view class="price-input">
          <text class="currency">¥</text>
          <input
            class="input"
            v-model="formData.price"
            type="digit"
            placeholder="0.00"
          />
        </view>
      </view>

      <!-- 原价 -->
      <view class="form-item">
        <text class="label">原价（选填）</text>
        <view class="price-input">
          <text class="currency">¥</text>
          <input
            class="input"
            v-model="formData.originalPrice"
            type="digit"
            placeholder="0.00"
          />
        </view>
      </view>

      <!-- 新旧程度 -->
      <view class="form-item">
        <text class="label">新旧程度</text>
        <view class="degree-options">
          <view
            v-for="degree in degreeOptions"
            :key="degree.value"
            :class="['degree-option', { active: formData.newDegree === degree.value }]"
            @click="formData.newDegree = degree.value"
          >
            {{ degree.label }}
          </view>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="btn-draft" @click="saveDraft">存草稿</button>
      <button class="btn-submit" @click="submitProduct">发布商品</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { productApi } from '@/api'

const formData = ref({
  title: '',
  description: '',
  images: [] as string[],
  categoryId: 0,
  price: '',
  originalPrice: '',
  newDegree: 'GOOD'
})

const showCategoryPicker = ref(false)
const categories = ref<any[]>([])

const degreeOptions = [
  { label: '全新', value: 'NEW' },
  { label: '几乎全新', value: 'ALMOST_NEW' },
  { label: '轻微使用', value: 'LIGHTLY_USED' },
  { label: '正常使用', value: 'GOOD' },
  { label: '有明显痕迹', value: 'FAIR' }
]

const selectedCategoryName = computed(() => {
  // TODO: 根据选中的品类ID获取品类名称
  return ''
})

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - formData.value.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.value.images.push(...res.tempFilePaths)
    }
  })
}

const removeImage = (index: number) => {
  formData.value.images.splice(index, 1)
}

const saveDraft = async () => {
  if (!formData.value.title) {
    uni.showToast({ title: '请输入商品标题', icon: 'none' })
    return
  }

  try {
    const res = await productApi.saveDraft({
      title: formData.value.title,
      description: formData.value.description,
      images: formData.value.images,
      categoryId: formData.value.categoryId,
      price: parseFloat(formData.value.price) || 0,
      originalPrice: parseFloat(formData.value.originalPrice) || undefined,
      newDegree: formData.value.newDegree
    })
    if (res.code === 200) {
      uni.showToast({ title: '已保存草稿', icon: 'success' })
    }
  } catch (error) {
    console.error('保存草稿失败', error)
  }
}

const submitProduct = async () => {
  if (!formData.value.title) {
    uni.showToast({ title: '请输入商品标题', icon: 'none' })
    return
  }

  if (!formData.value.categoryId) {
    uni.showToast({ title: '请选择商品品类', icon: 'none' })
    return
  }

  if (!formData.value.price) {
    uni.showToast({ title: '请输入出售价格', icon: 'none' })
    return
  }

  if (formData.value.images.length === 0) {
    uni.showToast({ title: '请至少上传一张商品图片', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '发布中...' })
    const res = await productApi.publish({
      title: formData.value.title,
      description: formData.value.description,
      images: formData.value.images,
      categoryId: formData.value.categoryId,
      price: parseFloat(formData.value.price),
      originalPrice: parseFloat(formData.value.originalPrice) || undefined,
      newDegree: formData.value.newDegree
    })
    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    }
  } catch (error) {
    uni.hideLoading()
    console.error('发布失败', error)
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
  padding-bottom: 200rpx;
}

.form-section {
  background: $color-white;
  border-radius: $radius-xl;
  overflow: hidden;
}

.form-item {
  padding: 32rpx;
  border-bottom: 1rpx solid $color-border-light;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  display: block;
  font-size: $text-base;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  padding: 24rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
  font-size: $text-base;
}

.textarea {
  width: 100%;
  height: 200rpx;
  padding: 24rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
  font-size: $text-base;
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.preview-image {
  width: 100%;
  height: 100%;
  border-radius: $radius-md;
}

.remove-btn {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  background: $color-error;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 200rpx;
  height: 200rpx;
  background: $color-bg-tertiary;
  border: 2rpx dashed $color-border;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-text {
  margin-top: 8rpx;
  font-size: $text-xs;
  color: $color-text-placeholder;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
}

.picker-value .placeholder {
  color: $color-text-placeholder;
}

.price-input {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
}

.currency {
  font-size: $text-lg;
  color: $color-text-primary;
  margin-right: 8rpx;
}

.price-input .input {
  padding: 0;
  background: transparent;
}

.degree-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.degree-option {
  padding: 16rpx 32rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.degree-option.active {
  background: $color-primary-light;
  color: $color-primary;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background: $color-white;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.btn-draft {
  flex: 1;
  background: $color-white;
  color: $color-primary;
  border: 2rpx solid $color-primary;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 88rpx;
  line-height: 88rpx;
}

.btn-submit {
  flex: 2;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 88rpx;
  line-height: 88rpx;
}
</style>