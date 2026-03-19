<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { forumService } from '@M/forum/services/ForumService.ts'
import { imageService } from '@M/forum/services/ImageService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import type { CreatePostRequest } from '@M/forum/dtos/Post.ts'
import MarkdownEditor, { type DraftInfo } from '@M/forum/components/markdownEditor/MarkdownEditor.vue'
import DraftToast, { type DraftToastData } from '@S/components/DraftToast.vue'

const router = useRouter()
const toast = useToast()
const { isAuthenticated, getCurrentUserId } = useAuth()

/* ---------------- 编辑器引用 ---------------- */
const editorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null)

/* ---------------- 表单数据 ---------------- */
const formData = ref<CreatePostRequest>({
  userId: '',
  title: '',
  type: 'TEXT',
  text: '',
  link: '',
})

/* ---------------- 表单验证 ---------------- */
const formErrors = ref({
  title: '',
  content: '',
})

/* ---------------- 图片数量 ---------------- */
const imageCount = computed(() => imageService.countImages(formData.value.text || ''))

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 验证表单
function validateForm(): boolean {
  let isValid = true
  formErrors.value = { title: '', content: '' }

  if (!formData.value.title.trim()) {
    formErrors.value.title = translate('forum', 'titleRequired')
    isValid = false
  }

  if (formData.value.type === 'TEXT' && !formData.value.text?.trim()) {
    formErrors.value.content = translate('forum', 'contentRequired')
    isValid = false
  }

  if (formData.value.type === 'LINK' && !formData.value.link?.trim()) {
    formErrors.value.content = translate('forum', 'linkRequired')
    isValid = false
  }

  if (imageCount.value > 9) {
    formErrors.value.content = '图片数量超出限制，最多允许 9 张'
    isValid = false
  }

  return isValid
}

// 提交创建帖子
async function createPost() {
  if (!validateForm()) return

  const userId = getCurrentUserId()
  if (!userId) {
    router.push('/forum/login')
    return
  }

  const requestData: CreatePostRequest = {
    userId: userId,
    title: formData.value.title.trim(),
    type: formData.value.type,
    text: formData.value.text?.trim() || undefined,
    link: formData.value.link?.trim() || undefined,
  }

  const result = await executeAsync(async () => {
    const apiResult = await forumService.createPost(requestData)
    if (apiResult.isSuccess) return apiResult.getValue()
    throw new Error(String(apiResult.error) || '创建帖子失败')
  }, translate('forum', 'createPostFailed'))

  if (result) {
    editorRef.value?.clearCache()
    router.push(`/forum/posts/${result.slug}`)
  }
}

// 重置表单
function resetForm() {
  formData.value = { userId: '', title: '', type: 'TEXT', text: '', link: '' }
  formErrors.value = { title: '', content: '' }
  editorRef.value?.clearCache()
}

// 处理编辑器错误
function handleEditorError(message: string) {
  formErrors.value.content = message
}

// 当前草稿信息
const currentDraft = ref<DraftInfo | null>(null)

// 草稿提示显示状态
const draftToastVisible = ref(false)
const draftToastData = ref<DraftToastData | null>(null)

// 处理草稿可用事件
function handleDraftAvailable(draft: DraftInfo) {
  console.log('[CreatePost] handleDraftAvailable called, draft:', draft)
  currentDraft.value = draft

  // 显示自定义草稿提示
  draftToastData.value = {
    savedAt: draft.savedAt,
    preview: draft.preview,
    onRestore: () => restoreDraft(),
    onDiscard: () => discardDraft(),
  }
  draftToastVisible.value = true
  console.log('[CreatePost] draftToastVisible set to true, draftToastData:', draftToastData.value)
}

// 关闭草稿提示
function closeDraftToast() {
  draftToastVisible.value = false
}

// 恢复草稿
function restoreDraft() {
  if (editorRef.value?.loadFromCache()) {
    toast.add({
      severity: 'success',
      summary: '草稿已恢复',
      life: 3000
    })
  }
  currentDraft.value = null
  draftToastVisible.value = false
}

// 丢弃草稿
function discardDraft() {
  editorRef.value?.clearCache()
  currentDraft.value = null
  draftToastVisible.value = false
  toast.add({
    severity: 'success',
    summary: '草稿已丢弃',
    life: 3000
  })
}

onMounted(() => {
  if (!isAuthenticated()) router.push('/forum/login')
})
</script>

