
import { createApp } from 'vue'
import App from './App.vue'
import router from '@/app/router'
import PrimeVue from 'primevue/config'

// 基础样式
import Aura from '@primeuix/themes/aura'

// 全局样式
import '../assets/main.css'
import '../assets/base.css'
import '../assets/primevue.css'

const app = createApp(App)

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
