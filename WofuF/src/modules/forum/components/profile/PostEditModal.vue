<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import { forumService } from '@M/forum/services/ForumService'
import { translate } from '@S/services/i18n'
import type { PostSummary } from '@M/forum/dtos/Member'
import MarkdownEditor from '@M/forum/components/markdownEditor/MarkdownEditor.vue'
import { currentLocale } from '@S/services/i18n/useLocale'

const props = defineProps<{
  visible: boolean
  post: PostSummary | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'postUpdated'): void
}>()

/* ---------------- 编辑器引用 ---------------- */
const editorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null)

/* ---------------- 表单数据 ---------------- */
const title = ref('')
const text = ref('')
const link = ref('')
const postType = ref<'TEXT' | 'LINK'>('TEXT')
const category = ref<string>('DISCUSSION')

/* ---------------- 状态 ---------------- */
const isLoading = ref(false)
const fetchingPost = ref(false)
const errorMsg = ref('')

/* ---------------- 分类选项 ---------------- */
const categories = computed(() => {
  const _locale = currentLocale.value
  return [
    { id: 'DISCUSSION', label: translate('forum', 'category.discussion') },
    { id: 'QUESTION', label: translate('forum', 'category.question') },
    { id: 'SHOWCASE', label: translate('forum', 'category.showcase') },
    { id: 'NEWS', label: translate('forum', 'category.news') },
    { id: 'GUIDE', label: translate('forum', 'category.guide') },
  ]
})

/* ---------------- 获取完整帖子 ---------------- */
watch(
  () => props.visible,
  async (val) => {
    if (val && props.post) {
      errorMsg.value = ''
      fetchingPost.value = true

      const result = await forumService.getPostBySlug(props.post.slug)

      fetchingPost.value = false

      if (result.isSuccess) {
        const fullPost = result.getValue().post
        title.value = fullPost.title
        text.value = fullPost.text || ''
        link.value = fullPost.link || ''
        postType.value = fullPost.type === 'LINK' ? 'LINK' : 'TEXT'
        category.value = fullPost.category || 'DISCUSSION'
      } else {
        errorMsg.value = String(result.error)
      }
    }
  },
)

/* ---------------- 保存编辑 ---------------- */
async function handleSave() {
  if (!title.value.trim()) {
    errorMsg.value = translate('forum', 'titleRequired')
    return
  }

  if (postType.value === 'TEXT' && !text.value.trim()) {
    errorMsg.value = translate('forum', 'contentRequired')
    return
  }

  if (postType.value === 'LINK' && !link.value.trim()) {
    errorMsg.value = translate('forum', 'linkRequired')
    return
  }

  if (!props.post) return

  isLoading.value = true
  errorMsg.value = ''

  const result = await forumService.editPost(props.post.postId, {
    title: title.value.trim(),
    text: text.value.trim() || undefined,
    link: link.value.trim() || undefined,
    category: category.value,
  })

  isLoading.value = false

  if (result.isSuccess) {
    editorRef.value?.clearCache()
    emit('postUpdated')
    emit('update:visible', false)
  } else {
    errorMsg.value = String(result.error)
  }
}

function handleClose() {
  editorRef.value?.clearCache()
  emit('update:visible', false)
}
</script>

<template>
  <PrimeDialog
    :visible="visible"
    :header="translate('forum', 'profile.editPost')"
    :modal="true"
    :style="{ width: '900px', maxWidth: '95vw' }"
    :closable="!isLoading && !fetchingPost"
    :closeOnEscape="!isLoading && !fetchingPost"
    @update:visible="emit('update:visible', $event)"
  >
    <div v-if="post" class="bf-edit-post">
      <!-- 加载状态 -->
      <div v-if="fetchingPost" class="bf-loading-state">
        <i class="pi pi-spin pi-spinner"></i>
        <span>{{ translate('forum', 'loading') }}</span>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="bf-error-message">
        <svg class="bf-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <span>{{ errorMsg }}</span>
      </div>

      <!-- 表单 -->
      <form v-if="!fetchingPost" @submit.prevent="handleSave" class="bf-form">
        <!-- 标题和类型 - 横向布局 -->
        <div class="bf-form-row">
          <!-- 标题 -->
          <div class="bf-form-field bf-form-field--flex-2">
            <label class="bf-label bf-label--sm">
              <svg class="bf-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 7V4h16v3M9 20h6M12 4v16"/>
              </svg>
              {{ translate('forum', 'title') }}
            </label>
            <input
              v-model="title"
              type="text"
              class="bf-input"
              :placeholder="translate('forum', 'enterTitle')"
              :disabled="isLoading"
            />
          </div>

          <!-- 类型选择 -->
          <div class="bf-form-field bf-form-field--flex-1">
            <label class="bf-label bf-label--sm">
              {{ translate('forum', 'postType') }}
            </label>
            <div class="bf-type-selector">
              <button
                type="button"
                class="bf-type-option"
                :class="{ 'bf-type-option--active': postType === 'TEXT' }"
                :disabled="isLoading"
                @click="postType = 'TEXT'"
              >
                {{ translate('forum', 'textPost') }}
              </button>
              <button
                type="button"
                class="bf-type-option"
                :class="{ 'bf-type-option--active': postType === 'LINK' }"
                :disabled="isLoading"
                @click="postType = 'LINK'"
              >
                {{ translate('forum', 'linkPost') }}
              </button>
            </div>
          </div>
        </div>

        <!-- 分类选择 -->
        <div class="bf-form-field">
          <label class="bf-label bf-label--sm">
            {{ translate('forum', 'postType') }}
          </label>
          <div class="bf-category-selector">
            <button
              v-for="cat in categories"
              :key="cat.id"
              type="button"
              class="bf-category-option"
              :class="{ 'bf-category-option--active': category === cat.id }"
              :disabled="isLoading"
              @click="category = cat.id"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>

        <!-- Markdown 编辑器 -->
        <div v-if="postType === 'TEXT'" class="bf-form-field bf-form-field--editor">
          <MarkdownEditor
            ref="editorRef"
            v-model="text"
            :max-images="9"
            :disabled="isLoading"
            placeholder="支持 Markdown 格式，可上传图片..."
          />
        </div>

        <!-- 链接 -->
        <div v-if="postType === 'LINK'" class="bf-form-field">
          <label class="bf-label bf-label--sm">
            <svg class="bf-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
            {{ translate('forum', 'link') }}
          </label>
          <input
            v-model="link"
            type="url"
            class="bf-input"
            :placeholder="translate('forum', 'enterLink')"
            :disabled="isLoading"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="bf-button-group">
          <button
            type="button"
            @click="handleClose"
            class="bf-btn bf-btn--secondary"
            :disabled="isLoading"
          >
            {{ translate('forum', 'cancel') }}
          </button>
          <button
            type="submit"
            :disabled="isLoading"
            class="bf-btn bf-btn--primary"
            :class="{ 'bf-btn--loading': isLoading }"
          >
            <span v-if="isLoading" class="bf-spinner"></span>
            <span v-else>{{ translate('forum', 'save') }}</span>
          </button>
        </div>
      </form>
    </div>

    <template #footer>
      <div></div>
    </template>
  </PrimeDialog>
