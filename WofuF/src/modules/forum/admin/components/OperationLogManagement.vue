<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { forumService } from '@M/forum/services/ForumService.ts'
import type { OperationLogEntry, OperationType, TargetType } from '@M/forum/admin/dtos/Admin.ts'
import { translate } from '@S/services/i18n'

const toast = useToast()
const router = useRouter()

// ==================== 状态 ====================
const logs = ref<OperationLogEntry[]>([])
const isLoading = ref(false)
const errorMsg = ref('')
const currentPage = ref(0)
const pageSize = ref(20)
const totalLogs = ref(0)
const totalPages = ref(0)

// 筛选状态
const filterOperatorId = ref('')
const filterOperationType = ref<OperationType | ''>('')
const filterTargetType = ref<TargetType | ''>('')

// ==================== 筛选选项 ====================
const operationTypeOptions = computed(() => [
  { id: '' as OperationType, label: translate('forum', 'admin.logs.allOperations') },
  { id: 'POST_PIN' as OperationType, label: '置顶帖子' },
  { id: 'POST_UNPIN' as OperationType, label: '取消置顶' },
  { id: 'POST_FEATURE' as OperationType, label: '加精帖子' },
  { id: 'POST_UNFEATURE' as OperationType, label: '取消加精' },
  { id: 'POST_HIDE' as OperationType, label: '隐藏帖子' },
  { id: 'POST_SHOW' as OperationType, label: '显示帖子' },
  { id: 'POST_SET_REVIEW' as OperationType, label: '设为待审核' },
  { id: 'POST_APPROVE' as OperationType, label: '审核通过' },
  { id: 'POST_DELETE' as OperationType, label: '删除帖子' },
  { id: 'POST_EDIT' as OperationType, label: '编辑帖子' },
  { id: 'COMMENT_HIDE' as OperationType, label: '隐藏评论' },
  { id: 'COMMENT_SHOW' as OperationType, label: '显示评论' },
  { id: 'COMMENT_DELETE' as OperationType, label: '删除评论' },
  { id: 'MEMBER_BAN' as OperationType, label: '封禁用户' },
  { id: 'MEMBER_UNBAN' as OperationType, label: '解封用户' },
  { id: 'MEMBER_GRANT_PERMISSION' as OperationType, label: '授予权限' },
  { id: 'MEMBER_REVOKE_PERMISSION' as OperationType, label: '撤销权限' },
  { id: 'IMAGE_DELETE' as OperationType, label: '删除图片' },
])

const targetTypeOptions = computed(() => [
  { id: '' as TargetType, label: translate('forum', 'admin.logs.allTargets') },
  { id: 'POST' as TargetType, label: '帖子' },
  { id: 'COMMENT' as TargetType, label: '评论' },
  { id: 'MEMBER' as TargetType, label: '成员' },
  { id: 'IMAGE' as TargetType, label: '图片' },
])

// ==================== 辅助函数 ====================

// 格式化时间
function formatTime(timestamp: number): string {
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

  return date.toLocaleString()
}

// 获取操作类型颜色
function getOperationColor(type: OperationType): string {
  const colorMap: Record<string, string> = {
    POST_PIN: 'bf-op-blue',
    POST_UNPIN: 'bf-op-blue',
    POST_FEATURE: 'bf-op-purple',
    POST_UNFEATURE: 'bf-op-purple',
    POST_HIDE: 'bf-op-red',
    POST_SHOW: 'bf-op-green',
    POST_SET_REVIEW: 'bf-op-yellow',
    POST_APPROVE: 'bf-op-green',
    POST_DELETE: 'bf-op-red-dark',
    POST_EDIT: 'bf-op-gray',
    COMMENT_HIDE: 'bf-op-red',
    COMMENT_SHOW: 'bf-op-green',
    COMMENT_DELETE: 'bf-op-red-dark',
    COMMENT_EDIT: 'bf-op-gray',
    MEMBER_BAN: 'bf-op-red-dark',
    MEMBER_UNBAN: 'bf-op-green',
    MEMBER_GRANT_PERMISSION: 'bf-op-indigo',
    MEMBER_REVOKE_PERMISSION: 'bf-op-indigo',
    IMAGE_DELETE: 'bf-op-red',
    UNKNOWN: 'bf-op-gray',
  }
  return colorMap[type] || 'bf-op-gray'
}

