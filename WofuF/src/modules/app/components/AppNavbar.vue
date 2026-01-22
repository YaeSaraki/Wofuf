<script setup lang="ts">
import Menubar from 'primevue/menubar'
import { useRouter } from 'vue-router'
import { getLocale, setLocale, useLocale } from '@S/services/i18n/useLocale.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import type { MenuItem } from 'primevue/menuitem'

const router = useRouter()
const { translate } = useLocale()

const items = computed<MenuItem[]>(() => [
  {
    label: translate('app', 'nav.status'),
    icon: 'pi pi-home',
    command: () => router.push('/'),
  },
  {
    label: translate('app', 'nav.about'),
    icon: 'pi pi-info-circle',
    command: () => router.push('/about'),
  },
  {
    label: getLocale() === 'zh' ? 'English' : '中文',
    icon: 'pi pi-globe',
    command: () => {
      const targetLang = getLocale() === 'zh' ? 'en' : 'zh'
      setLocale(targetLang)
    },
  },
])

const popupMenuRef = ref<HTMLElement | null>(null)
const showCloseButton = ref(false)
let hideTimer: ReturnType<typeof setTimeout> | null = null

const menuPosition = ref({
  x: typeof window !== 'undefined' ? window.innerWidth / 2 - 160 : 100,
  y: typeof window !== 'undefined' ? window.innerHeight / 100 : 100,
})

const dragState = ref({
  isDragging: false,
  startX: 0,
  startY: 0,
  offsetX: 0,
  offsetY: 0,
})

const menuStyle = computed(() => {
  const menuWidth = popupMenuRef.value?.offsetWidth || 320
  const menuHeight = popupMenuRef.value?.offsetHeight || 200

  let x = menuPosition.value.x
  let y = menuPosition.value.y

  if (dragState.value.isDragging && dragState.value.offsetX !== 0) {
    x = dragState.value.offsetX
    y = dragState.value.offsetY
  }

  if (typeof window !== 'undefined') {
    const viewportWidth = window.innerWidth
    const viewportHeight = window.innerHeight

    if (x + menuWidth > viewportWidth) {
      x = viewportWidth - menuWidth - 10
    }
    if (x < 10) {
      x = 10
    }
    if (y + menuHeight > viewportHeight) {
      y = viewportHeight - menuHeight - 10
    }
    if (y < 10) {
      y = 10
    }
  }

  return {
    left: `${x}px`,
    top: `${y}px`,
    transform: dragState.value.isDragging ? 'scale(0.98)' : 'scale(1)',
    backdropFilter: dragState.value.isDragging ? 'blur(4px)' : 'blur(3px)',
  }
})

function clearTimer() {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
}

function startHideTimer() {
  clearTimer()
  hideTimer = setTimeout(() => {
    if (!dragState.value.isDragging) {
      showCloseButton.value = false
    }
  }, 3000)
}

function showButton() {
  showCloseButton.value = true
  startHideTimer()
}

function hideButton() {
  showCloseButton.value = false
  clearTimer()
}

const handleDragStart = (event: MouseEvent | TouchEvent) => {
  event.preventDefault()
  event.stopPropagation()

  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0]?.clientX || 0
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0]?.clientY || 0

  dragState.value = {
    isDragging: true,
    startX: clientX,
    startY: clientY,
    offsetX: menuPosition.value.x,
    offsetY: menuPosition.value.y,
  }

  document.addEventListener('mousemove', handleDragMove)
  document.addEventListener('touchmove', handleDragMove)
  document.addEventListener('mouseup', handleDragEnd)
  document.addEventListener('touchend', handleDragEnd)
  document.body.style.userSelect = 'none'

  hideButton()
}

const handleDragMove = (event: MouseEvent | TouchEvent) => {
  if (!dragState.value.isDragging) return
  event.preventDefault()

  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0]?.clientX || 0
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0]?.clientY || 0

  const deltaX = clientX - dragState.value.startX
  const deltaY = clientY - dragState.value.startY

  dragState.value.offsetX += deltaX
  dragState.value.offsetY += deltaY

  dragState.value.startX = clientX
  dragState.value.startY = clientY

  menuPosition.value.x = dragState.value.offsetX
  menuPosition.value.y = dragState.value.offsetY
}

const handleDragEnd = () => {
  if (!dragState.value.isDragging) return

  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)
  document.body.style.userSelect = ''

  dragState.value.isDragging = false
  showButton()
}

const resetMenuPosition = () => {
  if (typeof window !== 'undefined') {
    menuPosition.value = {
      x: window.innerWidth / 2 - 160,
      y: window.innerHeight / 100,
    }
  }
  hideButton()
}

