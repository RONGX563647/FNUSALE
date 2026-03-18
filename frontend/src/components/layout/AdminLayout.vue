<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const appStore = useAppStore()

const mainStyle = computed(() => ({
  marginLeft: appStore.sidebarCollapsed ? '64px' : '210px',
  transition: 'margin-left 0.3s'
}))
</script>

<template>
  <div class="admin-layout">
    <Sidebar />
    <div :style="mainStyle" class="main-container">
      <Header />
      <el-main class="main-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.main-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>