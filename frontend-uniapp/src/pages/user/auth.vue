<template>
  <view class="auth-page">
    <view class="auth-status-card" v-if="userStore.userInfo">
      <view class="status-icon">
        <uni-icons
          :type="getStatusIcon()"
          size="48"
          :color="getStatusColor()"
        />
      </view>
      <text class="status-text">{{ getStatusText() }}</text>
      <text class="status-desc" v-if="userStore.userInfo.authResultRemark">
        {{ userStore.userInfo.authResultRemark }}
      </text>
    </view>

    <!-- 认证表单 -->
    <view class="auth-form" v-if="canSubmitAuth">
      <view class="form-section">
        <view class="form-item">
          <text class="label">身份类型</text>
          <view class="identity-options">
            <view
              :class="['identity-option', { active: formData.identityType === 'STUDENT' }]"
              @click="formData.identityType = 'STUDENT'"
            >
              学生
            </view>
            <view
              :class="['identity-option', { active: formData.identityType === 'TEACHER' }]"
              @click="formData.identityType = 'TEACHER'"
            >
              教师
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label">{{ formData.identityType === 'STUDENT' ? '学号' : '工号' }}</text>
          <input
            class="input"
            v-model="formData.studentTeacherId"
            :placeholder="formData.identityType === 'STUDENT' ? '请输入学号' : '请输入工号'"
          />
        </view>

        <view class="form-item upload-item">
          <text class="label">上传证件</text>
          <view class="upload-area" @click="uploadImage">
            <image
              v-if="formData.authImageUrl"
              class="preview-image"
              :src="formData.authImageUrl"
              mode="aspectFill"
            />
            <view v-else class="upload-placeholder">
              <uni-icons type="plusempty" size="32" color="#94A3B8" />
              <text class="upload-text">点击上传学生证/校园卡照片</text>
            </view>
          </view>
        </view>
      </view>

      <button class="btn-submit" @click="submitAuth">提交认证</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const formData = ref({
  identityType: 'STUDENT' as 'STUDENT' | 'TEACHER',
  studentTeacherId: '',
  authImageUrl: ''
})

const canSubmitAuth = computed(() => {
  const status = userStore.userInfo?.authStatus
  return status === 'UNAUTH' || status === 'AUTH_FAILED'
})

const getStatusIcon = () => {
  const status = userStore.userInfo?.authStatus
  switch (status) {
    case 'AUTH_SUCCESS':
      return 'checkbox-filled'
    case 'UNDER_REVIEW':
      return 'info'
    case 'AUTH_FAILED':
      return 'closeempty'
    default:
      return 'info'
  }
}

const getStatusColor = () => {
  const status = userStore.userInfo?.authStatus
  switch (status) {
    case 'AUTH_SUCCESS':
      return '#10B981'
    case 'UNDER_REVIEW':
      return '#F59E0B'
    case 'AUTH_FAILED':
      return '#EF4444'
    default:
      return '#64748B'
  }
}

const getStatusText = () => {
  const status = userStore.userInfo?.authStatus
  switch (status) {
    case 'AUTH_SUCCESS':
      return '认证成功'
    case 'UNDER_REVIEW':
      return '审核中'
    case 'AUTH_FAILED':
      return '认证失败'
    default:
      return '未认证'
  }
}

const uploadImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // TODO: 上传图片
      formData.value.authImageUrl = tempFilePath
      uni.showToast({ title: '图片上传功能开发中', icon: 'none' })
    }
  })
}

const submitAuth = async () => {
  if (!formData.value.studentTeacherId) {
    uni.showToast({ title: '请输入学号/工号', icon: 'none' })
    return
  }

  if (!formData.value.authImageUrl) {
    uni.showToast({ title: '请上传证件照片', icon: 'none' })
    return
  }

  try {
    const res = await userStore.submitAuth({
      studentTeacherId: formData.value.studentTeacherId,
      identityType: formData.value.identityType,
      authImageUrl: formData.value.authImageUrl
    })
    if (res.code === 200) {
      uni.showToast({ title: '提交成功', icon: 'success' })
    }
  } catch (error) {
    console.error('提交失败', error)
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
}

.auth-status-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx;
  background: $color-white;
  border-radius: $radius-xl;
  margin-bottom: 32rpx;
}

.status-icon {
  margin-bottom: 24rpx;
}

.status-text {
  font-size: $text-xl;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.status-desc {
  font-size: $text-sm;
  color: $color-text-secondary;
  text-align: center;
}

.auth-form {
  background: $color-white;
  border-radius: $radius-xl;
  padding: 32rpx;
}

.form-section {
  margin-bottom: 48rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.label {
  display: block;
  font-size: $text-base;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.identity-options {
  display: flex;
  gap: 24rpx;
}

.identity-option {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  border: 2rpx solid $color-border;
  border-radius: $radius-md;
  font-size: $text-base;
  color: $color-text-secondary;
}

.identity-option.active {
  border-color: $color-primary;
  color: $color-primary;
  background: $color-primary-light;
}

.input {
  width: 100%;
  padding: 24rpx;
  background: $color-bg-tertiary;
  border-radius: $radius-md;
  font-size: $text-base;
}

.upload-area {
  width: 100%;
  height: 400rpx;
  border: 2rpx dashed $color-border;
  border-radius: $radius-lg;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.upload-text {
  margin-top: 16rpx;
  font-size: $text-sm;
  color: $color-text-placeholder;
}

.btn-submit {
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 96rpx;
  line-height: 96rpx;
}
</style>