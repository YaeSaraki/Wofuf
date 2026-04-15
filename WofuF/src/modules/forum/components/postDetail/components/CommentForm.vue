<script lang="ts" setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import ImagePicker from '@M/forum/components/imagePicker/ImagePicker.vue'

/* ---------------- 夜间模式检测 ---------------- */
const isDark = ref(false)
const checkDarkMode = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

interface Props {
  postSlug: string
  parentCommentId?: string
  parentComment?: CommentDto | null
}

interface CommentDto {
  commentId: string
  text: string
  memberNickname?: string
  memberId: string
  shortId?: string | null
  createdAt: string
  [key: string]: unknown
}

const props = defineProps<Props>()
const router = useRouter()
const { isAuthenticated } = useAuth()

const emit = defineEmits<{
  replyAdded: []
  replyCancelled: []
}>()

/* ---------------- 表单数据 ---------------- */
const replyText = ref('')
const showImagePicker = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const popupRef = ref<HTMLElement | null>(null)

// 本地弹窗控制（用于顶级评论的回复弹窗）
const showLocalPopup = ref(false)

// 打开顶级评论回复弹窗
function openTopLevelReply() {
  showLocalPopup.value = true
}

/* ---------------- 拖拽状态 ---------------- */
const dragState = reactive({
  isDragging: false,
  startX: 0,
  startY: 0,
  offsetX: 0,
  offsetY: 0,
})

// 初始位置（屏幕中央偏下）
const popupWidth = 520
const initialX = Math.round((window.innerWidth - popupWidth) / 2)
const initialY = Math.round(window.innerHeight * 0.15)

onMounted(() => {
  // 初始化位置
  dragState.offsetX = initialX
  dragState.offsetY = initialY

  // 监听窗口resize
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 监听窗口resize，重新计算位置确保不超出边界
function handleResize() {
  const viewportWidth = window.innerWidth
  const maxX = viewportWidth - popupWidth - 10
  if (dragState.offsetX > maxX) {
    dragState.offsetX = maxX
  }
  if (dragState.offsetX < 10) {
    dragState.offsetX = 10
  }
}

/* ---------------- 拖拽处理 ---------------- */
function handleDragStart(e: MouseEvent) {
  e.preventDefault()
  e.stopPropagation()

  dragState.isDragging = true
  dragState.startX = e.clientX
  dragState.startY = e.clientY
  document.body.style.userSelect = 'none'

  document.addEventListener('mousemove', handleDragMove)
  document.addEventListener('mouseup', handleDragEnd)
}

function handleDragMove(e: MouseEvent) {
  if (!dragState.isDragging) return
  e.preventDefault()

  const deltaX = e.clientX - dragState.startX
  const deltaY = e.clientY - dragState.startY

  dragState.offsetX += deltaX
  dragState.offsetY += deltaY

  // 边界检测
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  const minX = 10
  const maxX = viewportWidth - popupWidth - 10
  const minY = 10
  const maxY = viewportHeight - 100

  dragState.offsetX = Math.max(minX, Math.min(dragState.offsetX, maxX))
  dragState.offsetY = Math.max(minY, Math.min(dragState.offsetY, maxY))

  dragState.startX = e.clientX
  dragState.startY = e.clientY
}

function handleDragEnd() {
  dragState.isDragging = false
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
}

/* ---------------- 弹窗样式 ---------------- */
const popupStyle = computed(() => {
  const transform = dragState.isDragging ? 'scale(0.98)' : 'scale(1)'
  return {
    left: `${dragState.offsetX}px`,
    top: `${dragState.offsetY}px`,
    width: `${popupWidth}px`,
    transform,
  }
})

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 字数统计
const charCount = computed(() => replyText.value.length)

// 判断是否正在回复（来自父组件的回复 OR 本地弹窗）
const isReplying = computed(() => !!props.parentCommentId || showLocalPopup.value)

// 提交回复
async function submitReply() {
  if (!replyText.value.trim()) {
    return
  }

  if (!isAuthenticated()) {
    router.push('/forum/login')
    return
  }

  const result = await executeAsync(async () => {
    if (props.parentCommentId) {
      return await forumService.replyToComment(props.parentCommentId, {
        postSlug: props.postSlug,
        comment: replyText.value.trim(),
      })
    } else {
      return await forumService.replyToPostBySlug(props.postSlug, {
        comment: replyText.value.trim(),
      })
    }
  }, translate('forum', 'replyFailed'))

  if (result) {
    replyText.value = ''
    emit('replyAdded')
  }
}

// 取消回复
function cancelReply() {
  replyText.value = ''
  showLocalPopup.value = false
  emit('replyCancelled')
}

// 截断文本
function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength).trim() + '...'
}

