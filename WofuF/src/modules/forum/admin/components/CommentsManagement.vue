<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useToast } from 'primevue/usetoast'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import type { CommentSummary } from '@M/forum/admin/dtos/Admin.ts'
import { translate } from '@S/services/i18n'
import router from '@S/infra/router'
import { cacheService } from '@S/infra/cache'

const toast = useToast()

// ==================== 状态 ====================
const comments = ref<CommentSummary[]>([])
const isLoading = ref(false)
const errorMsg = ref('')
const currentPage = ref(0)
const pageSize = ref(20)
const totalComments = ref(0)
const searchQuery = ref('')
const contentSearchQuery = ref('')

// 操作中的评论ID
const operatingCommentId = ref<string | null>(null)

// 批量选择状态
const selectedCommentIds = ref<Set<string>>(new Set())
const isBatchOperating = ref(false)

// ==================== 计算属性 ====================
const totalPages = computed(() => Math.ceil(totalComments.value / pageSize.value))

// ==================== 辅助函数 ====================

// 格式化时间
function formatTime(timestamp: number | null): string {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return translate('forum', 'time.justNow')
  if (diffMins < 60) return `${diffMins} ${translate('forum', 'time.minutesAgo')}`
  if (diffHours < 24) return `${diffHours} ${translate('forum', 'time.hoursAgo')}`
  if (diffDays < 7) return `${diffDays} ${translate('forum', 'time.daysAgo')}`

  return date.toLocaleDateString()
}

// 判断评论是否隐藏
function isHidden(comment: CommentSummary): boolean {
  // 确保返回严格的布尔值
  // 兼容后端省略 false 字段（undefined）或字段名差异（hidden / status）的情况
  return Boolean(
    comment.isHidden === true || (comment as any).hidden === true || (comment as any).status === 'HIDDEN',
  )
}

// 跳转到帖子
function goToPost(slug: string) {
  if (!slug) return
  sessionStorage.setItem('forum_post_from', '/forum/admin')
  router.push(`/forum/posts/${slug}`)
}

// ==================== 数据加载 ====================
async function loadComments() {
  isLoading.value = true
  errorMsg.value = ''

  // 清除缓存以获取最新数据
  cacheService.clearModule('forum_service')

  try {
    const result = await adminService.getComments(
      currentPage.value,
      pageSize.value,
      searchQuery.value.trim() || undefined,
      contentSearchQuery.value.trim() || undefined,
      true, // 包含隐藏评论，用于管理
    )

    if (result.isSuccess) {
      comments.value = result.getValue().comments
      totalComments.value = result.getValue().total
      // 清空选择
      selectedCommentIds.value.clear()
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
    }
  } catch (e) {
    errorMsg.value = translate('forum', 'error')
  } finally {
    isLoading.value = false
  }
}

