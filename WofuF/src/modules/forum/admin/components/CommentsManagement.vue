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

// 操作中的评论ID
const operatingCommentId = ref<string | null>(null)

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
    comment.isHidden || (comment as any).hidden || (comment as any).status === 'HIDDEN',
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
      true, // 包含隐藏评论，用于管理
    )

    if (result.isSuccess) {
      comments.value = result.getValue().comments
      totalComments.value = result.getValue().total
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

  const index = comments.value.findIndex((c) => c.commentId === commentId)
  if (index === -1) return

  const comment = comments.value[index]!

  // 使用修复后的 isHidden 函数记录上一个状态，用于失败时回滚
  const previousHidden = isHidden(comment)

  // 乐观更新：立即翻转状态（同时兼容可能存在的 hidden 字段）
  comment.isHidden = !isCurrentlyHidden
  if ('hidden' in comment) {
    ;(comment as any).hidden = !isCurrentlyHidden
  }

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
      // 成功：清除缓存即可
      cacheService.clearModule('forum_service')

      // ⚠️ 核心修复：移除这里的 await loadComments() ⚠️
      // 既然前端已经做了乐观更新，且后端返回了成功，就直接信任本地的最新状态。
      // 避免因为后端数据库/搜索引擎延迟，立刻拉取到旧列表而导致状态又变回去。

      toast.add({
        severity: 'success',
        summary: isCurrentlyHidden
          ? translate('forum', 'admin.showSuccess')
          : translate('forum', 'admin.hideSuccess'),
        life: 2000,
      })
    } else {
      // 失败：回滚状态
      comment.isHidden = previousHidden
      if ('hidden' in comment) (comment as any).hidden = previousHidden
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    // 异常：回滚状态
    comment.isHidden = previousHidden
    if ('hidden' in comment) (comment as any).hidden = previousHidden
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
      <!-- 搜索 -->
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
          :placeholder="translate('forum', 'searchComments')"
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
        :class="{ 'admin-card--hidden': isHidden(comment) }"
      >
        <!-- 卡片主体 -->
        <div class="admin-card__main" @click="goToPost(comment.postSlug || comment.postId)">
          <!-- 顶部：内容预览 + 徽章 -->
          <div class="admin-card__header">
            <p class="admin-card__text">{{ comment.content }}</p>
            <!-- 徽章 -->
            <div class="admin-card__badges">
              <span v-if="isHidden(comment)" class="admin-badge admin-badge--hidden">已隐藏</span>
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
  min-width: 200px;
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
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
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
