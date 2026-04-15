<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { translate } from '@S/services/i18n'
import type { ImageSummary } from '@M/forum/admin/dtos/Admin.ts'
import { useToast } from 'primevue/usetoast'

const toast = useToast()

/* ---------------- 状态 ---------------- */
const images = ref<ImageSummary[]>([])
const isLoading = ref(false)
const isDeleting = ref(false)
const selectedImages = ref<Set<string>>(new Set())
const currentPage = ref(0)
const pageSize = ref(24)
const totalImages = ref(0)
const folderFilter = ref<string | undefined>(undefined)

/* ---------------- 计算属性 ---------------- */
const totalPages = computed(() => Math.ceil(totalImages.value / pageSize.value))
const hasImages = computed(() => images.value.length > 0)

/* ---------------- 方法 ---------------- */
// 加载图片列表
async function loadImages() {
  isLoading.value = true
  try {
    const result = await adminService.getImages(currentPage.value, pageSize.value, folderFilter.value)
    if (result.isSuccess) {
      const data = result.getValue()
      images.value = data.images
      totalImages.value = data.total
    } else {
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: result.error(),
        life: 3000,
      })
    }
  } finally {
    isLoading.value = false
  }
}

// 删除图片
async function deleteImage(imageId: string) {
  isDeleting.value = true
  try {
    const result = await adminService.deleteImage(imageId)
    if (result.isSuccess) {
      toast.add({
        severity: 'success',
        summary: translate('forum', 'admin.deleteSuccess') || '删除成功',
        detail: result.getValue().message,
        life: 3000,
      })
      // 从列表中移除
      images.value = images.value.filter(img => img.imageId !== imageId)
      totalImages.value--
      selectedImages.value.delete(imageId)
    } else {
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: result.error(),
        life: 3000,
      })
    }
  } finally {
    isDeleting.value = false
  }
}

// 批量删除
async function batchDeleteImages() {
  if (selectedImages.value.size === 0) return

  isDeleting.value = true
  const deletePromises = Array.from(selectedImages.value).map(imageId =>
    adminService.deleteImage(imageId)
  )

  try {
    const results = await Promise.all(deletePromises)
    const successCount = results.filter(r => r.isSuccess).length
    const failCount = results.filter(r => !r.isSuccess).length

    toast.add({
      severity: failCount > 0 ? 'warn' : 'success',
      summary: failCount > 0 ? '部分删除失败' : '删除成功',
      detail: `成功删除 ${successCount} 张图片${failCount > 0 ? `，失败 ${failCount} 张` : ''}`,
      life: 3000,
    })

    // 重新加载
    await loadImages()
    selectedImages.value.clear()
  } finally {
    isDeleting.value = false
  }
}

// 选择图片
function toggleSelect(imageId: string) {
  if (selectedImages.value.has(imageId)) {
    selectedImages.value.delete(imageId)
  } else {
    selectedImages.value.add(imageId)
  }
  // 触发响应式更新
  selectedImages.value = new Set(selectedImages.value)
}

// 全选
function selectAll() {
  if (selectedImages.value.size === images.value.length) {
    selectedImages.value.clear()
  } else {
    selectedImages.value = new Set(images.value.map(img => img.imageId))
  }
  selectedImages.value = new Set(selectedImages.value)
}

// 翻页
function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  loadImages()
}

// 格式化文件大小
function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// 格式化日期
function formatDate(timestamp: number): string {
  return new Date(timestamp).toLocaleDateString()
}

// 获取图片类型图标
function getImageTypeIcon(contentType: string): string {
  if (contentType.includes('gif')) return '🎞️'
  if (contentType.includes('webp')) return '🖼️'
  return '🖼️'
}

onMounted(() => {
  loadImages()
})
</script>