// 插入选中的图片
function insertSelectedImage(markdown: string) {
  replyText.value += '\n' + markdown + '\n'
  showImagePicker.value = false
  textareaRef.value?.focus()
}

/* ---------------- Markdown 工具栏操作 ---------------- */
function insertBeforeCursor(before: string, after: string = '', placeholder: string = '') {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = replyText.value.substring(start, end)
  const beforeText = replyText.value.substring(0, start)
  const afterText = replyText.value.substring(end)

  if (selectedText) {
    replyText.value = beforeText + before + selectedText + after + afterText
  } else if (placeholder) {
    replyText.value = beforeText + before + placeholder + after + afterText
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + before.length, start + before.length + placeholder.length)
    }, 0)
  } else {
    replyText.value = beforeText + before + after + afterText
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + before.length, start + before.length)
    }, 0)
  }
}

function insertBold() {
  insertBeforeCursor('**', '**', '粗体文本')
}

function insertItalic() {
  insertBeforeCursor('*', '*', '斜体文本')
}

function insertCode() {
  insertBeforeCursor('`', '`', '代码')
}

function insertLink() {
  insertBeforeCursor('[', '](url)', '链接文字')
}

function insertQuote() {
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = replyText.value.substring(start, end)
  const beforeText = replyText.value.substring(0, start)
  const afterText = replyText.value.substring(end)

  if (selectedText) {
    replyText.value = beforeText + '\n> ' + selectedText + '\n' + afterText
  } else {
    replyText.value = beforeText + '\n> 引用内容\n' + afterText
  }
}
</script>

<template>
  <div class="bf-reply-section">
    <!-- 回复按钮（仅在非回复模式显示） -->
    <button v-if="!isReplying" @click="openTopLevelReply" class="bf-reply-btn">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <span>{{ translate('forum', 'reply') }}</span>
    </button>

    <!-- 使用 Teleport 弹窗 -->
    <Teleport to="body">
      <Transition name="bf-reply-popup">
        <div v-if="isReplying" class="bf-reply-popup-wrapper">
          <!-- 弹窗主体 -->
          <div
            ref="popupRef"
            class="bf-reply-popup"
            :class="{ 'bf-reply-popup--dark': isDark }"
            :style="popupStyle"
            @click.stop
          >
          <!-- 拖拽把手 -->
          <div class="bf-popup-handle" @mousedown="handleDragStart">
            <div class="bf-popup-handle-bar"></div>
          </div>

          <!-- 被回复的评论预览 -->
          <div v-if="parentComment" class="bf-reply-target">
            <div class="bf-reply-target__glass">
              <div class="bf-reply-target__header">
                <svg class="bf-reply-target__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span class="bf-reply-target__label">回复 @{{ parentComment.memberNickname || parentComment.memberId }}</span>
                <span v-if="parentComment.shortId" class="bf-reply-target__id">#{{ parentComment.shortId }}</span>
              </div>
              <div class="bf-reply-target__content">{{ truncateText(parentComment.text, 100) }}</div>
            </div>
          </div>

          <!-- 模态框头部 -->
          <div class="bf-popup-header">
            <div class="bf-header-left">
              <svg class="bf-reply-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <span class="bf-reply-title">{{ parentComment ? '添加回复' : '添加评论' }}</span>
            </div>
            <!-- X 关闭按钮 -->
            <button class="bf-popup-close" @click="cancelReply" title="关闭">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <!-- 错误提示 -->
          <div v-if="errorMsg" class="bf-error-message">
            <span>{{ errorMsg }}</span>
          </div>

          <!-- 编辑器 -->
          <div class="bf-editor-container">
            <!-- 工具栏 -->
            <div class="bf-editor-toolbar">
              <div class="bf-toolbar-left">
                <button type="button" class="bf-toolbar-btn" @click="insertBold" title="粗体">
                  <span class="bf-toolbar-text">B</span>
                </button>
                <button type="button" class="bf-toolbar-btn" @click="insertItalic" title="斜体">
                  <span class="bf-toolbar-text bf-toolbar-text--italic">I</span>
                </button>
                <button type="button" class="bf-toolbar-btn" @click="insertCode" title="代码">
                  <span class="bf-toolbar-text bf-toolbar-text--code">&lt;/&gt;</span>
                </button>
                <button type="button" class="bf-toolbar-btn" @click="insertLink" title="链接">
                  <span class="bf-toolbar-text bf-toolbar-text--symbol">↗</span>
                </button>
                <button type="button" class="bf-toolbar-btn" @click="insertQuote" title="引用">
                  <span class="bf-toolbar-text">"</span>
                </button>
                <div class="bf-toolbar-divider"></div>
                <button type="button" class="bf-toolbar-btn bf-toolbar-btn--image" @click="showImagePicker = true" title="选择图片">
                  <span class="bf-toolbar-text bf-toolbar-text--symbol">▢</span>
                </button>
              </div>
              <div class="bf-toolbar-right">
                <span class="bf-char-count" :class="{ 'bf-char-count--warning': charCount > 5000 }">
                  {{ charCount }}
                </span>
              </div>
            </div>

            <!-- 文本输入（无占位符） -->
            <textarea
              ref="textareaRef"
              v-model="replyText"
              rows="4"
              class="bf-editor-textarea"
            ></textarea>
          </div>

          <!-- 底部操作栏 -->
          <div class="bf-popup-footer">
            <div class="bf-footer-hint">
              <span>支持 Markdown 格式</span>
            </div>
            <div class="bf-reply-actions">
              <button @click="cancelReply" class="bf-btn bf-btn--ghost">
                {{ translate('forum', 'cancel') }}
              </button>
              <button
                @click="submitReply"
                :disabled="isLoading || !replyText.trim()"
                class="bf-btn bf-btn--primary"
                :class="{ 'bf-btn--loading': isLoading }"
              >
                <span v-if="isLoading" class="bf-spinner"></span>
                <span v-else>{{ translate('forum', 'submitReply') }}</span>
              </button>
            </div>
          </div>

          <!-- 图片选择器 -->
          <ImagePicker
            v-if="showImagePicker"
            @select="insertSelectedImage"
            @close="showImagePicker = false"
          />
        </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.bf-reply-section {
  margin-top: var(--bf-space-lg, 24px);
}