// ==================== 评论操作（与 PostsManagement 完全一致的逻辑） ====================
// 隐藏/显示评论
async function toggleHideComment(commentId: string, isCurrentlyHidden: boolean) {
  if (!commentId) return

  // 显示提示
  toast.add({
    severity: 'info',
    summary: isCurrentlyHidden
      ? translate('forum', 'admin.showing')
      : translate('forum', 'admin.hiding'),
    life: 2000,
  })

  // 防止重复点击
  operatingCommentId.value = commentId

  try {
    // 调用 API
    const result = isCurrentlyHidden
      ? await adminService.showComment(commentId)
      : await adminService.hideComment(commentId)

    if (result.isSuccess) {
      toast.add({
        severity: 'success',
        summary: isCurrentlyHidden
          ? translate('forum', 'admin.showSuccess')
          : translate('forum', 'admin.hideSuccess'),
        life: 2000,
      })
      // 重新加载评论列表，确保数据一致
      await loadComments()
    } else {
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingCommentId.value = null
  }
}

// ==================== 批量选择操作 ====================
// 切换评论选择状态
function toggleCommentSelection(commentId: string) {
  if (selectedCommentIds.value.has(commentId)) {
    selectedCommentIds.value.delete(commentId)
  } else {
    selectedCommentIds.value.add(commentId)
  }
  // 触发响应式更新
  selectedCommentIds.value = new Set(selectedCommentIds.value)
}

// 全选/取消全选
function toggleSelectAll() {
  if (selectedCommentIds.value.size === comments.value.length) {
    selectedCommentIds.value.clear()
  } else {
    selectedCommentIds.value = new Set(comments.value.map(c => c.commentId))
  }
  selectedCommentIds.value = new Set(selectedCommentIds.value)
}

// 是否全选
const isAllSelected = computed(() => {
  return comments.value.length > 0 && selectedCommentIds.value.size === comments.value.length
})

// 是否有选中
const hasSelection = computed(() => selectedCommentIds.value.size > 0)

// 批量隐藏
async function batchHide() {
  if (!hasSelection.value) return

  isBatchOperating.value = true
  const ids = Array.from(selectedCommentIds.value)

  toast.add({
    severity: 'info',
    summary: translate('forum', 'admin.hiding'),
    life: 2000,
  })

  try {
    const result = await adminService.batchHideComments(ids)
    if (result.isSuccess) {
      const data = result.getValue()
      toast.add({
        severity: 'success',
        summary: `批量隐藏完成: ${data.successCount} 成功, ${data.failCount} 失败`,
        life: 3000,
      })
      await loadComments()
    } else {
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    isBatchOperating.value = false
  }
}

// 批量显示
async function batchShow() {
  if (!hasSelection.value) return

  isBatchOperating.value = true
  const ids = Array.from(selectedCommentIds.value)

  toast.add({
    severity: 'info',
    summary: translate('forum', 'admin.showing'),
    life: 2000,
  })

  try {
    const result = await adminService.batchShowComments(ids)
    if (result.isSuccess) {
      const data = result.getValue()
      toast.add({
        severity: 'success',
        summary: `批量显示完成: ${data.successCount} 成功, ${data.failCount} 失败`,
        life: 3000,
      })
      await loadComments()
    } else {
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    isBatchOperating.value = false
  }
}

// ==================== 分页 ====================
function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadComments()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadComments()
  }
}

// 防抖搜索
let searchTimeout: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadComments()
  }, 300)
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="admin-container">
    <!-- 页面标题栏 -->
    <div class="admin-header">
      <div class="admin-header__title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
        <h1>{{ translate('forum', 'admin.commentsManagement') }}</h1>
      </div>
      <span class="admin-header__count"
        >{{ translate('forum', 'comments') }}: {{ totalComments }}</span
      >
    </div>

    <!-- 工具栏 -->
    <div class="admin-toolbar">
      <!-- 作者搜索 -->
      <div class="admin-search">
        <svg
          class="admin-search__icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="translate('forum', 'searchByAuthor') || '按作者搜索'"
          class="admin-search__input"
          @input="onSearchInput"
        />
      </div>

      <!-- 内容搜索 -->
      <div class="admin-search">
        <svg
          class="admin-search__icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
        <input
          v-model="contentSearchQuery"
          type="text"
          placeholder="按内容搜索..."
          class="admin-search__input"
          @input="onSearchInput"
        />
      </div>

      <!-- 刷新按钮 -->
      <button class="admin-btn admin-btn--secondary" @click="loadComments" :disabled="isLoading">
        <svg
          class="admin-btn__icon"
          :class="{ spinning: isLoading }"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M23 4v6h-6M1 20v-6h6" />
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
        </svg>
        {{ translate('forum', 'refresh') }}
      </button>
    </div>

    <!-- 批量操作栏 -->
    <Transition name="bf-slide">
      <div v-if="hasSelection" class="admin-batch-bar">
        <div class="admin-batch-bar__info">
          <span class="admin-batch-bar__count">{{ selectedCommentIds.size }}</span>
          <span class="admin-batch-bar__label">已选择</span>
        </div>
        <div class="admin-batch-bar__actions">
          <button
            class="admin-btn admin-btn--hide"
            @click="batchHide"
            :disabled="isBatchOperating"
          >
            <span class="admin-btn__symbol">⊘</span>
            隐藏
          </button>
          <button
            class="admin-btn admin-btn--show"
            @click="batchShow"
            :disabled="isBatchOperating"
          >
            <span class="admin-btn__symbol">⊙</span>
            显示
          </button>
          <button
            class="admin-btn admin-btn--cancel"
            @click="selectedCommentIds.clear(); selectedCommentIds = new Set()"
          >
            <span class="admin-btn__symbol">×</span>
          </button>
        </div>
      </div>
    </Transition>

    <!-- 错误提示 -->
    <div v-if="errorMsg" class="admin-error">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        />
      </svg>
      <span>{{ errorMsg }}</span>
      <button class="admin-btn admin-btn--ghost" @click="errorMsg = ''">×</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && comments.length === 0" class="admin-loading">
      <div class="admin-loading__spinner"></div>
      <span>{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!isLoading && comments.length === 0" class="admin-empty">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
        />
      </svg>
      <h3>{{ translate('forum', 'noComments') }}</h3>
    </div>

    <!-- 评论列表 -->
    <div v-else class="admin-list">
      <div
        v-for="comment in comments"
        :key="comment.commentId"
        class="admin-card"
        :class="{
          'admin-card--hidden': isHidden(comment),
          'admin-card--selected': selectedCommentIds.has(comment.commentId)
        }"
      >
        <!-- 选择框 -->
        <div class="admin-card__checkbox">
          <input
            type="checkbox"
            :checked="selectedCommentIds.has(comment.commentId)"
            @click.stop="toggleCommentSelection(comment.commentId)"
            class="admin-checkbox"
          />
        </div>

        <!-- 卡片主体 -->
        <div class="admin-card__main" @click="goToPost(comment.postSlug || comment.postId)">
          <!-- 顶部：内容预览 + 徽章 -->
          <div class="admin-card__header">
            <p class="admin-card__text">{{ comment.content }}</p>
            <!-- 徽章 -->
            <div class="admin-card__badges">
              <span v-if="isHidden(comment)" class="admin-badge admin-badge--hidden" title="已隐藏">◉</span>
            </div>
          </div>

          <!-- 底部：作者信息 -->
          <div class="admin-card__meta">
            <span class="admin-card__author">{{ comment.authorNickname || comment.authorId }}</span>
            <span class="admin-card__dot">·</span>
            <span class="admin-card__time">{{ formatTime(comment.createdAt) }}</span>
            <span class="admin-card__dot">·</span>
            <span
              class="admin-card__post-link-text"
              @click.stop="goToPost(comment.postSlug || comment.postId)"
            >
              帖子: {{ comment.postId.substring(0, 8) }}...
            </span>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="admin-card__actions">
          <!-- 隐藏/显示按钮 -->
          <button
            class="admin-action-btn"
            :class="{ 'admin-action-btn--active': isHidden(comment) }"
            @click.stop="toggleHideComment(comment.commentId, isHidden(comment))"
            :disabled="operatingCommentId === comment.commentId"
            :title="
              isHidden(comment)
                ? translate('forum', 'admin.show')
                : translate('forum', 'admin.hide')
            "
          >
            <!-- 隐藏状态：显示图标 (可恢复) -->
            <svg
              v-if="isHidden(comment)"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            <!-- 正常状态：显示图标 -->
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"
              />
              <line x1="1" y1="1" x2="23" y2="23" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="admin-pagination">
      <button
        class="admin-btn admin-btn--secondary"
        :disabled="currentPage === 0"
        @click="prevPage"
      >
        {{ translate('forum', 'prevPage') }}
      </button>
      <span class="admin-pagination__info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        class="admin-btn admin-btn--secondary"
        :disabled="currentPage >= totalPages - 1"
        @click="nextPage"
      >
        {{ translate('forum', 'nextPage') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
/* === 页面容器 === */
.admin-container {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 页面标题栏 === */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
}

.admin-header__title {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
}

.admin-header__title svg {
  width: 24px;
  height: 24px;
  color: var(--bf-primary);
}

.admin-header__title h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0;
}

.admin-header__count {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 工具栏 === */
.admin-toolbar {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  flex-wrap: wrap;
}

/* 搜索框 */
.admin-search {
  flex: 1;
  min-width: 180px;
  position: relative;
}

.admin-search__icon {
  position: absolute;
  left: 0.875rem;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--bf-text-muted);
  pointer-events: none;
}

.admin-search__input {
  width: 100%;
  padding: 0.625rem 1rem 0.625rem 2.5rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  outline: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-search__input::placeholder {
  color: var(--bf-text-muted);
}

.admin-search__input:focus {
  border-color: var(--bf-border-accent);
  background: var(--bf-btn-secondary-bg);
}

/* === 批量操作栏 === */
.admin-batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-sm, 12px) var(--bf-space-lg, 20px);
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 159, 28, 0.08) 100%);
  border: 1px solid rgba(255, 107, 53, 0.25);
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
}

