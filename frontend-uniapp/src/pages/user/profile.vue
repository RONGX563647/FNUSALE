<template>
  <view class="profile-page">
    <view class="avatar-section" @click="changeAvatar">
      <image
        class="avatar"
        :src="userStore.userInfo?.avatarUrl || '/static/default-avatar.png'"
        mode="aspectFill"
      />
      <text class="change-text">点击更换头像</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="label">用户名</text>
        <input
          class="input"
          v-model="formData.username"
          placeholder="请输入用户名"
        />
      </view>

      <view class="form-item">
        <text class="label">生日</text>
        <picker mode="date" :value="formData.birthday" @change="onBirthdayChange">
          <view class="picker">
            {{ formData.birthday || '请选择生日' }}
          </view>
        </picker>
      </view>
    </view>

    <button class="btn-save" @click="saveProfile">保存修改</button>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()

const formData = ref({
  username: '',
  birthday: ''
})

const uploading = ref(false)

onMounted(() => {
  if (userStore.userInfo) {
    formData.value.username = userStore.userInfo.username || ''
    formData.value.birthday = userStore.userInfo.birthday || ''
  }
})

const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      
      uploading.value = true
      try {
        const uploadRes = await userApi.uploadAvatar({ tempFilePath })
        if (uploadRes.code === 200 && uploadRes.data) {
          const avatarUrl = uploadRes.data.url
          await userStore.updateUserInfo({ avatarUrl })
          uni.showToast({ title: '头像更新成功', icon: 'success' })
        }
      } catch (error) {
        console.error('上传失败', error)
        uni.showToast({ title: '上传失败，请重试', icon: 'none' })
      } finally {
        uploading.value = false
      }
    }
  })
}

const onBirthdayChange = (e: any) => {
  formData.value.birthday = e.detail.value
}

const saveProfile = async () => {
  if (!formData.value.username) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }

  try {
    const res = await userStore.updateUserInfo({
      username: formData.value.username,
      birthday: formData.value.birthday
    })
    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
    }
  } catch (error) {
    console.error('保存失败', error)
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $color-bg-primary;
  padding: 32rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0;
  background: $color-white;
  border-radius: $radius-xl;
  margin-bottom: 32rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
}

.change-text {
  margin-top: 16rpx;
  font-size: $text-sm;
  color: $color-text-secondary;
}

.form-section {
  background: $color-white;
  border-radius: $radius-xl;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid $color-border-light;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  width: 160rpx;
  font-size: $text-base;
  color: $color-text-primary;
}

.input {
  flex: 1;
  font-size: $text-base;
  color: $color-text-primary;
  text-align: right;
}

.picker {
  flex: 1;
  font-size: $text-base;
  color: $color-text-primary;
  text-align: right;
}

.btn-save {
  margin-top: 48rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  color: $color-white;
  border: none;
  border-radius: $radius-lg;
  font-size: $text-base;
  height: 96rpx;
  line-height: 96rpx;
}
</style>