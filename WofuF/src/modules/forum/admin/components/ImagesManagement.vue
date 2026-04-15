<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { translate } from '@S/services/i18n'
import type { ImageSummary, MemberSummary } from '@M/forum/admin/dtos/Admin.ts'
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

// 筛选状态
const filterMemberId = ref<string | null>(null)
const memberSearchQuery = ref('')
const searchResults = ref<MemberSummary[]>([])
const isSearchingMember = ref(false)

/* ---------------- 计算属性 ---------------- */
const totalPages = computed(() => Math.ceil(totalImages.value / pageSize.value))
const hasImages = computed(() => images.value.length > 0)
const hasSelectedMember = computed(() => filterMemberId.value !== null)

// 按上传者分组的图片
const groupedImages = computed(() => {
  const groups: Record<string, { memberId: string | null, images: ImageSummary[] }> = {}

  for (const img of images.value) {
    const key = img.uploaderId || 'anonymous'
    if (!groups[key]) {
      groups[key] = { memberId: img.uploaderId, images: [] }
    }
    groups[key].images.push(img)
  }

  return groups
})

// 选中的成员信息
const selectedMember = computed(() => {
  if (!filterMemberId.value) return null
  return searchResults.value.find(m => m.memberId === filterMemberId.value) || null
})

/* ---------------- 方法 ---------------- */
async function loadImages() {
  isLoading.value = true
  try {
    const result = await adminService.getImages(currentPage.value, pageSize.value, undefined, filterMemberId.value || undefined)
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

// 搜索成员（防抖）
let searchTimeout: ReturnType<typeof setTimeout> | null = null
async function onMemberSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)

  if (!memberSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }

  searchTimeout = setTimeout(async () => {
    isSearchingMember.value = true
    try {
      const result = await adminService.getMembersList(memberSearchQuery.value.trim(), 0, 20)
      if (result.isSuccess) {
        searchResults.value = result.getValue().members
      }
    } catch (e) {
      console.warn('[ImagesManagement] Failed to search members:', e)
    } finally {
      isSearchingMember.value = false
    }
  }, 300)
}

function selectMember(member: MemberSummary) {
  filterMemberId.value = member.memberId
  memberSearchQuery.value = member.nickname
  searchResults.value = []
  currentPage.value = 0
  loadImages()
}

function clearMemberFilter() {
  filterMemberId.value = null
  memberSearchQuery.value = ''
  searchResults.value = []
  currentPage.value = 0
  loadImages()
}

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

    await loadImages()
    selectedImages.value.clear()
  } finally {
    isDeleting.value = false
  }
}

function toggleSelect(imageId: string) {
  if (selectedImages.value.has(imageId)) {
    selectedImages.value.delete(imageId)
  } else {
    selectedImages.value.add(imageId)
  }
  selectedImages.value = new Set(selectedImages.value)
}

function selectAllInGroup(groupKey: string) {
  const group = groupedImages.value[groupKey]
  if (!group) return

  const allSelected = group.images.every(img => selectedImages.value.has(img.imageId))

  if (allSelected) {
    group.images.forEach(img => selectedImages.value.delete(img.imageId))
  } else {
    group.images.forEach(img => selectedImages.value.add(img.imageId))
  }
  selectedImages.value = new Set(selectedImages.value)
}

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  loadImages()
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDate(timestamp: number): string {
  return new Date(timestamp).toLocaleDateString()
}

