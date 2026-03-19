import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'

export function createApp() {
  const app = createSSRApp({})
  const pinia = createPinia()
  app.use(pinia)
  return { app }
}