.admin-batch-bar__info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-batch-bar__count {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  background: var(--bf-fire-gradient);
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.admin-batch-bar__label {
  font-size: 14px;
  color: var(--bf-text-secondary);
}

.admin-batch-bar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 符号按钮 */
.admin-btn__symbol {
  font-size: 16px;
  line-height: 1;
}

/* 按钮变体 */
.admin-btn--hide {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 100px;
  color: #a78bfa;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.admin-btn--hide:hover:not(:disabled) {
  background: rgba(139, 92, 246, 0.25);
  border-color: rgba(139, 92, 246, 0.5);
  color: #c4b5fd;
}

.admin-btn--show {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: rgba(34, 197, 94, 0.12);
  border: 1px solid rgba(34, 197, 94, 0.25);
  border-radius: 100px;
  color: #4ade80;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.admin-btn--show:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
  color: #86efac;
}

.admin-btn--cancel {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  background: transparent;
  border: 1px solid var(--bf-border-default);
  border-radius: 100px;
  color: var(--bf-text-muted);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.admin-btn--cancel:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--bf-border-accent);
  color: var(--bf-text-primary);
}

/* 批量操作栏过渡动画 */
.bf-slide-enter-active,
.bf-slide-leave-active {
  transition: all 0.25s ease;
}