<template>
  <div class="bf-create-post">
    <!-- 草稿恢复提示 -->
    <DraftToast
      :visible="draftToastVisible"
      :data="draftToastData"
      @close="closeDraftToast"
    />

    <div class="bf-create-post-container">
      <!-- 页面标题 -->
      <div class="bf-page-header">
        <h1 class="bf-page-title">
          <span class="bf-text-gradient">{{ translate('forum', 'create_post') }}</span>
        </h1>
      </div>

      <form @submit.prevent="createPost" class="bf-form">
        <!-- 错误提示 -->
        <div v-if="errorMsg" class="bf-error-message">
          <svg class="bf-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMsg }}</span>
        </div>

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
              v-model="formData.title"
              type="text"
              required
              class="bf-input"
              :class="{ 'bf-input--error': formErrors.title }"
              :placeholder="translate('forum', 'enterTitle')"
            />
            <span v-if="formErrors.title" class="bf-field-error">{{ formErrors.title }}</span>
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
                :class="{ 'bf-type-option--active': formData.type === 'TEXT' }"
                @click="formData.type = 'TEXT'"
              >
                <span>{{ translate('forum', 'textPost') }}</span>
              </button>
              <button
                type="button"
                class="bf-type-option"
                :class="{ 'bf-type-option--active': formData.type === 'LINK' }"
                @click="formData.type = 'LINK'"
              >
                <span>{{ translate('forum', 'linkPost') }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Markdown 编辑器 -->
        <div v-if="formData.type === 'TEXT'" class="bf-form-field bf-form-field--editor">
          <MarkdownEditor
            ref="editorRef"
            v-model="formData.text!"
            :max-images="9"
            cache-key="new_post"
            placeholder="支持 Markdown 格式，可上传图片..."
            @error="handleEditorError"
            @draft-available="handleDraftAvailable"
          />
          <span v-if="formErrors.content" class="bf-field-error">{{ formErrors.content }}</span>
        </div>

        <!-- 链接 -->
        <div v-if="formData.type === 'LINK'" class="bf-form-field">
          <label class="bf-label bf-label--sm">
            <svg class="bf-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
            {{ translate('forum', 'link') }}
          </label>
          <input
            v-model="formData.link"
            type="url"
            class="bf-input"
            :class="{ 'bf-input--error': formErrors.content }"
            :placeholder="translate('forum', 'enterLink')"
          />
          <span v-if="formErrors.content" class="bf-field-error">{{ formErrors.content }}</span>
        </div>

        <!-- 操作按钮 -->
        <div class="bf-button-group">
          <button
            type="button"
            @click="resetForm"
            class="bf-btn bf-btn--secondary"
          >
            {{ translate('forum', 'reset') }}
          </button>
          <button
            type="submit"
            :disabled="isLoading"
            class="bf-btn bf-btn--primary"
            :class="{ 'bf-btn--loading': isLoading }"
          >
            <span v-if="isLoading" class="bf-spinner"></span>
            <span v-else>{{ translate('forum', 'create') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.bf-create-post {
  min-height: 100vh;
  padding: var(--bf-space-lg, 24px);
}

.bf-create-post-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面标题 */
.bf-page-header {
  margin-bottom: var(--bf-space-lg, 24px);
}

.bf-page-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.bf-text-gradient {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 表单 */
.bf-form {
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-lg, 24px);
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

/* 编辑器字段 - 更大的最小高度 */
.bf-form-field--editor {
  min-height: 500px;
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

.bf-form-field {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-xs, 6px);
}

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
  color: var(--bf-primary, #FF6B35);
}

.bf-input {
  width: 100%;
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 12px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  transition: all var(--bf-transition-fast, 0.15s ease);
  outline: none;
}

.bf-input::placeholder {
  color: var(--bf-text-muted);
}

.bf-input:focus {
  border-color: var(--bf-primary, #FF6B35);
  box-shadow: 0 0 0 3px var(--bf-input-focus, rgba(255, 107, 53, 0.3));
}

.bf-input--error {
  border-color: #ef4444 !important;
}

.bf-field-error {
  font-size: 0.75rem;
  color: #ef4444;
}

/* 类型选择器 */
.bf-type-selector {
  display: flex;
  gap: var(--bf-space-xs, 4px);
}

.bf-type-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-xs, 4px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-sm, 8px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 8px);
  color: var(--bf-text-secondary);
  font-size: 0.8125rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-type-option:hover {
  border-color: rgba(255, 255, 255, 0.15);
  color: var(--bf-text-primary);
}

.bf-type-option--active {
  background: rgba(255, 107, 53, 0.1);
  border-color: var(--bf-primary, #FF6B35);
  color: var(--bf-primary, #FF6B35);
}

/* 按钮 */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-lg, 20px);
  border-radius: var(--bf-btn-radius, 10px);
  font-weight: 500;
  font-size: 0.875rem;
  transition: all var(--bf-transition-fast, 0.15s ease);
  cursor: pointer;
  border: none;
  outline: none;
}

.bf-btn--primary {
  background: var(--bf-btn-primary-bg, linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%));
  color: white;
}

.bf-btn--primary:hover:not(:disabled) {
  background: var(--bf-btn-primary-hover, linear-gradient(135deg, #FF8C5A 0%, #FFAD6B 100%));
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
}

.bf-btn--secondary {
  background: var(--bf-btn-secondary-bg, rgba(255, 255, 255, 0.06));
  color: var(--bf-text-primary);
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-btn--secondary:hover:not(:disabled) {
  background: var(--bf-btn-secondary-hover, rgba(255, 255, 255, 0.1));
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

.bf-button-group {
  display: flex;
  gap: var(--bf-space-sm, 12px);
  justify-content: flex-end;
}

/* 响应式 - 平板 */
@media (max-width: 768px) {
  .bf-form-field--editor {
    min-height: 400px;
  }
}

/* 响应式 - 手机 */
@media (max-width: 640px) {
  .bf-create-post {
    padding: var(--bf-space-md, 16px);
  }

  .bf-form {
    padding: var(--bf-space-md, 16px);
    gap: var(--bf-space-md, 16px);
  }

  .bf-page-title {
    font-size: 1.25rem;
  }

  .bf-form-row {
    flex-direction: column;
    gap: var(--bf-space-md, 16px);
  }

  .bf-form-field--flex-1 {
    min-width: auto;
  }

  .bf-form-field--editor {
    min-height: 350px;
  }

  .bf-type-selector {
    flex-direction: row;
  }

  .bf-button-group {
    flex-direction: row;
  }

  .bf-button-group > * {
    flex: 1;
  }
}
</style>
