<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import type { CreatePostRequest } from '@M/forum/dtos/Post.ts'

const router = useRouter()
const { isAuthenticated, getCurrentUserId } = useAuth()

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
  
  return isValid
}

// 提交创建帖子
async function createPost() {
  if (!validateForm()) return
  
  // 检查是否已登录
  const userId = getCurrentUserId()
  if (!userId) {
    router.push('/forum/login')
    return
  }
  
  formData.value.userId = userId

  const result = await executeAsync(async () => {
    const apiResult = await forumService.createPost(formData.value)

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error(String(apiResult.error) || '创建帖子失败')
  }, translate('forum', 'createPostFailed'))

  if (result) {
    // 跳转到新创建的帖子
    router.push(`/forum/posts/${result.slug}`)
  }
}

// 重置表单
function resetForm() {
  formData.value = {
    userId: '',
    title: '',
    type: 'TEXT',
    text: '',
    link: '',
  }
  formErrors.value = { title: '', content: '' }
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!isAuthenticated()) {
    router.push('/forum/login')
  }
})
</script>

<template>
  <div class="bf-create-post">
    <div class="bf-create-post-container">
      <!-- 页面标题 -->
      <div class="bf-page-header">
        <h1 class="bf-page-title">
          <span class="bf-text-gradient">{{ translate('forum', 'create_post') }}</span>
        </h1>
        <p class="bf-page-subtitle">分享你的想法和发现</p>
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

        <!-- 标题 -->
        <div class="bf-form-field">
          <label class="bf-label">
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
        <div class="bf-form-field">
          <label class="bf-label">
            <svg class="bf-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
            </svg>
            {{ translate('forum', 'postType') }}
          </label>
          <div class="bf-type-selector">
            <button
              type="button"
              class="bf-type-option"
              :class="{ 'bf-type-option--active': formData.type === 'TEXT' }"
              @click="formData.type = 'TEXT'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
              <span>{{ translate('forum', 'textPost') }}</span>
            </button>
            <button
              type="button"
              class="bf-type-option"
              :class="{ 'bf-type-option--active': formData.type === 'LINK' }"
              @click="formData.type = 'LINK'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              <span>{{ translate('forum', 'linkPost') }}</span>
            </button>
          </div>
        </div>

        <!-- 文本内容 -->
        <div v-if="formData.type === 'TEXT'" class="bf-form-field">
          <label class="bf-label">
            <svg class="bf-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <line x1="17" y1="10" x2="3" y2="10"/>
              <line x1="21" y1="6" x2="3" y2="6"/>
              <line x1="21" y1="14" x2="3" y2="14"/>
              <line x1="17" y1="18" x2="3" y2="18"/>
            </svg>
            {{ translate('forum', 'content') }}
          </label>
          <textarea
            v-model="formData.text"
            rows="8"
            class="bf-input bf-textarea"
            :class="{ 'bf-input--error': formErrors.content }"
            :placeholder="translate('forum', 'enterContent')"
          ></textarea>
          <span v-if="formErrors.content" class="bf-field-error">{{ formErrors.content }}</span>
        </div>

        <!-- 链接 -->
        <div v-if="formData.type === 'LINK'" class="bf-form-field">
          <label class="bf-label">
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
            type="submit"
            :disabled="isLoading"
            class="bf-btn bf-btn--primary bf-btn--lg"
            :class="{ 'bf-btn--loading': isLoading }"
          >
            <span v-if="isLoading" class="bf-spinner"></span>
            <span v-else>{{ translate('forum', 'create') }}</span>
          </button>
          <button
            type="button"
            @click="resetForm"
            class="bf-btn bf-btn--secondary bf-btn--lg"
          >
            {{ translate('forum', 'reset') }}
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
  max-width: 680px;
  margin: 0 auto;
}

/* 页面标题 */
.bf-page-header {
  margin-bottom: var(--bf-space-xl, 32px);
}

.bf-page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 var(--bf-space-sm, 8px);
}

.bf-text-gradient {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.bf-page-subtitle {
  color: var(--bf-text-secondary, #B3B3B3);
  font-size: 0.875rem;
  margin: 0;
}

/* 表单 */
.bf-form {
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-xl, 32px);
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-lg, 24px);
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
  gap: var(--bf-space-sm, 8px);
}

.bf-label {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--bf-text-secondary);
}

.bf-label-icon {
  width: 16px;
  height: 16px;
  color: var(--bf-primary, #FF6B35);
}

.bf-input {
  width: 100%;
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
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

.bf-input:hover:not(:focus) {
  border-color: rgba(255, 255, 255, 0.15);
}

.bf-input--error {
  border-color: #ef4444 !important;
}

.bf-field-error {
  font-size: 0.75rem;
  color: #ef4444;
}

.bf-textarea {
  resize: vertical;
  min-height: 200px;
  line-height: 1.6;
}

/* 类型选择器 */
.bf-type-selector {
  display: flex;
  gap: var(--bf-space-sm, 8px);
}

.bf-type-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-type-option:hover {
  border-color: rgba(255, 255, 255, 0.15);
  color: var(--bf-text-primary);
}

.bf-type-option--active {
  background: var(--bf-fire-gradient-subtle, linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 159, 28, 0.1) 100%));
  border-color: var(--bf-primary, #FF6B35);
  color: var(--bf-primary, #FF6B35);
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

.bf-btn--lg {
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 24px);
  font-size: 1rem;
  font-weight: 600;
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-btn--loading {
  position: relative;
}

.bf-spinner {
  width: 18px;
  height: 18px;
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
  gap: var(--bf-space-md, 16px);
}

.bf-button-group > * {
  flex: 1;
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-form {
    padding: var(--bf-space-lg, 24px);
  }

  .bf-type-selector {
    flex-direction: column;
  }

  .bf-button-group {
    flex-direction: column;
  }
}
</style>