.bf-slide-enter-from,
.bf-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 按钮 */
.admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-btn:hover:not(:disabled) {
  background: var(--bf-btn-secondary-bg);
  border-color: var(--bf-border-accent);
  color: var(--bf-text-primary);
}

.admin-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-btn--secondary {
  background: var(--bf-card-bg);
}

.admin-btn--ghost {
  background: transparent;
  border: none;
  padding: 0.25rem 0.5rem;
}

.admin-btn__icon {
  width: 16px;
  height: 16px;
}

.admin-btn__icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* === 错误提示 === */
.admin-error {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: var(--bf-card-radius, 16px);
  color: #ef4444;
  font-size: 14px;
}

.admin-error svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* === 加载状态 === */
.admin-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xl, 40px);
  color: var(--bf-text-muted);
}

.admin-loading__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default);
  border-top-color: var(--bf-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* === 空状态 === */
.admin-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xl, 40px);
  color: var(--bf-text-muted);
}

.admin-empty svg {
  width: 48px;
  height: 48px;
  opacity: 0.5;
}

.admin-empty h3 {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

/* === 评论列表 === */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 评论卡片 === */
.admin-card {
  display: flex;
  align-items: center;
  padding: var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  box-shadow: var(--bf-card-shadow);
  cursor: pointer;
  transition: all var(--bf-transition-normal, 0.25s ease);
  position: relative;
  overflow: hidden;
}

.admin-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--bf-fire-gradient);
  opacity: 0;
  transition: opacity var(--bf-transition-fast, 0.15s ease);
}

.admin-card:hover {
  border-color: var(--bf-border-accent);
  box-shadow: var(--bf-card-shadow-hover);
  transform: translateY(-2px);
}

.admin-card:hover::before {
  opacity: 1;
}

.admin-card--hidden {
  opacity: 0.6;
  border-left: 3px solid #ef4444;
}

.admin-card--hidden::before {
  background: #ef4444;
  opacity: 1;
}

.admin-card--selected {
  border-color: rgba(255, 107, 53, 0.4);
  background: rgba(255, 107, 53, 0.03);
}

.admin-card--selected::before {
  background: var(--bf-fire-gradient);
  opacity: 1;
}

/* 选择框 */
.admin-card__checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--bf-space-sm, 10px);
  flex-shrink: 0;
}

.admin-card__checkbox .admin-checkbox {
  width: 18px;
  height: 18px;
  accent-color: var(--bf-primary);
  cursor: pointer;
  border-radius: 4px;
}

/* 卡片主体 */
.admin-card__main {
  flex: 1;
  min-width: 0;
}

.admin-card__header {
  display: flex;
  align-items: flex-start;
  gap: var(--bf-space-sm, 10px);
  margin-bottom: 12px;
}

.admin-card__text {
  font-size: 14px;
  color: var(--bf-text-primary);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.admin-card__badges {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.admin-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bf-fire-gradient-subtle);
  border: 1px solid var(--bf-border-accent);
  border-radius: 100px;
  font-size: 11px;
  font-weight: 500;
  color: var(--bf-primary);
}

.admin-badge--hidden {
  font-size: 14px;
  color: #8B5CF6;
  text-shadow: 0 0 8px rgba(139, 92, 246, 0.8);
  filter: drop-shadow(0 0 4px rgba(139, 92, 246, 0.6));
  border: none;
  background: none;
}

.admin-card__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--bf-text-muted);
  flex-wrap: wrap;
}

.admin-card__dot {
  color: var(--bf-text-muted);
  font-size: 10px;
}

.admin-card__author {
  color: var(--bf-text-secondary);
  font-weight: 500;
}

.admin-card__post-link-text {
  color: var(--bf-primary);
  cursor: pointer;
}

.admin-card__post-link-text:hover {
  text-decoration: underline;
}

/* 操作按钮 */
.admin-card__actions {
  display: flex;
  gap: 8px;
  margin-left: var(--bf-space-md, 16px);
  flex-shrink: 0;
}

.admin-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-action-btn:hover:not(:disabled) {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary);
}

.admin-action-btn--active {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.admin-action-btn--active:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.admin-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-action-btn svg {
  width: 18px;
  height: 18px;
}

/* === 分页 === */
.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-lg, 24px);
}

.admin-pagination__info {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 响应式 === */
@media (max-width: 640px) {
  .admin-header {
    flex-direction: column;
    gap: var(--bf-space-sm, 12px);
    padding: var(--bf-space-sm, 12px);
    align-items: flex-start;
  }

  .admin-toolbar {
    flex-direction: column;
    gap: var(--bf-space-sm, 12px);
    padding: var(--bf-space-sm, 12px);
  }

  .admin-search {
    width: 100%;
  }

  .admin-card {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--bf-space-sm, 12px);
  }

  .admin-card__actions {
    margin-left: 0;
    margin-top: var(--bf-space-sm, 8px);
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