</template>

<style scoped>
.bf-edit-post {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xs, 4px) 0;
}

/* 加载状态 */
.bf-loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-xl, 48px);
  color: var(--bf-text-secondary);
}

.bf-loading-state i {
  font-size: 1.5rem;
  color: var(--bf-primary);
}

/* 错误提示 */
.bf-error-message {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--bf-input-radius, 10px);
  color: #ef4444;
  font-size: 0.875rem;
}

.bf-error-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* 表单 */
.bf-form {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-lg, 24px);
}

/* 横向布局 */
.bf-form-row {
  display: flex;
  gap: var(--bf-space-lg, 24px);
  align-items: flex-start;
}

.bf-form-field--flex-2 {
  flex: 2;
}

.bf-form-field--flex-1 {
  flex: 1;
  min-width: 180px;
}

/* 编辑器字段 */
.bf-form-field--editor {
  min-height: 400px;
}

/* 表单字段 */
.bf-form-field {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-xs, 6px);
}

/* 标签 */
.bf-label {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--bf-text-secondary);
}

.bf-label--sm {
  font-size: 0.75rem;
}

.bf-label-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

/* 类型选择器 */
.bf-type-selector {
  display: flex;
  gap: 2px;
  background: var(--bf-surface);
  border-radius: var(--bf-input-radius, 10px);
  padding: 2px;
  border: 1px solid var(--bf-border);
}

.bf-type-option {
  flex: 1;
  padding: var(--bf-space-xs, 6px) var(--bf-space-sm, 12px);
  border: none;
  background: transparent;
  color: var(--bf-text-secondary);
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  border-radius: calc(var(--bf-input-radius, 10px) - 2px);
  transition: all 0.15s ease;
}

.bf-type-option:hover:not(:disabled) {
  color: var(--bf-text);
}

.bf-type-option--active {
  background: var(--bf-primary);
  color: #fff;
}

.bf-type-option:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 分类选择器 */
.bf-category-selector {
  display: flex;
  flex-wrap: wrap;
  gap: var(--bf-space-xs, 6px);
}

.bf-category-option {
  padding: var(--bf-space-xs, 6px) var(--bf-space-sm, 12px);
  border: 1px solid var(--bf-border);
  background: var(--bf-surface);
  color: var(--bf-text-secondary);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  border-radius: var(--bf-radius-md, 8px);
  transition: all 0.15s ease;
}

.bf-category-option:hover:not(:disabled) {
  border-color: var(--bf-primary);
  color: var(--bf-primary);
}

.bf-category-option--active {
  background: var(--bf-primary);
  border-color: var(--bf-primary);
  color: #fff;
}

.bf-category-option:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 输入框 */
.bf-input {
  width: 100%;
  padding: var(--bf-space-sm, 10px) var(--bf-space-md, 14px);
  background: var(--bf-surface);
  border: 1px solid var(--bf-border);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text);
  font-size: 0.875rem;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.bf-input:focus {
  outline: none;
  border-color: var(--bf-primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.bf-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 按钮组 */
.bf-button-group {
  display: flex;
  justify-content: flex-end;
  gap: var(--bf-space-sm, 12px);
  padding-top: var(--bf-space-sm, 8px);
  border-top: 1px solid var(--bf-border);
}

/* 按钮 */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-xs, 6px);
  padding: var(--bf-space-sm, 10px) var(--bf-space-lg, 20px);
  border: none;
  border-radius: var(--bf-btn-radius, 10px);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  min-width: 80px;
}

.bf-btn--primary {
  background: var(--bf-primary);
  color: #fff;
}

.bf-btn--primary:hover:not(:disabled) {
  background: var(--bf-primary-hover, #e55a2b);
}

.bf-btn--secondary {
  background: var(--bf-surface);
  color: var(--bf-text-secondary);
  border: 1px solid var(--bf-border);
}

.bf-btn--secondary:hover:not(:disabled) {
  background: var(--bf-bg-elevated);
  color: var(--bf-text);
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 加载动画 */
.bf-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
