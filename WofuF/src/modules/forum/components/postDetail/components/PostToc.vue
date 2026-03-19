<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { translate } from '@S/services/i18n'

interface TocItem {
  level: number
  text: string
  id: string
  children?: TocItem[]
}

const props = defineProps<{
  content: string
  contentRef: HTMLElement | null
}>()

// 夜间模式状态
const isDark = ref(false)
const isTocCollapsed = ref(false)
const expandedItems = ref<Set<string>>(new Set())

// 拖拽状态
const tocFloatRef = ref<HTMLElement | null>(null)
const dragState = ref({
  isDragging: false,
  startX: 0,
  startY: 0,
  offsetX: window.innerWidth - 244,
  offsetY: 100,
})

const checkDarkMode = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

// 构建层级结构的目录
const tocTree = computed<TocItem[]>(() => {
  if (!props.content) return []

  const items: TocItem[] = []
  const headingRegex = /^(#{1,6})\s+(.+)$/gm
  let match
  let index = 0

  while ((match = headingRegex.exec(props.content)) !== null) {
    const level = match[1]?.length ?? 1
    const text = (match[2] ?? '').trim()
    const id = `heading-${index++}`

    if (text) {
      items.push({ level, text, id })
    }
  }

  // 构建树形结构
  const root: TocItem[] = []
  const stack: TocItem[] = []

  for (const item of items) {
    while (stack.length > 0 && (stack[stack.length - 1]?.level ?? 0) >= item.level) {
      stack.pop()
    }

    if (stack.length === 0) {
      root.push(item)
    } else {
      const parent = stack[stack.length - 1]
      if (parent) {
        if (!parent.children) {
          parent.children = []
        }
        parent.children.push(item)
      }
    }

    stack.push(item)
  }

  // 默认展开所有有子项的标题
  const setDefaultExpanded = (items: TocItem[]) => {
    for (const item of items) {
      if (item.children && item.children.length > 0) {
        expandedItems.value.add(item.id)
        setDefaultExpanded(item.children)
      }
    }
  }
  if (expandedItems.value.size === 0) {
    setDefaultExpanded(root)
  }

  return root
})

// 扁平化的目录
const flatTocItems = computed(() => {
  const flatten = (items: TocItem[]): TocItem[] => {
    const result: TocItem[] = []
    for (const item of items) {
      result.push(item)
      if (item.children) {
        result.push(...flatten(item.children))
      }
    }
    return result
  }
  return flatten(tocTree.value)
})

// 计算弹窗样式
const tocStyle = computed(() => {
  const tocWidth = 220
  const tocHeight = tocFloatRef.value?.offsetHeight || (isTocCollapsed.value ? 50 : 400)
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  let x = dragState.value.offsetX
  let y = dragState.value.offsetY

  const minX = 10
  const maxX = viewportWidth - tocWidth - 10
  const minY = 10
  const maxY = viewportHeight - tocHeight - 10

  x = Math.max(minX, Math.min(x, maxX))
  y = Math.max(minY, Math.min(y, maxY))

  const transform = dragState.value.isDragging ? 'scale(0.98)' : 'scale(1)'

  return {
    left: `${x}px`,
    top: `${y}px`,
    width: `${tocWidth}px`,
    transform,
  }
})

// 切换展开状态
const toggleExpand = (item: TocItem) => {
  if (expandedItems.value.has(item.id)) {
    expandedItems.value.delete(item.id)
  } else {
    expandedItems.value.add(item.id)
  }
}

// 判断是否展开
const isExpanded = (item: TocItem) => expandedItems.value.has(item.id)

// 滚动到指定标题
const scrollToHeading = (item: TocItem) => {
  if (!props.contentRef) return

  const flatIndex = flatTocItems.value.findIndex(i => i.id === item.id)
  if (flatIndex === -1) return

  const headings = props.contentRef.querySelectorAll('h1, h2, h3, h4, h5, h6')
  if (headings[flatIndex]) {
    headings[flatIndex].scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 点击标题项
const handleTocItemClick = (item: TocItem) => {
  if (item.children && item.children.length > 0) {
    toggleExpand(item)
  } else {
    scrollToHeading(item)
  }
}

// 拖拽处理
const handleDragStart = (event: MouseEvent | TouchEvent) => {
  event.preventDefault()
  event.stopPropagation()

  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0]?.clientX || 0
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0]?.clientY || 0

  dragState.value = {
    ...dragState.value,
    isDragging: true,
    startX: clientX,
    startY: clientY,
  }

  document.addEventListener('mousemove', handleDragMove)
  document.addEventListener('touchmove', handleDragMove, { passive: false })
  document.addEventListener('mouseup', handleDragEnd)
  document.addEventListener('touchend', handleDragEnd)

  document.body.style.userSelect = 'none'
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
}

const handleDragEnd = () => {
  if (!dragState.value.isDragging) return

  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)

  document.body.style.userSelect = ''
  dragState.value.isDragging = false
}