.bf-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: transparent;
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-btn-radius, 12px);
  color: var(--bf-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-reply-btn:hover {
  background: var(--bf-btn-secondary-bg, rgba(255, 255, 255, 0.04));
  border-color: var(--bf-primary, #FF6B35);
  color: var(--bf-primary, #FF6B35);
}

/* 弹窗外层容器（用于定位遮罩和弹窗） */
.bf-reply-popup-wrapper {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
  pointer-events: none;
}

/* 弹窗样式 - 液态玻璃效果（匹配 PostToc） */
.bf-reply-popup {
  position: fixed;
  z-index: 10000;
  pointer-events: auto;
  display: flex;
  flex-direction: column;
  max-height: 80vh;
  border-radius: 16px;
  overflow: hidden;
  /* 液态玻璃 - 亮色模式 */
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

/* 弹窗 - 夜间模式 */
.bf-reply-popup--dark {
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

/* 拖拽把手 */
.bf-popup-handle {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
  cursor: grab;
}

.bf-popup-handle:active {
  cursor: grabbing;
}

.bf-popup-handle-bar {
  width: 40px;
  height: 4px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 2px;
  transition: background 0.2s ease;
}

.bf-popup-handle:hover .bf-popup-handle-bar {
  background: rgba(0, 0, 0, 0.4);
}

/* 夜间模式下的拖拽条 */
.bf-reply-popup--dark .bf-popup-handle-bar {
  background: rgba(255, 255, 255, 0.25);
}

.bf-reply-popup--dark .bf-popup-handle:hover .bf-popup-handle-bar {
  background: rgba(255, 255, 255, 0.4);
}

/* 被回复评论的预览 */
.bf-reply-target {
  padding: 0 16px 12px;
}

.bf-reply-target__glass {
  position: relative;
  border-radius: 12px;
  padding: var(--bf-space-sm, 12px) var(--bf-space-md, 16px);
  background: linear-gradient(
    135deg,
    rgba(255, 107, 53, 0.15) 0%,
    rgba(255, 159, 28, 0.08) 100%
  );
  border: 1px solid rgba(255, 107, 53, 0.25);
  overflow: hidden;
}

.bf-reply-target__glass::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 107, 53, 0.1) 0%,
    rgba(255, 159, 28, 0.05) 100%
  );
  pointer-events: none;
}

.bf-reply-target__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bf-reply-target__icon {
  color: var(--bf-primary, #FF6B35);
  flex-shrink: 0;
}

.bf-reply-target__label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--bf-primary, #FF6B35);
}

.bf-reply-target__id {
  font-size: 11px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: var(--bf-text-muted, #666);
  background: none;
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.1));
  border-radius: 4px;
  padding: 1px 6px;
  margin-left: 4px;
}

.bf-reply-target__id:hover {
  border-color: var(--bf-primary, #ff6b35);
  color: var(--bf-primary, #ff6b35);
}

.bf-reply-target__content {
  font-size: 0.8125rem;
  color: var(--bf-text-secondary, #b3b3b3);
  line-height: 1.5;
  padding-left: 22px;
}

/* 模态框头部 */
.bf-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 12px;
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
}

.bf-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bf-reply-icon {
  color: var(--bf-primary, #FF6B35);
}

.bf-reply-title {
  font-weight: 600;
  color: var(--bf-text-primary);
  font-size: 0.9375rem;
}

/* 关闭按钮 */
.bf-popup-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--bf-text-muted, #666666);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-popup-close:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--bf-text-primary, #ffffff);
}

/* 错误提示 */
.bf-error-message {
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  border-bottom: 1px solid rgba(239, 68, 68, 0.2);
  color: #ef4444;
  font-size: 0.875rem;
}

/* 编辑器容器 */
.bf-editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 工具栏 */
.bf-editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: var(--bf-surface, rgba(255, 255, 255, 0.02));
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
}

.bf-toolbar-left,
.bf-toolbar-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.bf-toolbar-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast);
  -webkit-tap-highlight-color: transparent;
}