// 获取目标类型标签
function getTargetTypeLabel(type: TargetType): string {
  const labelMap: Record<TargetType, string> = {
    POST: '帖子',
    COMMENT: '评论',
    MEMBER: '成员',
    IMAGE: '图片',
  }
  return labelMap[type] || type
}

// ==================== 数据加载 ====================
async function loadLogs() {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const result = await adminService.getOperationLogs(
      currentPage.value,
      pageSize.value,
      filterOperatorId.value || undefined,
      filterOperationType.value || undefined,
      filterTargetType.value || undefined
    )

    if (result.isSuccess) {
      const data = result.getValue()
      logs.value = data.logs
      totalLogs.value = data.total
      totalPages.value = data.totalPages
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
    }
  } catch (e) {
    errorMsg.value = translate('forum', 'error')
  } finally {
    isLoading.value = false
  }
}

// ==================== 分页 ====================
function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadLogs()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadLogs()
  }
}

// ==================== 筛选 ====================
let searchTimeout: ReturnType<typeof setTimeout> | null = null
function onFilterChange() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadLogs()
  }, 300)
}

function clearFilters() {
  filterOperatorId.value = ''
  filterOperationType.value = ''
  filterTargetType.value = ''
  currentPage.value = 0
  loadLogs()
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadLogs()
})
</script>

