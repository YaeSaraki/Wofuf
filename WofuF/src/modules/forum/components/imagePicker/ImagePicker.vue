<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { imageService } from '@M/forum/services/ImageService.ts'
import { authService } from '@M/auth/services/AuthService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import type { ImageListItem } from '@M/forum/services/ImageService.ts'

const emit = defineEmits<{
  (e: 'select', markdown: string): void
  (e: 'close'): void
}>()

/* ---------------- 状态 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()
const images = ref<ImageListItem[]>([])
const selectedImage = ref<ImageListItem | null>(null)
const searchQuery = ref('')
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

/* ---------------- 计算属性 ---------------- */
const filteredImages = computed(() => {
  if (!searchQuery.value) return images.value
  return images.value.filter(img =>
    img.fileName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    img.md5.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

/* ---------------- 方法 ---------------- */
// 加载图片列表 - 后端根据登录状态自动返回当前用户的图片
async function loadImages() {
  if (!authService.isAuthenticated()) {
    images.value = []
    return
  }

  const result = await executeAsync(async () => {
    return await imageService.getExistingImages()
  })

  if (result && result.images) {
    images.value = result.images
  }
}

// 删除图片
const isDeleting = ref(false)
const deleteError = ref('')

async function deleteImage(img: ImageListItem, event: Event) {
  event.stopPropagation()
  
  if (!confirm(`确定要删除图片 "${img.fileName}" 吗？`)) {
    return
  }

  isDeleting.value = true
  deleteError.value = ''

  const result = await imageService.deleteImage(img.imageId)

  if (result.isSuccess) {
    // 从列表中移除
    images.value = images.value.filter(i => i.imageId !== img.imageId)
  } else {
    deleteError.value = String(result.error)
    setTimeout(() => { deleteError.value = '' }, 3000)
  }

  isDeleting.value = false
}

// 选择图片
function selectImage(img: ImageListItem) {
  selectedImage.value = img
}

// 确认选择
function confirmSelection() {
  if (selectedImage.value) {
    const markdown = `![${selectedImage.value.fileName}](${selectedImage.value.url})`
    emit('select', markdown)
  }
}

// 双击快速选择
function handleDoubleClick(img: ImageListItem) {
  selectedImage.value = img
  confirmSelection()
}

// 关闭
function close() {
  emit('close')
}

// 获取图片文件名
function getImageName(img: ImageListItem): string {
  return img.fileName
}

// 触发文件上传
function triggerUpload() {
  fileInputRef.value?.click()
}

// 处理文件选择
async function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  isUploading.value = true
  errorMsg.value = ''

  const result = await imageService.uploadImage(file, 'posts')

  if (result.isSuccess) {
    const uploadData = result.getValue()
    // 添加到列表开头
    const newImage: ImageListItem = {
      imageId: uploadData.md5,
      md5: uploadData.md5,
      url: uploadData.url,
      folder: 'posts',
      fileName: file.name,
      fileSize: file.size,
      contentType: file.type,
      uploadedAt: Date.now()
    }
    images.value.unshift(newImage)
    // 自动选中新上传的图片
    selectImage(newImage)
  } else {
    errorMsg.value = String(result.error)
  }

  isUploading.value = false
  // 清空input
  input.value = ''
}

onMounted(() => {
  loadImages()
})
</script>

<template>
  <Teleport to="body">
    <div class="bf-image-picker-overlay" @click.self="close">
      <div class="bf-image-picker">
        <!-- 头部 -->
        <div class="bf-picker-header">
          <div class="bf-picker-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <span>选择图片</span>
          </div>
          <button @click="close" class="bf-close-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- 搜索框和上传按钮 -->
        <div class="bf-picker-search-bar">
          <div class="bf-picker-search">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索图片..."
              class="bf-search-input"
            />
          </div>
          <button class="bf-upload-btn" @click="triggerUpload" :disabled="isUploading">
            <svg v-if="!isUploading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            <div v-else class="bf-spinner bf-spinner--small"></div>
            <span>{{ isUploading ? '上传中...' : '上传图片' }}</span>
          </button>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="bf-hidden-input"
            @change="handleFileSelect"
          />
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="bf-error-message">
          <span>{{ errorMsg }}</span>
        </div>
        <div v-if="deleteError" class="bf-error-message bf-error-message--delete">
          <span>{{ deleteError }}</span>
        </div>

        <!-- 图片网格 -->
        <div class="bf-picker-content">
          <!-- 加载状态 -->
          <div v-if="isLoading" class="bf-loading-state">
            <div class="bf-spinner"></div>
            <span>加载中...</span>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredImages.length === 0" class="bf-empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <span>暂无图片</span>
            <button class="bf-upload-hint-btn" @click="triggerUpload">点击上传第一张图片</button>
          </div>

          <!-- 图片列表 -->
          <div v-else class="bf-image-grid">
            <div
              v-for="img in filteredImages"
              :key="img.imageId"
              class="bf-image-item"
              :class="{ 'bf-image-item--selected': selectedImage?.imageId === img.imageId }"
              @click="selectImage(img)"
              @dblclick="handleDoubleClick(img)"
            >
              <img :src="img.url" :alt="img.fileName" class="bf-image-thumb" loading="lazy" />
              <div class="bf-image-overlay">
                <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                </svg>
              </div>
              <div class="bf-image-name" :title="img.fileName">{{ img.fileName }}</div>
              <button 
                class="bf-image-delete-btn" 
                @click="(e) => deleteImage(img, e)"
                :disabled="isDeleting"
                title="删除图片"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="bf-picker-footer">
          <div class="bf-picker-info">
            <span>共 {{ images.length }} 张图片</span>
            <span v-if="selectedImage" class="bf-selected-count">已选择 1 张</span>
          </div>
          <div class="bf-picker-actions">
            <button @click="close" class="bf-btn bf-btn--ghost">
              取消
            </button>
            <button
              @click="confirmSelection"
              :disabled="!selectedImage"
              class="bf-btn bf-btn--primary"
            >
              确认选择
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.bf-image-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: var(--bf-space-lg, 24px);
}

.bf-image-picker {
  width: 100%;
  max-width: 800px;
  max-height: 80vh;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.95));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.4);
}

