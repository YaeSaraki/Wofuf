import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@/views': fileURLToPath(new URL('./src/**/views', import.meta.url)),
      '@/components': fileURLToPath(new URL('./src/**/components', import.meta.url)),
    },
  },
  server: {
    host: 'localhost',
    port: 5173,

    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8005',
        changeOrigin: true,

        // 如果你的后端本身就是 /api 开头，下面这行可以不要
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
