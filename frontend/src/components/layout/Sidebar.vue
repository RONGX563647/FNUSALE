<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores'
import {
  Odometer,
  DocumentChecked,
  User,
  Postcard,
  ChatLineSquare,
  TrendCharts,
  Setting,
  Tickets
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 菜单项
const menuItems = [
  { path: '/dashboard', title: '仪表盘', icon: Odometer },
  { path: '/audit', title: '待审核商品', icon: DocumentChecked },
  { path: '/user', title: '用户管理', icon: User },
  { path: '/user/auth', title: '认证审核', icon: Postcard },
  { path: '/dispute', title: '纠纷处理', icon: ChatLineSquare },
  { path: '/statistics', title: '数据统计', icon: TrendCharts },
  { path: '/config', title: '系统配置', icon: Setting },
  { path: '/log', title: '操作日志', icon: Tickets }
]

const activeMenu = computed(() => route.path)

function handleSelect(path: string) {
  router.push(path)
}
</script>

<template>
  <el-aside
    :width="appStore.sidebarCollapsed ? '64px' : '210px'"
    class="sidebar"
  >
    <!-- Logo -->
    <div class="logo">
      <img src="/favicon.svg" alt="Logo" class="logo-img" />
      <span v-if="!appStore.sidebarCollapsed" class="logo-text">FNUSALE</span>
    </div>

    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      @select="handleSelect"
    >
      <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
        <el-icon>
          <component :is="item.icon" />
        </el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<style lang="scss" scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background: #304156;
  transition: width 0.3s;
  z-index: 1001;
  overflow: hidden;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;
  overflow: hidden;

  .logo-img {
    width: 32px;
    height: 32px;
  }

  .logo-text {
    margin-left: 12px;
    font-size: 18px;
    font-weight: bold;
    color: #fff;
    white-space: nowrap;
  }
}

.el-menu {
  border-right: none;
}

.el-menu-item {
  &:hover {
    background-color: #263445 !important;
  }

  &.is-active {
    background-color: #409eff !important;
  }
}
</style>