/* 头部 */
.bf-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 24px);
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-picker-title {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  font-weight: 600;
  font-size: 1rem;
  color: var(--bf-text-primary);
}

.bf-picker-title svg {
  color: var(--bf-primary, #FF6B35);
}

.bf-close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--bf-text-muted);
  cursor: pointer;
  transition: all var(--bf-transition-fast);
}

.bf-close-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--bf-text-primary);
}

/* 搜索框和上传栏 */
.bf-picker-search-bar {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-picker-search {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  flex: 1;
  padding: var(--bf-space-sm, 8px);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
}

.bf-picker-search svg {
  color: var(--bf-text-muted);
  flex-shrink: 0;
}

.bf-search-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  outline: none;
}

.bf-search-input::placeholder {
  color: var(--bf-text-muted);
}

/* 上传按钮 */
.bf-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--bf-primary, #FF6B35);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast);
}

.bf-upload-btn:hover:not(:disabled) {
  background: #ff7a47;
}

.bf-upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.bf-hidden-input {
  display: none;
}

/* 小spinner */
.bf-spinner--small {
  width: 16px;
  height: 16px;
  border-width: 2px;
}

/* 空状态的上传提示 */
.bf-upload-hint-btn {
  margin-top: 12px;
  padding: 8px 16px;
  background: var(--bf-primary, #FF6B35);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 0.875rem;
  cursor: pointer;
}

/* 错误提示 */
.bf-error-message {
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border-bottom: 1px solid rgba(239, 68, 68, 0.2);
  color: #ef4444;
  font-size: 0.875rem;
}

/* 内容区域 */
.bf-picker-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--bf-space-md, 16px);
  min-height: 300px;
}

/* 加载状态 */
.bf-loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  gap: var(--bf-space-md, 16px);
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
  height: 300px;
  gap: var(--bf-space-md, 16px);
  color: var(--bf-text-muted);
}

/* 图片网格 */
.bf-image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--bf-space-md, 16px);
}

.bf-image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--bf-card-radius-sm, 12px);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all var(--bf-transition-fast);
}

.bf-image-item:hover {
  border-color: var(--bf-primary, #FF6B35);
  transform: scale(1.02);
}

.bf-image-item--selected {
  border-color: var(--bf-primary, #FF6B35);
  box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.2);
}

.bf-image-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bf-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 107, 53, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--bf-transition-fast);
}

.bf-image-item--selected .bf-image-overlay {
  opacity: 1;
}

.bf-image-overlay svg {
  color: white;
}

/* 图片名称 */
.bf-image-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 20px;
  padding: 4px 8px;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, transparent 100%);
  color: white;
  font-size: 0.75rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 删除按钮 */
.bf-image-delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s ease;
  z-index: 10;
}

.bf-image-item:hover .bf-image-delete-btn {
  opacity: 1;
}

.bf-image-delete-btn:hover:not(:disabled) {
  background: #dc2626;
  transform: scale(1.1);
}

.bf-image-delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 删除错误提示 */
.bf-error-message--delete {
  background: rgba(239, 68, 68, 0.15);
  border-bottom-color: rgba(239, 68, 68, 0.3);
  animation: fadeOut 3s forwards;
}

@keyframes fadeOut {
  0%, 80% { opacity: 1; }
  100% { opacity: 0; }
}

/* 底部 */
.bf-picker-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 24px);
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  background: var(--bf-surface, rgba(26, 26, 26, 0.5));
}

.bf-picker-info {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  font-size: 0.875rem;
  color: var(--bf-text-muted);
}

.bf-selected-count {
  color: var(--bf-primary, #FF6B35);
  font-weight: 500;
}

.bf-picker-actions {
  display: flex;
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

/* 响应式 */
@media (max-width: 640px) {
  .bf-image-picker-overlay {
    padding: var(--bf-space-md, 16px);
  }

  .bf-image-picker {
    max-height: 90vh;
  }

  .bf-image-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: var(--bf-space-sm, 8px);
  }

  .bf-picker-footer {
    flex-direction: column;
    gap: var(--bf-space-md, 16px);
  }

  .bf-picker-actions {
    width: 100%;
  }

  .bf-picker-actions .bf-btn {
    flex: 1;
  }
}
</style>