onMounted(() => {
  checkDarkMode()
  const observer = new MutationObserver(() => {
    checkDarkMode()
  })
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class']
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('touchmove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchend', handleDragEnd)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="bf-toc-float">
      <div
        v-if="tocTree.length > 0"
        ref="tocFloatRef"
        class="bf-toc-float"
        :class="{
          'bf-toc-float--collapsed': isTocCollapsed,
          'bf-toc-float--dragging': dragState.isDragging,
          'bf-toc-float--dark': isDark
        }"
        :style="tocStyle"
      >
        <!-- 拖拽手柄 -->
        <div class="bf-toc-float-handle" @mousedown="handleDragStart">
          <div class="bf-toc-float-handle-bar"></div>
        </div>

        <!-- 头部 -->
        <div class="bf-toc-float-header" @click="isTocCollapsed = !isTocCollapsed">
          <svg class="bf-toc-float-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="8" y1="6" x2="21" y2="6"/>
            <line x1="8" y1="12" x2="21" y2="12"/>
            <line x1="8" y1="18" x2="21" y2="18"/>
            <line x1="3" y1="6" x2="3.01" y2="6"/>
            <line x1="3" y1="12" x2="3.01" y2="12"/>
            <line x1="3" y1="18" x2="3.01" y2="18"/>
          </svg>
          <span class="bf-toc-float-title">{{ translate('forum', 'post.toc.title') }}</span>
          <span class="bf-toc-float-count">{{ flatTocItems.length }}</span>
        </div>

        <!-- 目录列表 -->
        <Transition name="bf-toc-content">
          <div v-show="!isTocCollapsed" class="bf-toc-float-list">
            <template v-for="item in tocTree" :key="item.id">
              <div class="bf-toc-float-item-wrapper">
                <button
                  type="button"
                  class="bf-toc-float-item"
                  :class="[
                    `bf-toc-float-item--level-${item.level}`,
                    { 'bf-toc-float-item--has-children': item.children && item.children.length > 0 }
                  ]"
                  @click="handleTocItemClick(item)"
                >
                  <span class="bf-toc-float-item-text">{{ item.text }}</span>
                  <svg
                    v-if="item.children && item.children.length > 0"
                    class="bf-toc-float-expand"
                    :class="{ 'bf-toc-float-expand--expanded': isExpanded(item) }"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <polyline points="9 18 15 12 9 6" />
                  </svg>
                </button>
                <!-- 子项 -->
                <Transition name="bf-toc-children">
                  <div v-if="item.children && item.children.length > 0 && isExpanded(item)" class="bf-toc-float-children">
                    <template v-for="child in item.children" :key="child.id">
                      <div class="bf-toc-float-item-wrapper">
                        <button
                          type="button"
                          class="bf-toc-float-item"
                          :class="[
                            `bf-toc-float-item--level-${child.level}`,
                            { 'bf-toc-float-item--has-children': child.children && child.children.length > 0 }
                          ]"
                          @click="handleTocItemClick(child)"
                        >
                          <span class="bf-toc-float-item-text">{{ child.text }}</span>
                          <svg
                            v-if="child.children && child.children.length > 0"
                            class="bf-toc-float-expand"
                            :class="{ 'bf-toc-float-expand--expanded': isExpanded(child) }"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                          >
                            <polyline points="9 18 15 12 9 6" />
                          </svg>
                        </button>
                        <!-- 第三层子项 -->
                        <Transition name="bf-toc-children">
                          <div v-if="child.children && child.children.length > 0 && isExpanded(child)" class="bf-toc-float-children">
                            <button
                              v-for="grandchild in child.children"
                              :key="grandchild.id"
                              type="button"
                              class="bf-toc-float-item"
                              :class="`bf-toc-float-item--level-${grandchild.level}`"
                              @click="scrollToHeading(grandchild)"
                            >
                              {{ grandchild.text }}
                            </button>
                          </div>
                        </Transition>
                      </div>
                    </template>
                  </div>
                </Transition>
              </div>
            </template>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 液态玻璃目录弹窗 */
.bf-toc-float {
  position: fixed;
  border-radius: 16px;
  overflow: hidden;
  z-index: 1000;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.35) 0%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.35) 100%
  );
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.bf-toc-float--dark {
  background: linear-gradient(
    135deg,
    rgba(70, 70, 80, 0.45) 0%,
    rgba(60, 60, 67, 0.4) 50%,
    rgba(70, 70, 80, 0.45) 100%
  );
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.35),
    0 2px 8px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.12),
    inset 0 -1px 0 rgba(255, 255, 255, 0.05);
}

.bf-toc-float--dark .bf-toc-float-handle-bar {
  background: rgba(255, 255, 255, 0.25);
}

