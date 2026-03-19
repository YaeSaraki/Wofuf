import {createApp} from 'vue'
import router from '@S/infra/router'
import App from '@M/app/App.vue'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'

// 引入各个模块
import '@S/services/i18n/index.ts'

import '@M/app/index.ts'
import '@M/players/index.ts'
import '@M/forum/index.ts'
import '@M/auth/index.ts'

// 全局样式
import Aura from '@primeuix/themes/aura'
import '@S/assets/main.css'
import '@S/assets/base.css'
import '@S/assets/theme.css'
import '@S/assets/bonfire-theme.css'
import '@S/layout'

import '@S/assets/primevue.css'

// 初始化主题系统 - 支持 light/dark/system 三种模式
;(function initTheme() {
  const savedTheme = localStorage.getItem('theme')
  let isDark = false
  
  if (savedTheme === 'dark') {
    isDark = true
  } else if (savedTheme === 'light') {
    isDark = false
  } else {
    // system 或未设置时跟随系统
    isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  
  if (isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  
  // 监听系统主题变化
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    const currentTheme = localStorage.getItem('theme')
    // 只有在跟随系统模式时才响应系统主题变化
    if (!currentTheme || currentTheme === 'system') {
      if (e.matches) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  })
})()

const app = createApp(App)

app.use(router)
app.use(PrimeVue, {
  // Default theme configuration
  theme: {
    preset: Aura,
    options: {
      prefix: 'p',
      darkModeSelector: '.dark',
      cssLayer: false,
    },
  },
})
app.use(ToastService)

app.mount('#app')