const handleMenuClick = () => {
  showButton()
}

onMounted(() => {
  hideButton()
  window.addEventListener('resize', resetMenuPosition)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)
  document.body.style.userSelect = ''
  clearTimer()
  window.removeEventListener('resize', resetMenuPosition)
})
</script>

<template>
  <!-- 菜单 -->
  <div
    ref="popupMenuRef"
    class="floating-menu rounded-2xl border border-zinc-200 dark:border-zinc-600 select-none flex fixed z-9999"
    :style="menuStyle"
    @click.stop="handleMenuClick"
    @mouseleave="startHideTimer"
    @mouseenter="showButton"
  >
    <span
      class="p-3 ml-2 border-zinc-200 dark:border-zinc-700 cursor-move flex items-center"
      @mousedown="handleDragStart"
      @touchstart="handleDragStart"
    >
      <span class="logo font-bold boder-none text-zinc-800 dark:text-white">🐾 WofuF</span>
    </span>

    <!-- 重置按钮 -->
    <button
      v-show="showCloseButton"
      class="floating-menu absolute -top-2 -left-2 p-1.5 hover:scale-105 transition-opacity duration-200 shadow-2xl rounded-2xl border border-zinc-200 dark:border-zinc-600"
      @click.stop="resetMenuPosition"
      aria-label="Reset position"
      title="Reset to center"
    >
      <svg
        class="w-4 h-4 text-zinc-500 dark:text-zinc-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
        />
      </svg>
    </button>

    <!-- 菜单内容 -->
    <Menubar
      :model="items"
      style="
        background: transparent;
        border: none !important;
        --p-menubar-submenu-border-color: rgb(245 245 245 / 0.5);
      "
    />
  </div>
</template>

<style scoped>
/* 拖拽反馈 */
.cursor-move {
  cursor: move;
}

.cursor-move:active {
  cursor: grabbing;
}

/* 入场动画 */
@keyframes floatIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 子菜单 */
:deep(.p-menubar-submenu) {
  margin: 0 4px !important;
}
:deep(.p-menubar-root-list) {
  min-width: 100px !important;
}
/* 子菜单列表 */
:deep(.p-menubar-root-list > .p-menubar-item > .p-submenu-list) {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 12px !important;
  padding: 8px !important;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.2) !important;
  margin-top: 8px !important;
}

.dark :deep(.p-menubar-root-list > .p-menubar-item > .p-submenu-list) {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1) !important;
}

/* 子菜单项 */
:deep(.p-submenu-list .p-menubar-item) {
  background: transparent !important;
  margin: 0 !important;
}

:deep(.p-submenu-list .p-menubar-item-link) {
  background: transparent !important;
  margin: 2px 0 !important;
}

:deep(.p-submenu-list .p-menubar-item-link:hover) {
  background: transparent !important;
  backdrop-filter: blur(20px) !important;
  border-radius: 8px !important;
}

.dark :deep(.p-submenu-list .p-menubar-item-link:hover) {
  background: transparent !important;
}

/* 文字和图标 */
:deep(.p-menubar-item-label) {
  background: transparent !important;
  font-weight: 500 !important;
  letter-spacing: -0.01em !important;
  font-size: 15px !important;
}

:deep(.p-menubar-item-icon) {
  background: transparent !important;
  color: rgba(0, 0, 0, 0.8) !important;
  transition: all 0.2s ease !important;
}

.dark :deep(.p-menubar-item-icon) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

:deep(.p-menubar-item-link:hover .p-menubar-item-icon) {
  background: transparent !important;
  color: rgba(0, 0, 0, 1) !important;
  transform: scale(1.05) !important;
}

.dark :deep(.p-menubar-item-link:hover .p-menubar-item-icon) {
  color: rgba(255, 255, 255, 1) !important;
}

/* 子菜单图标 */
:deep(.p-menubar-submenu-icon) {
  color: rgba(0, 0, 0, 0.6) !important;
  font-size: 12px !important;
}

.dark :deep(.p-menubar-submenu-icon) {
  color: rgba(255, 255, 255, 0.7) !important;
}

/* 子菜单动画 */
:deep(.p-menubar-root-list > .p-menubar-item > .p-submenu-list) {
  animation: menuSlide 0.25s cubic-bezier(0.2, 0, 0, 1) !important;
  transform-origin: top center !important;
}

@keyframes menuSlide {
  from {
    opacity: 0 !important;
    transform: translateY(-10px) scale(0.95) !important;
  }
  to {
    opacity: 1 !important;
    transform: translateY(0) scale(1) !important;
  }
}
</style>