.bf-toc-float--dark .bf-toc-float-handle:hover .bf-toc-float-handle-bar {
  background: rgba(255, 255, 255, 0.4);
}

.bf-toc-float--dragging {
  box-shadow:
    0 12px 48px rgba(0, 0, 0, 0.3),
    0 4px 12px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  cursor: grabbing;
}

.bf-toc-float-handle {
  padding: 8px 0 4px;
  display: flex;
  justify-content: center;
  cursor: grab;
}

.bf-toc-float-handle:active {
  cursor: grabbing;
}

.bf-toc-float-handle-bar {
  width: 40px;
  height: 4px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 2px;
  transition: background 0.2s ease;
}

.bf-toc-float-handle:hover .bf-toc-float-handle-bar {
  background: rgba(0, 0, 0, 0.4);
}

.bf-toc-float--collapsed .bf-toc-float-handle {
  display: flex;
  padding: 6px 0 4px;
}

.bf-toc-float--collapsed {
  width: auto;
  min-width: 60px;
}

.bf-toc-float-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  transition: all var(--bf-transition-fast);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.bf-toc-float--collapsed .bf-toc-float-header {
  border-bottom: none;
}

.bf-toc-float-header:hover {
  background: rgba(255, 255, 255, 0.1);
}

.bf-toc-float-icon {
  width: 18px;
  height: 18px;
  color: var(--bf-primary);
  flex-shrink: 0;
}

.bf-toc-float-title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--bf-text-primary);
}

.bf-toc-float-count {
  font-size: 0.6875rem;
  padding: 2px 6px;
  background: var(--bf-fire-gradient-subtle);
  border-radius: 100px;
  color: var(--bf-primary);
  margin-left: auto;
}

.bf-toc-float-list {
  padding: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.bf-toc-float-item-wrapper {
  display: flex;
  flex-direction: column;
}

.bf-toc-float-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 6px 10px;
  background: transparent;
  border: none;
  border-radius: 8px;
  text-align: left;
  font-size: 0.8125rem;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast);
}

.bf-toc-float-item-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.bf-toc-float-item:hover {
  background: rgba(255, 255, 255, 0.15);
  color: var(--bf-text-primary);
}

.bf-toc-float-item--level-1 {
  font-weight: 700;
  font-size: 0.875rem;
  color: var(--bf-text-primary);
  padding: 8px 10px;
  border-radius: 10px;
}

.bf-toc-float-item--level-1:hover {
  background: rgba(255, 107, 53, 0.15);
  color: var(--bf-primary);
}

.bf-toc-float-item--level-2 {
  font-weight: 600;
  font-size: 0.8125rem;
  color: var(--bf-text-primary);
  padding: 6px 10px;
}

.bf-toc-float-item--level-3 {
  font-size: 0.75rem;
  color: var(--bf-text-secondary);
  padding: 5px 10px;
}

.bf-toc-float-item--level-4 {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
  padding: 4px 10px;
}

.bf-toc-float-item--level-5,
.bf-toc-float-item--level-6 {
  font-size: 0.6875rem;
  color: var(--bf-text-muted);
  padding: 3px 10px;
  opacity: 0.8;
}

.bf-toc-float-item--has-children {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.bf-toc-float-expand {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  margin-left: 6px;
  color: var(--bf-text-muted);
  transition: transform 0.2s ease;
}

.bf-toc-float-expand--expanded {
  transform: rotate(90deg);
  color: var(--bf-primary);
}

.bf-toc-float-children {
  padding-left: 8px;
  margin-left: 8px;
  border-left: 2px solid rgba(255, 107, 53, 0.3);
}

/* 过渡动画 */
.bf-toc-children-enter-active,
.bf-toc-children-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.bf-toc-children-enter-from,
.bf-toc-children-leave-to {
  opacity: 0;
  max-height: 0;
}

.bf-toc-float-enter-active,
.bf-toc-float-leave-active {
  transition: all 0.3s ease;
}

.bf-toc-float-enter-from,
.bf-toc-float-leave-to {
  opacity: 0;
  transform: translateX(50px);
}

.bf-toc-content-enter-active,
.bf-toc-content-leave-active {
  transition: all 0.2s ease;
}

.bf-toc-content-enter-from,
.bf-toc-content-leave-to {
  opacity: 0;
  max-height: 0;
  overflow: hidden;
}

/* 响应式 */
@media (max-width: 1024px) {
  .bf-toc-float {
    right: 12px;
    top: 80px;
    width: 180px;
  }

  .bf-toc-float-list {
    max-height: 250px;
  }
}

@media (max-width: 640px) {
  .bf-toc-float {
    width: 160px;
    right: 8px;
    top: 70px;
  }

  .bf-toc-float-item {
    font-size: 0.75rem;
    padding: 5px 8px;
  }
}
</style>
