<script lang="ts" setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  visible: boolean
  src: string
  alt?: string
  images?: string[]  // 所有图片列表，用于左右切换
  currentIndex?: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:currentIndex', index: number): void
}>()

// 当前图片索引
const currentIdx = ref(props.currentIndex ?? 0)

// 缩放状态
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)

// 是否正在拖拽
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)

// 计算当前图片
const currentImage = ref<string>(props.src)

// 计算图片总数
const totalImages = ref(props.images?.length ?? 1)

// 监听 props 变化
watch(() => props.visible, (visible) => {
  if (visible) {
    document.body.style.overflow = 'hidden'
    resetTransform()
  } else {
    document.body.style.overflow = ''
  }
})

watch(() => props.src, (src) => {
  currentImage.value = src
  resetTransform()
})

watch(() => props.currentIndex, (idx) => {
  if (idx !== undefined) {
    currentIdx.value = idx
  }
})

// 重置变换
function resetTransform() {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

// 关闭灯箱
function close() {
  emit('close')
}

// 切换到上一张
function prevImage() {
  if (!props.images || props.images.length <= 1) return
  currentIdx.value = currentIdx.value > 0 ? currentIdx.value - 1 : props.images.length - 1
  const img = props.images[currentIdx.value]
  if (img) currentImage.value = img
  emit('update:currentIndex', currentIdx.value)
  resetTransform()
}

// 切换到下一张
function nextImage() {
  if (!props.images || props.images.length <= 1) return
  currentIdx.value = currentIdx.value < props.images.length - 1 ? currentIdx.value + 1 : 0
  const img = props.images[currentIdx.value]
  if (img) currentImage.value = img
  emit('update:currentIndex', currentIdx.value)
  resetTransform()
}

// 放大
function zoomIn() {
  scale.value = Math.min(scale.value + 0.25, 3)
}

// 缩小
function zoomOut() {
  scale.value = Math.max(scale.value - 0.25, 0.5)
}

// 重置缩放
function resetZoom() {
  resetTransform()
}

// 鼠标滚轮缩放
function handleWheel(event: WheelEvent) {
  event.preventDefault()
  if (event.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

// 开始拖拽
function startDrag(event: MouseEvent | TouchEvent) {
  if (scale.value <= 1) return

  isDragging.value = true
  const touch = 'touches' in event ? event.touches[0] : null
  const clientX = touch ? touch.clientX : (event as MouseEvent).clientX
  const clientY = touch ? touch.clientY : (event as MouseEvent).clientY
  dragStartX.value = clientX - translateX.value
  dragStartY.value = clientY - translateY.value
}

// 拖拽中
function onDrag(event: MouseEvent | TouchEvent) {
  if (!isDragging.value) return

  const touch = 'touches' in event ? event.touches[0] : null
  const clientX = touch ? touch.clientX : (event as MouseEvent).clientX
  const clientY = touch ? touch.clientY : (event as MouseEvent).clientY
  translateX.value = clientX - dragStartX.value
  translateY.value = clientY - dragStartY.value
}

// 结束拖拽
function endDrag() {
  isDragging.value = false
}

// 键盘事件
function handleKeydown(event: KeyboardEvent) {
  if (!props.visible) return

  switch (event.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prevImage()
      break
    case 'ArrowRight':
      nextImage()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
      zoomOut()
      break
    case '0':
      resetZoom()
      break
  }
}

// 计算图片样式
const imageStyle = {
  transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value})`,
  transition: isDragging.value ? 'none' : 'transform 0.2s ease'
}

// 组件挂载/卸载
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="bf-lightbox">
      <div
        v-if="visible"
        class="bf-lightbox-overlay"
        @click="close"
        @wheel="handleWheel"
      >
        <!-- 关闭按钮 -->
        <button class="bf-lightbox-close" @click="close">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <!-- 工具栏 -->
        <div class="bf-lightbox-toolbar" @click.stop>
          <button class="bf-toolbar-btn" @click="zoomOut" title="缩小">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </button>
          <span class="bf-toolbar-zoom">{{ Math.round(scale * 100) }}%</span>
          <button class="bf-toolbar-btn" @click="zoomIn" title="放大">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <line x1="11" y1="8" x2="11" y2="14"/>
              <line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </button>
          <div class="bf-toolbar-divider"></div>
          <button class="bf-toolbar-btn" @click="resetZoom" title="重置">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
        </div>

        <!-- 图片容器 -->
        <div
          class="bf-lightbox-content"
          @click.stop
          @mousedown="startDrag"
          @mousemove="onDrag"
          @mouseup="endDrag"
          @mouseleave="endDrag"
          @touchstart="startDrag"
          @touchmove="onDrag"
          @touchend="endDrag"
        >
          <img
            :src="currentImage"
            :alt="alt || ''"
            class="bf-lightbox-image"
            :style="imageStyle"
            draggable="false"
          />
        </div>

        <!-- 导航按钮 -->
        <template v-if="images && images.length > 1">
          <button class="bf-lightbox-nav bf-lightbox-nav--prev" @click.stop="prevImage">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
          </button>
          <button class="bf-lightbox-nav bf-lightbox-nav--next" @click.stop="nextImage">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>
        </template>

        <!-- 图片计数 -->
        <div v-if="images && images.length > 1" class="bf-lightbox-counter">
          {{ currentIdx + 1 }} / {{ images.length }}
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.bf-lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: zoom-out;
}

.bf-lightbox-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}

.bf-lightbox-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.bf-lightbox-close svg {
  width: 20px;
  height: 20px;
}

.bf-lightbox-toolbar {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: rgba(0, 0, 0, 0.6);
  border-radius: 20px;
  z-index: 10;
}

.bf-toolbar-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.bf-toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.bf-toolbar-btn svg {
  width: 18px;
  height: 18px;
}

.bf-toolbar-zoom {
  min-width: 48px;
  text-align: center;
  color: white;
  font-size: 0.875rem;
  font-weight: 500;
}

.bf-toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 var(--bf-space-xs, 4px);
}

.bf-lightbox-content {
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bf-lightbox-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
  cursor: grab;
}

.bf-lightbox-image:active {
  cursor: grabbing;
}

.bf-lightbox-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}

.bf-lightbox-nav:hover {
  background: rgba(255, 255, 255, 0.2);
}

.bf-lightbox-nav svg {
  width: 24px;
  height: 24px;
}

.bf-lightbox-nav--prev {
  left: 20px;
}

.bf-lightbox-nav--next {
  right: 20px;
}

.bf-lightbox-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: var(--bf-space-xs, 4px) var(--bf-space-md, 16px);
  background: rgba(0, 0, 0, 0.6);
  border-radius: 12px;
  color: white;
  font-size: 0.875rem;
  z-index: 10;
}

/* 过渡动画 */
.bf-lightbox-enter-active,
.bf-lightbox-leave-active {
  transition: opacity 0.2s ease;
}

.bf-lightbox-enter-from,
.bf-lightbox-leave-to {
  opacity: 0;
}

.bf-lightbox-enter-active .bf-lightbox-image,
.bf-lightbox-leave-active .bf-lightbox-image {
  transition: transform 0.2s ease;
}

.bf-lightbox-enter-from .bf-lightbox-image,
.bf-lightbox-leave-to .bf-lightbox-image {
  transform: scale(0.9);
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-lightbox-toolbar {
    top: auto;
    bottom: 80px;
    padding: var(--bf-space-xs, 4px) var(--bf-space-sm, 8px);
  }

  .bf-lightbox-nav {
    width: 40px;
    height: 40px;
  }

  .bf-lightbox-nav--prev {
    left: 10px;
  }

  .bf-lightbox-nav--next {
    right: 10px;
  }
}
</style>
