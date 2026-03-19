<script lang="ts" setup>
/**
 * 草稿恢复提示组件
 * 使用液态玻璃效果，带有进度条倒计时
 */
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

export interface DraftToastData {
  savedAt: Date
  preview: string
  onRestore: () => void
  onDiscard: () => void
}

const props = withDefaults(
  defineProps<{
    visible: boolean
    data?: DraftToastData | null
    duration?: number // 持续时间（毫秒）
  }>(),
  {
    duration: 15000,
    data: null,
  }
)

const emit = defineEmits<{
  (e: 'close'): void
}>()

// 夜间模式状态
const isDark = ref(false)
const checkDarkMode = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

// 进度条状态
const progress = ref(100)
let progressInterval: ReturnType<typeof setInterval> | null = null
let elapsedAtPause = 0 // 暂停时已经过的时间

// 格式化时间为相对时间
const formatTimeAgo = (date: Date): string => {
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`
  return date.toLocaleDateString()
}

// 剩余时间显示
const remainingTime = computed(() => {
  const remaining = Math.ceil((progress.value / 100) * (props.duration / 1000))
  return remaining
})

// 开始倒计时
const startCountdown = (startProgress: number = 100) => {
  stopCountdown()
  progress.value = startProgress
  elapsedAtPause = ((100 - startProgress) / 100) * props.duration

  const startTime = Date.now()

  progressInterval = setInterval(() => {
    const elapsed = Date.now() - startTime + elapsedAtPause
    const remaining = Math.max(0, 100 - (elapsed / props.duration) * 100)
    progress.value = remaining

    if (remaining <= 0) {
      stopCountdown()
      emit('close')
    }
  }, 100)
}

// 停止倒计时
const stopCountdown = () => {
  if (progressInterval) {
    clearInterval(progressInterval)
    progressInterval = null
  }
}

// 处理恢复
const handleRestore = () => {
  stopCountdown()
  props.data?.onRestore?.()
  emit('close')
}

// 处理丢弃
const handleDiscard = () => {
  stopCountdown()
  props.data?.onDiscard?.()
  emit('close')
}

// 处理关闭
const handleClose = () => {
  stopCountdown()
  emit('close')
}

// 鼠标悬停时暂停倒计时
const handleMouseEnter = () => {
  // 记录当前进度
  if (progressInterval) {
    elapsedAtPause = ((100 - progress.value) / 100) * props.duration
  }
  stopCountdown()
}

// 鼠标离开时继续倒计时（从暂停的位置继续）
const handleMouseLeave = () => {
  if (props.visible && progress.value > 0) {
    startCountdown(progress.value)
  }
}

// 监听可见性变化
watch(
  () => props.visible,
  (newVisible) => {
    if (newVisible) {
      elapsedAtPause = 0
      startCountdown()
    } else {
      stopCountdown()
    }
  }
)

// 生命周期
onMounted(() => {
  checkDarkMode()
  const observer = new MutationObserver(() => {
    checkDarkMode()
  })
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  })

  if (props.visible) {
    startCountdown()
  }
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<template>
  <Transition name="bf-draft-toast">
    <div
      v-if="visible && data"
      class="bf-draft-toast"
      :class="{ 'bf-draft-toast--dark': isDark }"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
      @click.stop
    >
      <!-- 关闭按钮 - 右上角 -->
      <button class="bf-draft-toast__close" @click="handleClose" title="关闭">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>

      <!-- 内容区域 -->
      <div class="bf-draft-toast__body">
        <!-- 图标和标题 -->
        <div class="bf-draft-toast__header">
          <div class="bf-draft-toast__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
            </svg>
          </div>
          <div class="bf-draft-toast__title-group">
            <span class="bf-draft-toast__title">发现已保存的草稿</span>
            <span class="bf-draft-toast__time">保存于 {{ formatTimeAgo(data.savedAt) }}</span>
          </div>
        </div>

        <!-- 内容预览 -->
        <div class="bf-draft-toast__preview">
          {{ data.preview }}
        </div>

        <!-- 操作按钮 -->
        <div class="bf-draft-toast__actions">
          <button class="bf-draft-btn bf-draft-btn--restore" @click="handleRestore">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
            恢复草稿
          </button>
          <button class="bf-draft-btn bf-draft-btn--discard" @click="handleDiscard">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
            丢弃
          </button>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="bf-draft-toast__progress">
        <div
          class="bf-draft-toast__progress-bar"
          :style="{ width: `${progress}%` }"
        ></div>
      </div>

      <!-- 剩余时间提示 -->
      <div class="bf-draft-toast__remaining">
        {{ remainingTime }} 秒后自动关闭
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.bf-draft-toast {
  position: fixed;
  top: 80px;
  right: 24px;
  width: 340px;
  border-radius: 16px;
  overflow: hidden;
  z-index: 10001;
  /* 液态玻璃效果 */
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.4) 0%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.4) 100%
  );
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.bf-draft-toast--dark {
  background: linear-gradient(
    135deg,
    rgba(70, 70, 80, 0.5) 0%,
    rgba(60, 60, 67, 0.45) 50%,
    rgba(70, 70, 80, 0.5) 100%
  );
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

/* 关闭按钮 */
.bf-draft-toast__close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: 8px;
  color: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  transition: all 0.15s ease;
  z-index: 1;
}

.bf-draft-toast__close:hover {
  background: rgba(0, 0, 0, 0.15);
  color: rgba(0, 0, 0, 0.8);
}

.bf-draft-toast--dark .bf-draft-toast__close {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
}

.bf-draft-toast--dark .bf-draft-toast__close:hover {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
}

.bf-draft-toast__close svg {
  width: 16px;
  height: 16px;
}

/* 内容区域 */
.bf-draft-toast__body {
  padding: 16px;
  padding-top: 20px;
}

/* 头部 */
.bf-draft-toast__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.bf-draft-toast__icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border-radius: 10px;
  flex-shrink: 0;
}

.bf-draft-toast__icon svg {
  width: 20px;
  height: 20px;
  color: white;
}

.bf-draft-toast__title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.bf-draft-toast__title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1a1a1a;
}

.bf-draft-toast--dark .bf-draft-toast__title {
  color: rgba(255, 255, 255, 0.95);
}

.bf-draft-toast__time {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.5);
}

.bf-draft-toast--dark .bf-draft-toast__time {
  color: rgba(255, 255, 255, 0.5);
}

/* 内容预览 */
.bf-draft-toast__preview {
  font-size: 0.8125rem;
  color: rgba(0, 0, 0, 0.6);
  line-height: 1.5;
  padding: 10px 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bf-draft-toast--dark .bf-draft-toast__preview {
  color: rgba(255, 255, 255, 0.65);
  background: rgba(0, 0, 0, 0.2);
}

/* 操作按钮 */
.bf-draft-toast__actions {
  display: flex;
  gap: 10px;
}

.bf-draft-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  border: none;
}

.bf-draft-btn svg {
  width: 16px;
  height: 16px;
}

.bf-draft-btn--restore {
  background: rgba(34, 197, 94, 0.15);
  color: #16a34a;
}

.bf-draft-btn--restore:hover {
  background: rgba(34, 197, 94, 0.25);
}

.bf-draft-toast--dark .bf-draft-btn--restore {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.bf-draft-toast--dark .bf-draft-btn--restore:hover {
  background: rgba(34, 197, 94, 0.3);
}

.bf-draft-btn--discard {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.bf-draft-btn--discard:hover {
  background: rgba(239, 68, 68, 0.2);
}

.bf-draft-toast--dark .bf-draft-btn--discard {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.bf-draft-toast--dark .bf-draft-btn--discard:hover {
  background: rgba(239, 68, 68, 0.25);
}

/* 进度条 */
.bf-draft-toast__progress {
  height: 3px;
  background: rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.bf-draft-toast--dark .bf-draft-toast__progress {
  background: rgba(0, 0, 0, 0.2);
}

.bf-draft-toast__progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  transition: width 0.1s linear;
}

/* 剩余时间 */
.bf-draft-toast__remaining {
  text-align: center;
  font-size: 0.6875rem;
  color: rgba(0, 0, 0, 0.4);
  padding: 6px 0 8px;
}

.bf-draft-toast--dark .bf-draft-toast__remaining {
  color: rgba(255, 255, 255, 0.4);
}

/* 过渡动画 */
.bf-draft-toast-enter-active {
  animation: slideIn 0.3s ease;
}

.bf-draft-toast-leave-active {
  animation: slideOut 0.2s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-draft-toast {
    right: 12px;
    left: 12px;
    width: auto;
    top: 70px;
  }
}
</style>