<template>
  <div class="admin-container">
    <!-- 页面标题栏 -->
    <div class="admin-header">
      <div class="admin-header__title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        <h1>{{ translate('forum', 'admin.logs.title') }}</h1>
      </div>
      <span class="admin-header__count">{{ translate('forum', 'admin.logs.operation') }}: {{ totalLogs }}</span>
    </div>

    <!-- 工具栏 -->
    <div class="admin-toolbar">
      <!-- 操作者搜索 -->
      <div class="admin-search">
        <svg class="admin-search__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          v-model="filterOperatorId"
          type="text"
          :placeholder="translate('forum', 'admin.logs.filterByOperatorId')"
          class="admin-search__input"
          @input="onFilterChange"
        />
      </div>

      <!-- 操作类型筛选 -->
      <select v-model="filterOperationType" class="admin-select" @change="onFilterChange">
        <option v-for="opt in operationTypeOptions" :key="opt.id" :value="opt.id">
          {{ opt.label }}
        </option>
      </select>

      <!-- 目标类型筛选 -->
      <select v-model="filterTargetType" class="admin-select" @change="onFilterChange">
        <option v-for="opt in targetTypeOptions" :key="opt.id" :value="opt.id">
          {{ opt.label }}
        </option>
      </select>

      <!-- 清除筛选 -->
      <button
        v-if="filterOperatorId || filterOperationType || filterTargetType"
        class="admin-btn admin-btn--secondary"
        @click="clearFilters"
      >
        <svg class="admin-btn__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
        清除
      </button>

      <!-- 刷新按钮 -->
      <button class="admin-btn admin-btn--secondary" @click="loadLogs" :disabled="isLoading">
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
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
      </svg>
      <span>{{ errorMsg }}</span>
      <button class="admin-btn admin-btn--ghost" @click="errorMsg = ''">×</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && logs.length === 0" class="admin-loading">
      <div class="admin-loading__spinner"></div>
      <span>{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!isLoading && logs.length === 0" class="admin-empty">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      <h3>{{ translate('forum', 'admin.logs.noLogs') }}</h3>
    </div>

    <!-- 日志列表 -->
    <div v-else class="admin-list">
      <div
        v-for="log in logs"
        :key="log.logId"
        class="log-card"
      >
        <div class="log-card__main">
          <!-- 顶部：操作类型 + 目标信息 -->
          <div class="log-card__header">
            <span class="log-operation" :class="getOperationColor(log.operationType)">
              {{ log.operationName }}
            </span>
            <span class="log-target-type">{{ getTargetTypeLabel(log.targetType) }}</span>
          </div>

          <!-- 操作者信息 -->
          <div class="log-card__operator">
            <svg class="log-card__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span class="log-card__operator-name">{{ log.operatorNickname || '未知用户' }}</span>
            <a
              :href="`/forum/members/${log.operatorNickname || log.operatorId}`"
              target="_blank"
              class="log-card__operator-id-link"
              :title="log.operatorId"
            >
              ({{ log.operatorId }})
            </a>
          </div>

          <!-- 目标ID (可点击链接) -->
          <div class="log-card__target">
            <span class="log-card__label">{{ translate('forum', 'admin.logs.targetId') }}:</span>
            <a
              v-if="log.targetType === 'POST'"
              :href="`/forum/posts/${log.targetId}`"
              target="_blank"
              class="log-card__link"
              :title="log.targetId"
            >
              {{ log.targetId.substring(0, 16) }}...
            </a>
            <!-- 评论链接需要 postSlug，暂时显示 ID -->
            <code
              v-else-if="log.targetType === 'COMMENT'"
              class="log-card__id"
              :title="log.targetId"
            >
              {{ log.targetId.substring(0, 16) }}...
            </code>
            <a
              v-else-if="log.targetType === 'MEMBER'"
              :href="`/forum/members/${log.targetId}`"
              target="_blank"
              class="log-card__link"
              :title="log.targetId"
            >
              {{ log.targetId.substring(0, 16) }}...
            </a>
            <code v-else class="log-card__id" :title="log.targetId">{{ log.targetId.substring(0, 16) }}...</code>
          </div>

          <!-- 操作详情 -->
          <div v-if="log.details" class="log-card__details">
            <span class="log-card__label">{{ translate('forum', 'admin.logs.details') }}:</span>
            <span class="log-card__detail-text">{{ log.details }}</span>
          </div>

          <!-- 底部：时间 -->
          <div class="log-card__meta">
            <span class="log-card__time">
              <svg class="log-card__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              {{ formatTime(log.createdAt) }}
            </span>
            <code class="log-card__log-id" :title="log.logId">日志ID: {{ log.logId.substring(0, 8) }}...</code>
          </div>
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

/* 筛选器 */
.admin-select {
  padding: 0.5rem 2rem 0.5rem 0.75rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  outline: none;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-select:focus {
  border-color: var(--bf-border-accent);
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

/* === 日志列表 === */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 日志卡片 === */
.log-card {
  display: flex;
  padding: var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  transition: all var(--bf-transition-normal, 0.25s ease);
}

.log-card:hover {
  border-color: var(--bf-border-accent);
  transform: translateY(-1px);
}

.log-card__main {
  flex: 1;
  min-width: 0;
}

.log-card__header {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
  margin-bottom: 12px;
}

.log-operation {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}

.log-target-type {
  font-size: 12px;
  color: var(--bf-text-muted);
  padding: 2px 8px;
  background: var(--bf-input-bg);
  border-radius: 4px;
}

.log-card__target {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.log-card__label {
  font-size: 12px;
  color: var(--bf-text-muted);
}

.log-card__id {
  font-size: 12px;
  color: var(--bf-text-secondary);
  background: var(--bf-input-bg);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.log-card__operator {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  padding: 6px 10px;
  background: var(--bf-input-bg);
  border-radius: 8px;
}

.log-card__operator-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--bf-text-primary);
}

.log-card__operator-id {
  font-size: 11px;
  color: var(--bf-text-muted);
  font-family: monospace;
}

.log-card__operator-id-link {
  font-size: 11px;
  color: var(--bf-primary);
  font-family: monospace;
  text-decoration: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.log-card__operator-id-link:hover {
  text-decoration: underline;
}

.log-card__link {
  font-size: 12px;
  color: var(--bf-primary);
  background: var(--bf-input-bg);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  text-decoration: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.log-card__link:hover {
  background: var(--bf-btn-secondary-bg);
  text-decoration: underline;
}

.log-card__log-id {
  font-size: 11px;
  color: var(--bf-text-muted);
  font-family: monospace;
  margin-left: auto;
}

.log-card__details {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 12px;
}

.log-card__detail-text {
  font-size: 13px;
  color: var(--bf-text-secondary);
}

.log-card__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--bf-text-muted);
}

.log-card__operator,
.log-card__time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.log-card__icon {
  width: 14px;
  height: 14px;
}

/* 操作类型颜色 */
.bf-op-blue {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.bf-op-purple {
  background: rgba(168, 85, 247, 0.15);
  color: #a78bfa;
}

.bf-op-red {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
}

.bf-op-red-dark {
  background: rgba(220, 38, 38, 0.15);
  color: #fca5a5;
}

.bf-op-green {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
}

.bf-op-yellow {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
}

.bf-op-indigo {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
}

.bf-op-gray {
  background: rgba(107, 114, 128, 0.15);
  color: #9ca3af;
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

  .log-card {
    padding: var(--bf-space-sm, 12px);
  }

  .log-card__meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