function getMemberLabel(memberId: string | null): string {
  if (!memberId) return 'Anonymous'
  const member = searchResults.value.find(m => m.memberId === memberId)
  return member ? member.nickname : `Member: ${memberId.slice(0, 8)}...`
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
        <span class="bf-icon">&#9745;</span>
        <h3 class="bf-section-title">
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
          <span class="bf-icon">&#10005;</span>
          {{ translate('forum', 'admin.batchDelete') || `删除 (${selectedImages.size})` }}
        </button>
        <button class="bf-btn bf-btn--ghost" @click="loadImages" :disabled="isLoading">
          <span class="bf-icon">&#8635;</span>
          {{ translate('forum', 'admin.refresh') || '刷新' }}
        </button>
      </div>
    </div>

    <!-- 成员筛选 -->
    <div class="bf-member-filter">
      <div class="bf-filter-label">
        <span class="bf-icon">&#9787;</span>
        <span>{{ translate('forum', 'admin.filterByMember') || '筛选成员' }}</span>
      </div>

      <div class="bf-member-search">
        <input
          type="text"
          class="bf-search-input"
          :placeholder="translate('forum', 'admin.searchMember') || '输入成员昵称搜索...'"
          v-model="memberSearchQuery"
          @input="onMemberSearchInput"
          @focus="onMemberSearchInput"
        />

        <!-- 搜索结果下拉 -->
        <div v-if="searchResults.length > 0" class="bf-search-results">
          <div
            v-for="member in searchResults"
            :key="member.memberId"
            class="bf-search-result-item"
            @click="selectMember(member)"
          >
            <span class="bf-member-nickname">{{ member.nickname }}</span>
            <span class="bf-member-id">{{ member.memberId.slice(0, 8) }}...</span>
          </div>
        </div>

        <!-- 搜索加载中 -->
        <div v-else-if="isSearchingMember" class="bf-search-loading">
          {{ translate('forum', 'admin.searching') || '搜索中...' }}
        </div>
      </div>

      <!-- 选中的成员标签 -->
      <div v-if="hasSelectedMember" class="bf-selected-member">
        <span class="bf-member-name">{{ selectedMember?.nickname || memberSearchQuery }}</span>
        <button class="bf-clear-filter" @click="clearMemberFilter">&#10005;</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && !hasImages" class="bf-loading-state">
      <div class="bf-spinner"></div>
      <span>{{ translate('forum', 'admin.loading') || '加载中...' }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!hasImages" class="bf-empty-state">
      <span class="bf-icon bf-icon--large">&#9744;</span>
      <span>{{ translate('forum', 'admin.images.empty') || '暂无图片' }}</span>
    </div>

    <!-- 按成员分组的图片 -->
    <div v-else class="bf-images-container">
      <div
        v-for="(group, groupKey) in groupedImages"
        :key="groupKey"
        class="bf-image-group"
      >
        <!-- 组成员头部 -->
        <div class="bf-group-header">
          <button class="bf-group-select" @click="selectAllInGroup(groupKey)">
            <span class="bf-icon">
              {{ group.images.every(img => selectedImages.has(img.imageId)) ? '&#10003;' : '&#9744;' }}
            </span>
          </button>
          <span class="bf-group-label">{{ getMemberLabel(group.memberId) }}</span>
          <span class="bf-group-count">{{ group.images.length }} images</span>
        </div>

        <!-- 组内图片网格 -->
        <div class="bf-images-grid">
          <div
            v-for="image in group.images"
            :key="image.imageId"
            class="bf-image-card"
            :class="{ 'bf-image-card--selected': selectedImages.has(image.imageId) }"
            @click="toggleSelect(image.imageId)"
          >
            <!-- 选择指示器 -->
            <div class="bf-image-checkbox">
              <span v-if="selectedImages.has(image.imageId)" class="bf-icon">&#10003;</span>
            </div>

            <!-- 图片预览 -->
            <div class="bf-image-preview">
              <img :src="image.url" :alt="image.fileName" loading="lazy" />
            </div>

            <!-- 图片信息 -->
            <div class="bf-image-info">
              <div class="bf-image-name" :title="image.fileName">{{ image.fileName }}</div>
              <div class="bf-image-meta">
                <span>{{ formatFileSize(image.fileSize) }}</span>
                <span>{{ formatDate(image.uploadedAt) }}</span>
              </div>
            </div>

            <!-- 删除按钮 -->
            <button
              class="bf-image-delete"
              @click.stop="deleteImage(image.imageId)"
              :disabled="isDeleting"
            >
              <span class="bf-icon">&#10005;</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="bf-pagination">
      <button
        class="bf-pagination-btn"
        :disabled="currentPage === 0"
        @click="goToPage(currentPage - 1)"
      >
        <span class="bf-icon">&#8592;</span>
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
        <span class="bf-icon">&#8594;</span>
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

.bf-icon {
  font-family: monospace;
  font-size: 1rem;
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
  gap: 0.75rem;
}

.bf-section-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--bf-text-primary);
}

.bf-image-count {
  color: var(--bf-text-muted);
  font-size: 0.875rem;
}

.bf-header-actions {
  display: flex;
  gap: 0.5rem;
}

/* 成员筛选 */
.bf-member-filter {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.03));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-radius-lg, 10px);
}

.bf-filter-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--bf-text-secondary);
  font-size: 0.875rem;
  white-space: nowrap;
}

.bf-member-search {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.bf-search-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-radius-md, 8px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
}

.bf-search-input::placeholder {
  color: var(--bf-text-muted);
}

.bf-search-input:focus {
  outline: none;
  border-color: var(--bf-primary);
}

.bf-search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.25rem;
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-radius-md, 8px);
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.bf-search-result-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  transition: background 0.15s;
}

.bf-search-result-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.bf-member-nickname {
  color: var(--bf-text-primary);
  font-size: 0.875rem;
}

.bf-member-id {
  color: var(--bf-text-muted);
  font-size: 0.75rem;
}

.bf-search-loading {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.25rem;
  padding: 0.5rem 0.75rem;
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-radius-md, 8px);
  color: var(--bf-text-muted);
  font-size: 0.875rem;
}

.bf-selected-member {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: var(--bf-primary);
  border-radius: var(--bf-radius-md, 8px);
  color: white;
  font-size: 0.875rem;
}

.bf-member-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bf-clear-filter {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  opacity: 0.8;
  font-size: 0.75rem;
}

.bf-clear-filter:hover {
  opacity: 1;
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

.bf-icon--large {
  font-size: 4rem;
}

/* 图片容器 */
.bf-images-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 0 1.5rem;
  overflow-y: auto;
  flex: 1;
}

/* 图片组 */
.bf-image-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.bf-group-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.03));
  border-radius: var(--bf-radius-md, 8px);
}

.bf-group-select {
  background: transparent;
  border: none;
  color: var(--bf-text-muted);
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
}

.bf-group-select:hover {
  color: var(--bf-text-primary);
}

.bf-group-label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--bf-text-primary);
}

.bf-group-count {
  font-size: 0.75rem;
  color: var(--bf-text-muted);
  margin-left: auto;
}

/* 图片网格 */
.bf-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.75rem;
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
  top: 0.375rem;
  left: 0.375rem;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
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
  padding: 0.5rem;
}

.bf-image-name {
  font-size: 0.75rem;
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
  font-size: 0.625rem;
  color: var(--bf-text-muted);
}

/* 删除按钮 */
.bf-image-delete {
  position: absolute;
  top: 0.375rem;
  right: 0.375rem;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.8);
  border: none;
  border-radius: 6px;
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

  .bf-member-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }

  .bf-member-search {
    max-width: none;
  }

  .bf-images-container {
    padding: 0 1rem;
  }

  .bf-images-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 0.5rem;
  }

  .bf-image-delete {
    opacity: 1;
  }
}
</style>
