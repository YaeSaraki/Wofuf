<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import ImagePicker from '@M/forum/components/imagePicker/ImagePicker.vue'

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
const isReplying = ref(false)
const showImagePicker = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 字数统计
const charCount = computed(() => replyText.value.length)

// 监听 parentCommentId 变化，自动展开回复表单
watch(
  () => props.parentCommentId,
  (newVal) => {
    if (newVal) {
      isReplying.value = true
    }
  }
)

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
    isReplying.value = false
    emit('replyAdded')
  }
}

// 取消回复
function cancelReply() {
  replyText.value = ''
  isReplying.value = false
  emit('replyCancelled')
}

// 开始回复
function startReply() {
  if (!isAuthenticated()) {
    router.push('/forum/login')
    return
  }
  isReplying.value = true
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
    <!-- 回复按钮 -->
    <div v-if="!isReplying">
      <button @click="startReply" class="bf-reply-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span>{{ translate('forum', 'reply') }}</span>
      </button>
    </div>

    <!-- 回复表单 -->
    <div v-else class="bf-reply-form">
      <!-- 被回复的评论预览（液态毛玻璃浮动卡片） -->
      <div v-if="parentComment" class="bf-reply-target">
        <div class="bf-reply-target__glass liquid-glass-strong">
          <div class="bf-reply-target__header">
            <svg class="bf-reply-target__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span class="bf-reply-target__label">回复 @{{ parentComment.memberNickname || parentComment.memberId }}</span>
            <button class="bf-reply-target__close" @click="cancelReply" title="取消回复">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="bf-reply-target__content">{{ truncateText(parentComment.text, 120) }}</div>
        </div>
      </div>

      <!-- 头部 -->
      <div class="bf-reply-header">
        <div class="bf-header-left">
          <svg class="bf-reply-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span class="bf-reply-title">{{ parentComment ? '添加回复' : '添加评论' }}</span>
        </div>
        <span class="bf-reply-hint">Markdown</span>
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
            <button type="button" class="bf-toolbar-btn" @click="insertBold" title="粗体 (Ctrl+B)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/>
                <path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/>
              </svg>
            </button>
            <button type="button" class="bf-toolbar-btn" @click="insertItalic" title="斜体 (Ctrl+I)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="19" y1="4" x2="10" y2="4"/>
                <line x1="14" y1="20" x2="5" y2="20"/>
                <line x1="15" y1="4" x2="9" y2="20"/>
              </svg>
            </button>
            <button type="button" class="bf-toolbar-btn" @click="insertCode" title="代码">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="16 18 22 12 16 6"/>
                <polyline points="8 6 2 12 8 18"/>
              </svg>
            </button>
            <button type="button" class="bf-toolbar-btn" @click="insertLink" title="链接 (Ctrl+K)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
            </button>
            <button type="button" class="bf-toolbar-btn" @click="insertQuote" title="引用">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21"/>
                <path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21"/>
              </svg>
            </button>
            <div class="bf-toolbar-divider"></div>
            <button type="button" class="bf-toolbar-btn bf-toolbar-btn--image" @click="showImagePicker = true" title="选择图片">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
            </button>
          </div>
          <div class="bf-toolbar-right">
            <span class="bf-char-count" :class="{ 'bf-char-count--warning': charCount > 5000 }">
              {{ charCount }}
            </span>
          </div>
        </div>

        <!-- 文本输入 -->
        <textarea
          ref="textareaRef"
          v-model="replyText"
          rows="4"
          class="bf-editor-textarea"
          :placeholder="translate('forum', 'enterReply')"
        ></textarea>
      </div>

      <!-- 底部操作栏 -->
      <div class="bf-reply-footer">
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

.bf-reply-form {
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 被回复评论的预览（液态毛玻璃浮动卡片） */
.bf-reply-target {
  padding: var(--bf-space-md, 16px);
  padding-bottom: 0;
}

.bf-reply-target__glass {
  position: relative;
  border-radius: 12px;
  padding: var(--bf-space-sm, 12px) var(--bf-space-md, 16px);
  overflow: hidden;
}

.bf-reply-target__glass::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 107, 53, 0.08) 0%,
    rgba(255, 159, 28, 0.05) 100%
  );
  pointer-events: none;
}

