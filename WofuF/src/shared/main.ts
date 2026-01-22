import { createApp } from 'vue'
import router from '@S/infra/router'
import App from '@M/app/App.vue'
import PrimeVue from 'primevue/config'

// 引入各个模块
import '@S/services/i18n/index.ts'

import '@M/app/index.ts'
import '@M/players/index.ts'

// 全局样式
import Aura from '@primeuix/themes/aura'
import '@S/assets/main.css'
import '@S/assets/base.css'
import '@S/layout'

import '@S/assets/primevue.css'

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
      darkModeSelector: 'system',
      cssLayer: false,
    },
  },
})

app.mount('#app')
