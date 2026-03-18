<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAppStore, useAuthStore } from '@/stores'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await authStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <el-header class="header">
    <div class="left">
      <el-icon class="toggle-btn" @click="toggleSidebar">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="right">
      <el-dropdown>
        <span class="user-info">
          <el-avatar :size="32" icon="User" />
          <span class="username">{{ authStore.adminInfo?.nickname || '管理员' }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :icon="User">个人信息</el-dropdown-item>
            <el-dropdown-item divided :icon="SwitchButton" @click="handleLogout">
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<style lang="scss" scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;

  .toggle-btn {
    font-size: 20px;
    cursor: pointer;
    color: #606266;

    &:hover {
      color: #409eff;
    }
  }
}

.right {
  display: flex;
  align-items: center;
  margin-left: auto;
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    .username {
      color: #606266;
    }
  }
}
</style>