.bf-reply-target__header {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 6px);
  margin-bottom: var(--bf-space-xs, 6px);
}

.bf-reply-target__icon {
  color: var(--bf-primary, #FF6B35);
  flex-shrink: 0;
}

.bf-reply-target__label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--bf-primary, #FF6B35);
  flex: 1;
}

.bf-reply-target__close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--bf-text-muted, #666666);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-reply-target__close:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--bf-text-primary, #ffffff);
}

.bf-reply-target__content {
  font-size: 0.8125rem;
  color: var(--bf-text-secondary, #b3b3b3);
  line-height: 1.5;
  padding-left: 20px;
}

/* 头部 */
.bf-reply-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-md, 16px);
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
}

.bf-header-left {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
}

.bf-reply-icon {
  color: var(--bf-primary, #FF6B35);
}

.bf-reply-title {
  font-weight: 600;
  color: var(--bf-text-primary);
  font-size: 0.9375rem;
}

.bf-reply-hint {
  font-size: 0.6875rem;
  padding: 2px 6px;
  background: var(--bf-surface, rgba(255, 255, 255, 0.05));
  border-radius: 4px;
  color: var(--bf-text-muted);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 错误提示 */
.bf-error-message {
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
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
  padding: var(--bf-space-xs, 4px) var(--bf-space-sm, 8px);
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

.bf-toolbar-btn svg {
  width: 16px;
  height: 16px;
}

.bf-toolbar-btn--image {
  color: var(--bf-primary, #FF6B35);
}

.bf-toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--bf-border-default, rgba(255, 255, 255, 0.1));
  margin: 0 var(--bf-space-xs, 4px);
}

.bf-char-count {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
  font-variant-numeric: tabular-nums;
  padding: 0 var(--bf-space-sm, 8px);
}

.bf-char-count--warning {
  color: #f59e0b;
}

/* 文本区域 */
.bf-editor-textarea {
  flex: 1;
  width: 100%;
  min-height: 140px;
  padding: var(--bf-space-md, 16px);
  background: transparent;
  border: none;
  color: var(--bf-text-primary);
  font-size: 0.9375rem;
  line-height: 1.6;
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.bf-editor-textarea::placeholder {
  color: var(--bf-text-muted, #666666);
}

/* 底部 */
.bf-reply-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
  background: var(--bf-surface, rgba(255, 255, 255, 0.02));
}

.bf-footer-hint {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
}

.bf-reply-actions {
  display: flex;
  gap: var(--bf-space-sm, 8px);
}

/* 按钮 */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  border-radius: var(--bf-btn-radius, 12px);
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

/* ==================== 手机端适配 ==================== */
@media (max-width: 640px) {
  .bf-reply-form {
    border-radius: var(--bf-card-radius-sm, 12px);
  }

  .bf-reply-header {
    padding: var(--bf-space-sm, 12px) var(--bf-space-md, 16px);
  }

  .bf-reply-title {
    font-size: 0.875rem;
  }

  /* 工具栏 - 手机端更大按钮 */
  .bf-editor-toolbar {
    padding: var(--bf-space-sm, 8px);
    gap: var(--bf-space-xs, 4px);
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .bf-toolbar-left {
    flex: 1;
  }

  .bf-toolbar-btn {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
  }

  .bf-toolbar-btn svg {
    width: 18px;
    height: 18px;
  }

  .bf-toolbar-divider {
    display: none;
  }

  .bf-char-count {
    display: none;
  }

  /* 文本区域 */
  .bf-editor-textarea {
    min-height: 120px;
    padding: var(--bf-space-sm, 12px) var(--bf-space-md, 16px);
    font-size: 1rem; /* 防止 iOS 缩放 */
  }

  /* 底部 */
  .bf-reply-footer {
    flex-direction: column;
    gap: var(--bf-space-sm, 8px);
    padding: var(--bf-space-sm, 12px) var(--bf-space-md, 16px);
  }

  .bf-reply-actions {
    width: 100%;
    display: flex;
  }

  .bf-reply-actions .bf-btn {
    flex: 1;
    padding: var(--bf-space-sm, 12px);
  }

  .bf-btn {
    font-size: 0.9375rem;
  }
}
</style>