.bf-toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--bf-text-primary);
}

.bf-toolbar-btn:active {
  transform: scale(0.95);
}

/* 文字图标按钮 */
.bf-toolbar-text {
  font-weight: 700;
  font-size: 14px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.bf-toolbar-text--symbol {
  font-size: 16px;
  font-weight: 400;
}

.bf-toolbar-text--italic {
  font-style: italic;
}

.bf-toolbar-text--code {
  font-size: 11px;
  letter-spacing: -1px;
}

.bf-toolbar-btn--image {
  color: var(--bf-primary, #FF6B35);
}

.bf-toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--bf-border-default, rgba(255, 255, 255, 0.1));
  margin: 0 4px;
}

.bf-char-count {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
  font-variant-numeric: tabular-nums;
  padding: 0 8px;
}

.bf-char-count--warning {
  color: #f59e0b;
}

/* 文本区域 */
.bf-editor-textarea {
  flex: 1;
  width: 100%;
  min-height: 140px;
  padding: 16px;
  background: transparent;
  border: none;
  color: var(--bf-text-primary);
  font-size: 0.9375rem;
  line-height: 1.6;
  resize: none;
  outline: none;
  font-family: inherit;
}

/* 底部 */
.bf-popup-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
  background: var(--bf-surface, rgba(255, 255, 255, 0.02));
}

.bf-footer-hint {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
}

.bf-reply-actions {
  display: flex;
  gap: 8px;
}

/* 按钮 */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.875rem;
  transition: all var(--bf-transition-fast, 0.15s ease);
  cursor: pointer;
  border: none;
  outline: none;
  -webkit-tap-highlight-color: transparent;
}

.bf-btn--primary {
  background: var(--bf-btn-primary-bg, linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%));
  color: white;
}

.bf-btn--primary:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
}

.bf-btn--primary:active:not(:disabled) {
  transform: scale(0.98);
}

.bf-btn--ghost {
  background: transparent;
  color: var(--bf-text-secondary);
}

.bf-btn--ghost:hover {
  color: var(--bf-text-primary);
  background: rgba(255, 255, 255, 0.04);
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 弹窗过渡动画 */
.bf-reply-popup-enter-active,
.bf-reply-popup-leave-active {
  transition: all 0.25s ease;
}

.bf-reply-popup-enter-from,
.bf-reply-popup-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}

/* 手机端适配 */
@media (max-width: 640px) {
  .bf-reply-popup {
    left: 10px !important;
    right: 10px !important;
    width: auto !important;
    max-width: 100%;
    border-radius: 16px 16px 0 0;
    max-height: 70vh;
  }

  .bf-reply-target {
    padding: 0 12px 10px;
  }

  .bf-popup-header {
    padding: 0 12px 10px;
  }

  .bf-editor-toolbar {
    padding: 6px 10px;
  }

  .bf-toolbar-btn {
    width: 38px;
    height: 38px;
  }

  .bf-toolbar-divider {
    display: none;
  }

  .bf-char-count {
    display: none;
  }

  .bf-editor-textarea {
    min-height: 100px;
    padding: 12px;
    font-size: 1rem;
  }

  .bf-popup-footer {
    flex-direction: column;
    gap: 8px;
    padding: 10px 12px;
  }

  .bf-reply-actions {
    width: 100%;
  }

  .bf-reply-actions .bf-btn {
    flex: 1;
    padding: 10px;
  }
}
</style>
