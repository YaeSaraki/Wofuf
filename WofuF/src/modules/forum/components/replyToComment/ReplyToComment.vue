<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'

interface Props {
  postSlug: string  // 改名为 postSlug，表示帖子的 slug
  parentCommentId?: string
}

const props = defineProps<Props>()
const router = useRouter()
const { isAuthenticated, getCurrentUserId } = useAuth()

const emit = defineEmits<{
  replyAdded: []
}>()

/* ---------------- 表单数据 ---------------- */
const replyText = ref('')
const isReplying = ref(false)

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 提交回复
async function submitReply() {
  if (!replyText.value.trim()) {
    return
  }

  // 检查是否已登录
  const userId = getCurrentUserId()
  if (!userId) {
    router.push('forum/login')
    return
  }

  const result = await executeAsync(async () => {
    if (props.parentCommentId) {
      // 回复评论
      return await forumService.replyToComment(props.parentCommentId, {
        postSlug: props.postSlug,
        userId,
        comment: replyText.value.trim(),
      })
    } else {
      // 回复帖子 - 使用新的 slug API
      return await forumService.replyToPostBySlug(props.postSlug, {
        userId,
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
}

// 开始回复
function startReply() {
  if (!isAuthenticated()) {
    router.push('forum/login')
    return
  }
  isReplying.value = true
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
      <div class="bf-reply-header">
        <svg class="bf-reply-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="bf-reply-title">添加评论</span>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="bf-error-message">
        <span>{{ errorMsg }}</span>
      </div>

      <textarea
        v-model="replyText"
        rows="4"
        class="bf-textarea"
        :placeholder="translate('forum', 'enterReply')"
      ></textarea>

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
  padding: var(--bf-space-lg, 24px);
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

.bf-reply-header {
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
}

.bf-error-message {
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--bf-input-radius, 10px);
  color: #ef4444;
  font-size: 0.875rem;
}

.bf-textarea {
  width: 100%;
  padding: var(--bf-space-md, 16px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  line-height: 1.6;
  resize: vertical;
  transition: all var(--bf-transition-fast, 0.15s ease);
  outline: none;
}

.bf-textarea::placeholder {
  color: var(--bf-text-muted);
}

.bf-textarea:focus {
  border-color: var(--bf-primary, #FF6B35);
  box-shadow: 0 0 0 3px var(--bf-input-focus, rgba(255, 107, 53, 0.3));
}

.bf-reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--bf-space-sm, 8px);
}

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

.bf-btn--ghost {
  background: transparent;
  color: var(--bf-text-secondary);
}

.bf-btn--ghost:hover {
  color: var(--bf-text-primary);
  background: var(--bf-btn-secondary-bg, rgba(255, 255, 255, 0.04));
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-btn--loading {
  position: relative;
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
</style>
