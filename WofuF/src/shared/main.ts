import {createApp} from 'vue'
import router from '@S/infra/router'
import App from '@M/app/App.vue'
import PrimeVue from 'primevue/config'

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
import '@S/layout'

import '@S/assets/primevue.css'

// 初始化暗黑模式 - 在 Vue 应用挂载前执行以避免闪烁
;(function initDarkMode() {
  const isDark =
    localStorage.theme === 'dark' ||
    (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)

  if (isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }

  // 监听系统主题变化
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    if (!('theme' in localStorage)) {
      if (e.matches) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  })
})()

const app = createApp(App)

// initImageResources()
// app.config.globalProperties.$getImageUrl = getImageUrl;
// app.provide('$getImageUrl', getImageUrl);

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

app.mount('#app')
