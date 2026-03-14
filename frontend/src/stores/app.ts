import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // State
  const loading = ref(false)
  const theme = ref<'light' | 'dark'>('light')

  // Actions
  function setLoading(value: boolean) {
    loading.value = value
  }

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  return {
    // State
    loading,
    theme,
    // Actions
    setLoading,
    toggleTheme
  }
})