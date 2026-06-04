import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/global.css'

const app = createApp(App)

// Pinia 状态管理（持久化）
const pinia = createPinia()
pinia.use(piniaPersistedstate)
app.use(pinia)

// Vue Router
app.use(router)

// Element Plus
app.use(ElementPlus, { locale: { el: { } } })

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
