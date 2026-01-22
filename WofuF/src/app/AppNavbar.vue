<script setup lang="ts">
import Menubar from 'primevue/menubar'
import { useRouter } from 'vue-router'
const router = useRouter()

const items = [
  {
    label: '状态',
    icon: 'pi pi-home',
    command: () => router.push('/'),
  },
  {
    label: '关于',
    icon: 'pi pi-info-circle',
    command: () => router.push('/about'),
  },
]
</script>
<template>
  <div class="fixed-menubar-wrapper">
    <Menubar
      class="glass-menubar"
      style="
        background-color: transparent;
        backdrop-filter: blur(2px);
        -webkit-backdrop-filter: blur(2px); /* 兼容 Safari */

        --p-menubar-border-radius: 15px;
        --p-menubar-submenu-border-radius: 15px;
      "
      :model="items"
    >
      <template #start>
        <span class="logo">🐾 WofuF</span>
      </template>
    </Menubar>
  </div>
</template>

<style scoped>
/* 导航栏外层容器：固定定位核心 */
.fixed-menubar-wrapper {
  position: fixed; /* 固定在视口顶部 */
  top: 1%;
  margin-left: 50%;
  transform: translateX(-50%);
  z-index: 999; /* 确保不被其他元素遮挡 */
  user-select: none;
}

</style>