<template>
  <div class="bf-images-management">
    <!-- 头部操作栏 -->
    <div class="bf-images-header">
      <div class="bf-header-left">
        <h3 class="bf-section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          {{ translate('forum', 'admin.images.title') || '图片管理' }}
        </h3>
        <span class="bf-image-count">{{ totalImages }} {{ translate('forum', 'admin.images.count') || '张图片' }}</span>
      </div>

      <div class="bf-header-actions">
        <button
          v-if="selectedImages.size > 0"
          class="bf-btn bf-btn--danger"
          :disabled="isDeleting"
          @click="batchDeleteImages"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
          {{ translate('forum', 'admin.batchDelete') || `删除选中 (${selectedImages.size})` }}
        </button>
        <button class="bf-btn bf-btn--ghost" @click="loadImages" :disabled="isLoading">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          {{ translate('forum', 'admin.refresh') || '刷新' }}
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && !hasImages" class="bf-loading-state">
      <div class="bf-spinner"></div>
      <span>{{ translate('forum', 'admin.loading') || '加载中...' }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!hasImages" class="bf-empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
        <circle cx="8.5" cy="8.5" r="1.5"/>
        <polyline points="21 15 16 10 5 21"/>
      </svg>
      <span>{{ translate('forum', 'admin.images.empty') || '暂无图片' }}</span>
    </div>

    <!-- 图片网格 -->
    <div v-else class="bf-images-grid">
      <div
        v-for="image in images"
        :key="image.imageId"
        class="bf-image-card"
        :class="{ 'bf-image-card--selected': selectedImages.has(image.imageId) }"
        @click="toggleSelect(image.imageId)"
      >
        <!-- 选择指示器 -->
        <div class="bf-image-checkbox">
          <svg v-if="selectedImages.has(image.imageId)" viewBox="0 0 24 24" fill="currentColor">
            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
          </svg>
        </div>

        <!-- 图片预览 -->
        <div class="bf-image-preview">
          <img :src="image.url" :alt="image.fileName" loading="lazy" />
        </div>

        <!-- 图片信息 -->
        <div class="bf-image-info">
          <div class="bf-image-name" :title="image.fileName">{{ image.fileName }}</div>
          <div class="bf-image-meta">
            <span class="bf-image-size">{{ formatFileSize(image.fileSize) }}</span>
            <span class="bf-image-date">{{ formatDate(image.uploadedAt) }}</span>
          </div>
        </div>

        <!-- 删除按钮 -->
        <button
          class="bf-image-delete"
          @click.stop="deleteImage(image.imageId)"
          :disabled="isDeleting"
          :title="translate('forum', 'admin.delete') || '删除'"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="bf-pagination">
      <button
        class="bf-pagination-btn"
        :disabled="currentPage === 0"
        @click="goToPage(currentPage - 1)"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>

      <div class="bf-pagination-info">
        {{ translate('forum', 'admin.pageInfo')
          .replace('{current}', String(currentPage + 1))
          .replace('{total}', String(totalPages)) }}
      </div>

      <button
        class="bf-pagination-btn"
        :disabled="currentPage >= totalPages - 1"
        @click="goToPage(currentPage + 1)"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.bf-images-management {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%;
}

/* 头部 */
.bf-images-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-radius-xl, 14px);
}

.bf-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.bf-section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--bf-text-primary);
}

.bf-section-title svg {
  color: var(--bf-primary, #FF6B35);
}

.bf-image-count {
  color: var(--bf-text-muted);
  font-size: 0.875rem;
}

.bf-header-actions {
  display: flex;
  gap: 0.5rem;
}

/* 按钮 */
.bf-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: var(--bf-btn-radius, 12px);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-btn--ghost {
  background: transparent;
  color: var(--bf-text-secondary);
}

.bf-btn--ghost:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.05);
  color: var(--bf-text-primary);
}

.bf-btn--danger {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.bf-btn--danger:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.25);
}

/* 加载状态 */
.bf-loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 4rem 2rem;
  color: var(--bf-text-muted);
}

.bf-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-top-color: var(--bf-primary, #FF6B35);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.bf-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 4rem 2rem;
  color: var(--bf-text-muted);
}

/* 图片网格 */
.bf-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1rem;
  padding: 0 1.5rem;
  overflow-y: auto;
  flex: 1;
}

/* 图片卡片 */
.bf-image-card {
  position: relative;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-radius-lg, 10px);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bf-image-card:hover {
  border-color: var(--bf-border-accent);
  transform: translateY(-2px);
}

.bf-image-card--selected {
  border-color: var(--bf-primary, #FF6B35);
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2);
}

/* 选择指示器 */
.bf-image-checkbox {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  z-index: 2;
  color: white;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.bf-image-card:hover .bf-image-checkbox,
.bf-image-card--selected .bf-image-checkbox {
  opacity: 1;
}

.bf-image-card--selected .bf-image-checkbox {
  background: var(--bf-primary, #FF6B35);
  border-color: var(--bf-primary, #FF6B35);
}

/* 图片预览 */
.bf-image-preview {
  aspect-ratio: 1;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.2);
}

.bf-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图片信息 */
.bf-image-info {
  padding: 0.75rem;
}

.bf-image-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--bf-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.25rem;
}

.bf-image-meta {
  display: flex;
  gap: 0.5rem;
  font-size: 0.6875rem;
  color: var(--bf-text-muted);
}

/* 删除按钮 */
.bf-image-delete {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.8);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 2;
}

.bf-image-card:hover .bf-image-delete {
  opacity: 1;
}

.bf-image-delete:hover {
  background: #ef4444;
}

.bf-image-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 分页 */
.bf-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1rem;
}

.bf-pagination-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.bf-pagination-btn:hover:not(:disabled) {
  border-color: var(--bf-border-accent);
  color: var(--bf-text-primary);
}

.bf-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-pagination-info {
  font-size: 0.875rem;
  color: var(--bf-text-muted);
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-images-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .bf-images-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0.75rem;
    padding: 0 1rem;
  }

  .bf-image-delete {
    opacity: 1;
  }
}
</style>
