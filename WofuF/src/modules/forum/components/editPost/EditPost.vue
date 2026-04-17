<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { forumService } from '@M/forum/services/ForumService.ts'
import { imageService } from '@M/forum/services/ImageService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import MarkdownEditor from '@M/forum/components/markdownEditor/MarkdownEditor.vue'
import { currentLocale } from '@S/services/i18n/useLocale'

const router = useRouter()
const route = useRoute()
const toast = useToast()

/* ---------------- 编辑器引用 ---------------- */
const editorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null)

/* ---------------- 表单数据 ---------------- */
const formData = ref({
  title: '',
  type: 'TEXT' as 'TEXT' | 'LINK',
  text: '',
  link: '',
  category: 'DISCUSSION',
  postId: '',
  slug: '',
})

/* ---------------- 状态 ---------------- */
const submitLoading = ref(false)
const fetchLoading = ref(true)
const formErrors = ref({
  title: '',
  content: '',
})

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

/* ---------------- 图片数量 ---------------- */
const imageCount = computed(() => imageService.countImages(formData.value.text || ''))

/* ---------------- 复用通用加载逻辑 ---------------- */
const { errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 获取帖子数据 ---------------- */
async function fetchPost() {
  const slug = route.params.slug as string
  if (!slug) {
    toast.add({ severity: 'error', summary: '帖子不存在', life: 3000 })
    router.push('/forum')
    return
  }

  fetchLoading.value = true
  const result = await forumService.getPostBySlug(slug)

  if (result.isFailure) {
    toast.add({ severity: 'error', summary: String(result.error), life: 3000 })
    router.push('/forum')
    return
  }

  const post = result.getValue().post
  formData.value = {
    title: post.title,
    type: post.type === 'LINK' ? 'LINK' : 'TEXT',
    text: post.text || '',
    link: post.link || '',
    category: post.category || 'DISCUSSION',
    postId: post.postId || '',
    slug: post.slug,
  }
  fetchLoading.value = false
}

/* ---------------- 验证表单 ---------------- */
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

/* ---------------- 保存编辑 ---------------- */
async function savePost() {
  if (!validateForm()) return

  submitLoading.value = true
  errorMsg.value = null

  try {
    const apiResult = await forumService.editPost(formData.value.postId, {
      title: formData.value.title.trim(),
      text: formData.value.text?.trim() || undefined,
      link: formData.value.link?.trim() || undefined,
      category: formData.value.category,
    })

    if (apiResult.isFailure) {
      toast.add({
        severity: 'error',
        summary: String(apiResult.error) || '更新失败',
        life: 3000,
      })
      return
    }

    editorRef.value?.clearCache()
    toast.add({
      severity: 'success',
      summary: translate('forum', 'profile.editSuccess') || '更新成功',
      life: 3000,
    })
    router.push(`/forum/posts/${formData.value.slug}`)
  } catch (err: any) {
    toast.add({
      severity: 'error',
      summary: err?.message || '更新失败',
      life: 3000,
    })
  } finally {
    submitLoading.value = false
  }
}

/* ---------------- 重置表单 ---------------- */
function resetForm() {
  fetchPost() // 重新获取原始数据
}

/* ---------------- 取消编辑 ---------------- */
function cancelEdit() {
  router.push(`/forum/posts/${formData.value.slug}`)
}

onMounted(() => {
  fetchPost()
})
</script>

<template>
  <div class="bf-edit-post">
    <div class="bf-edit-post-container">
      <!-- 页面标题 -->
      <div class="bf-page-header">
        <h1 class="bf-page-title">
          <span class="bf-text-gradient">{{ translate('forum', 'profile.editPost') }}</span>
        </h1>
      </div>

      <!-- 加载状态 -->
      <div v-if="fetchLoading" class="bf-loading-state">
        <i class="pi pi-spin pi-spinner"></i>
        <span>{{ translate('forum', 'loading') }}</span>
      </div>

      <!-- 表单 -->
      <form v-if="!fetchLoading" @submit.prevent="savePost" class="bf-form">
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
              class="bf-input"
              :class="{ 'bf-input--error': formErrors.title }"
              :placeholder="translate('forum', 'enterTitle')"
              :disabled="submitLoading"
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
                :disabled="submitLoading"
                @click="formData.type = 'TEXT'"
              >
                {{ translate('forum', 'textPost') }}
              </button>
              <button
                type="button"
                class="bf-type-option"
                :class="{ 'bf-type-option--active': formData.type === 'LINK' }"
                :disabled="submitLoading"
                @click="formData.type = 'LINK'"
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
              :class="{ 'bf-category-option--active': formData.category === cat.id }"
              :disabled="submitLoading"
              @click="formData.category = cat.id"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>

        <!-- Markdown 编辑器 -->
        <div v-if="formData.type === 'TEXT'" class="bf-form-field bf-form-field--editor">
          <MarkdownEditor
            ref="editorRef"
            v-model="formData.text"
            :max-images="9"
            cache-key="edit_post"
            :disabled="submitLoading"
            placeholder="支持 Markdown 格式，可上传图片..."
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
            :disabled="submitLoading"
          />
          <span v-if="formErrors.content" class="bf-field-error">{{ formErrors.content }}</span>
        </div>

        <!-- 操作按钮 -->
        <div class="bf-button-group">
          <button
            type="button"
            @click="cancelEdit"
            class="bf-btn bf-btn--secondary"
            :disabled="submitLoading"
          >
            {{ translate('forum', 'cancel') }}
          </button>
          <button
            type="submit"
            :disabled="submitLoading"
            class="bf-btn bf-btn--primary"
            :class="{ 'bf-btn--loading': submitLoading }"
          >
            <span v-if="submitLoading" class="bf-spinner"></span>
            <span v-else>{{ translate('forum', 'save') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.bf-edit-post {
  min-height: 100vh;
  padding: var(--bf-space-lg, 24px);
}

.bf-edit-post-container {
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
  font-size: 2rem;
  color: var(--bf-primary);
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

/* 编辑器字段 */
.bf-form-field--editor {
  min-height: 500px;
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

.bf-input--error {
  border-color: var(--bf-danger, #ef4444);
}

/* 错误提示 */
.bf-field-error {
  font-size: 0.75rem;
  color: var(--bf-danger, #ef4444);
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
  min-width: 100px;
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
