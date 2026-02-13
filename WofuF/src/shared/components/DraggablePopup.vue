<script lang="ts" setup>
import {
  computed,
  defineEmits,
  defineProps,
  nextTick,
  onUnmounted,
  ref,
  watch,
  withDefaults,
} from 'vue'

const props = withDefaults(
  defineProps<{
    // 是否显示弹窗
    visible: boolean
    // 弹窗初始位置 X
    initX: number
    // 弹窗初始位置 Y
    initY: number
    // 弹窗宽度（默认 320px，对应 w-80）
    width?: string | number
    // 弹窗最大高度（默认 400px）
    maxHeight?: string | number
  }>(),
  {
    width: 320,
    maxHeight: 400,
  },
)

const emit = defineEmits<{
  // 弹窗关闭事件
  (e: 'close'): void
  // 拖拽过程中位置更新事件（可选）
  (e: 'update:position', x: number, y: number): void
}>()

// 弹窗 DOM 引用
const popupRef = ref<HTMLElement | null>(null)

// 拖拽状态
const dragState = ref({
  isDragging: false,
  startX: 0,
  startY: 0,
  offsetX: 0,
  offsetY: 0,
})

// 外部点击关闭的清理函数
let cleanupExternalClick: (() => void) | null = null

// 计算弹窗样式（核心：边界检测 + 拖拽位置计算）
const popupStyle = computed(() => {
  if (!props.visible) return {display: 'none'}

  // 获取弹窗实际尺寸（优先 DOM 渲染值，其次默认值/Props 值）
  const menuWidth = typeof props.width === 'number' ? props.width : parseInt(props.width) || 320
  const menuHeight = popupRef.value?.offsetHeight || parseInt(props.maxHeight as string) || 400
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  // 初始位置（优先拖拽偏移量，其次 props 初始位置）
  let x = dragState.value.offsetX || props.initX
  let y = dragState.value.offsetY || props.initY

  // 边界检测：确保弹窗不超出视口（留出 10px 边距）
  const minX = 10
  const maxX = viewportWidth - menuWidth - 10
  const minY = 10
  const maxY = viewportHeight - menuHeight - 10

  // 限制位置在合法范围内
  x = Math.max(minX, Math.min(x, maxX))
  y = Math.max(minY, Math.min(y, maxY))

  // 拖拽状态下的样式微调
  const transform = dragState.value.isDragging ? 'scale(0.98)' : 'scale(1)'
  const backdropFilter = dragState.value.isDragging ? 'blur(3px)' : 'blur(2px)'

  return {
    left: `${x}px`,
    top: `${y}px`,
    width: typeof props.width === 'string' ? props.width : `${props.width}px`,
    maxHeight: typeof props.maxHeight === 'string' ? props.maxHeight : `${props.maxHeight}px`,
    transform,
    backdropFilter,
    display: 'block',
  }
})

// 初始化弹窗（显示时设置外部点击监听 + 重置拖拽偏移）
const initPopup = () => {
  if (!props.visible) return

  // 重置拖拽偏移量为初始位置
  dragState.value.offsetX = props.initX
  dragState.value.offsetY = props.initY

  // 下一次渲染后添加外部点击关闭监听
  nextTick(() => {
    setupExternalClickHandler()
  })
}

// 设置外部点击关闭监听
const setupExternalClickHandler = () => {
  // 清理旧监听
  if (cleanupExternalClick) {
    cleanupExternalClick()
  }

  const handleClickOutside = (event: MouseEvent) => {
    if (popupRef.value && !popupRef.value.contains(event.target as Node)) {
      emit('close')
    }
  }

  document.addEventListener('mousedown', handleClickOutside)
  cleanupExternalClick = () => {
    document.removeEventListener('mousedown', handleClickOutside)
    cleanupExternalClick = null
  }
}

// 处理拖拽开始
const handleDragStart = (event: MouseEvent | TouchEvent) => {
  if (!props.visible) return

  event.preventDefault()
  event.stopPropagation()

  // 获取鼠标/触摸点坐标
  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0]?.clientX || 0
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0]?.clientY || 0

  // 初始化拖拽状态
  dragState.value = {
    isDragging: true,
    startX: clientX,
    startY: clientY,
    offsetX: parseInt(popupStyle.value.left as string) || props.initX,
    offsetY: parseInt(popupStyle.value.top as string) || props.initY,
  }

  // 添加拖拽移动/结束监听
  document.addEventListener('mousemove', handleDragMove)
  document.addEventListener('touchmove', handleDragMove)
  document.addEventListener('mouseup', handleDragEnd)
  document.addEventListener('touchend', handleDragEnd)

  // 防止拖拽时选中文本
  document.body.style.userSelect = 'none'
}

// 处理拖拽移动
const handleDragMove = (event: MouseEvent | TouchEvent) => {
  if (!dragState.value.isDragging) return

  event.preventDefault()

  // 获取鼠标/触摸点坐标
  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0]?.clientX || 0
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0]?.clientY || 0

  // 计算移动偏移量并更新弹窗位置
  const deltaX = clientX - dragState.value.startX
  const deltaY = clientY - dragState.value.startY

  dragState.value.offsetX += deltaX
  dragState.value.offsetY += deltaY

  // 更新拖拽起点（平滑拖拽）
  dragState.value.startX = clientX
  dragState.value.startY = clientY

  // 发射位置更新事件（可选，供父组件同步位置）
  emit('update:position', dragState.value.offsetX, dragState.value.offsetY)
}

// 处理拖拽结束
const handleDragEnd = () => {
  if (!dragState.value.isDragging) return

  // 移除拖拽监听
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)

  // 恢复文本选择
  document.body.style.userSelect = ''

  // 重置拖拽状态
  dragState.value.isDragging = false

  // 发射最终位置更新事件
  emit('update:position', dragState.value.offsetX, dragState.value.offsetY)
}

// 主动关闭弹窗
const closePopup = () => {
  emit('close')
}

// 监听弹窗可见性变化，初始化/清理弹窗
watch(
  () => props.visible,
  (newVisible) => {
    if (newVisible) {
      initPopup()
    } else {
      // 隐藏时清理所有监听和状态
      if (cleanupExternalClick) {
        cleanupExternalClick()
      }
      dragState.value = {
        isDragging: false,
        startX: 0,
        startY: 0,
        offsetX: 0,
        offsetY: 0,
      }
    }
  },
  {immediate: true},
)

// 组件卸载时清理所有监听
onUnmounted(() => {
  if (cleanupExternalClick) {
    cleanupExternalClick()
  }

  // 清理拖拽监听
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)
})

// 暴露插槽道具（供父组件使用拖拽开始方法）
defineExpose({
  handleDragStart,
})
</script>

<template>
  <!-- 通用可拖拽弹窗容器 -->
  <div
    ref="popupRef"
    :style="popupStyle"
    class="floating-menu fixed z-50 shadow-xl rounded-2xl border border-zinc-400 dark:border-zinc-600 overflow-y-auto no-scrollbar flex flex-col"
    @click.stop
  >
    <slot :closePopup="closePopup" :handleDragStart="handleDragStart" name="content"/>
  </div>
</template>

<style scoped></style>
