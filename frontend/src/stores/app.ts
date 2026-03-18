import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 设备类型
  const device = ref<'desktop' | 'mobile'>('desktop')

  // 是否为移动端
  const isMobile = computed(() => device.value === 'mobile')

  // 切换侧边栏
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 折叠侧边栏
  function collapseSidebar(): void {
    sidebarCollapsed.value = true
  }

  // 展开侧边栏
  function expandSidebar(): void {
    sidebarCollapsed.value = false
  }

  // 设置设备类型
  function setDevice(newDevice: 'desktop' | 'mobile'): void {
    device.value = newDevice
    if (newDevice === 'mobile') {
      sidebarCollapsed.value = true
    }
  }

  // 监听窗口大小变化
  function watchResize(): void {
    const handleResize = () => {
      const width = window.innerWidth
      if (width < 992) {
        setDevice('mobile')
      } else {
        setDevice('desktop')
      }
    }

    handleResize()
    window.addEventListener('resize', handleResize)
  }

  return {
    sidebarCollapsed,
    device,
    isMobile,
    toggleSidebar,
    collapseSidebar,
    expandSidebar,
    setDevice,
    watchResize
  }
})