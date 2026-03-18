<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores'

const appStore = useAppStore()

onMounted(() => {
  appStore.watchResize()
})
</script>

<template>
  <router-view />
</template>

<style>
@use '@/styles/index.scss';
</style>