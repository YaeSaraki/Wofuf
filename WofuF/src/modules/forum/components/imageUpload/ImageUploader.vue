<script lang="ts" setup>
import { ref, computed } from 'vue'
import { imageService } from '@M/forum/services/ImageService.ts'
import { translate } from '@S/services/i18n'

const props = defineProps<{
  maxImages?: number
  folder?: string
}>()

const emit = defineEmits<{
  (e: 'upload', markdown: string, url: string): void
  (e: 'error', message: string): void
}>()

// 配置
const maxImages = computed(() => props.maxImages ?? 9)
const folder = computed(() => props.folder ?? 'posts')

// 状态
const isUploading = ref(false)
const uploadProgress = ref(0)

// 文件输入引用
const fileInput = ref<HTMLInputElement | null>(null)

// 触发文件选择
function triggerUpload() {
  fileInput.value?.click()
}

// 处理文件选择
async function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  await uploadFile(file)

  // 清空 input，允许重复选择相同文件
  target.value = ''
}

// 处理拖拽
function handleDrop(event: DragEvent) {
  event.preventDefault()
  const file = event.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('image/')) {
    uploadFile(file)
  }
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
}

// 上传文件
async function uploadFile(file: File) {
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    emit('error', '不支持的文件类型，仅支持 JPEG, PNG, GIF, WebP')
    return
  }

  // 验证文件大小 (10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    emit('error', '文件大小不能超过 10MB')
    return
  }

  isUploading.value = true
  uploadProgress.value = 0

  // 模拟进度
  const progressInterval = setInterval(() => {
    if (uploadProgress.value < 90) {
      uploadProgress.value += 10
    }
  }, 100)

  try {
    const result = await imageService.uploadImage(file, folder.value)

    clearInterval(progressInterval)
    uploadProgress.value = 100

    if (result.isSuccess) {
      const data = result.getValue()
      emit('upload', data.markdown, data.url)
    } else {
      emit('error', String(result.error) || '上传失败')
    }
  } catch (error) {
    clearInterval(progressInterval)
    emit('error', '上传失败，请重试')
  } finally {
    setTimeout(() => {
      isUploading.value = false
      uploadProgress.value = 0
    }, 500)
  }
}
</script>

<template>
  <div
    class="bf-image-uploader"
    @drop="handleDrop"
    @dragover="handleDragOver"
  >
    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="bf-file-input"
      @change="handleFileSelect"
    />

    <!-- 上传按钮 -->
    <button
      type="button"
      class="bf-upload-btn"
      :class="{ 'bf-upload-btn--uploading': isUploading }"
      :disabled="isUploading"
      @click="triggerUpload"
    >
      <!-- 上传中状态 -->
      <template v-if="isUploading">
        <div class="bf-upload-progress">
          <svg class="bf-progress-ring" viewBox="0 0 24 24">
            <circle
              class="bf-progress-ring-bg"
              cx="12"
              cy="12"
              r="10"
              fill="none"
              stroke-width="2"
            />
            <circle
              class="bf-progress-ring-fill"
              cx="12"
              cy="12"
              r="10"
              fill="none"
              stroke-width="2"
              :stroke-dasharray="`${uploadProgress * 0.628} 100`"
            />
          </svg>
          <span class="bf-progress-text">{{ uploadProgress }}%</span>
        </div>
      </template>

      <!-- 正常状态 -->
      <template v-else>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <span>{{ translate('forum', 'uploadImage') || '上传图片' }}</span>
      </template>
    </button>

    <!-- 提示信息 -->
    <div class="bf-upload-hint">
      <span class="bf-hint-text">支持 JPEG, PNG, GIF, WebP</span>
      <span class="bf-hint-divider">•</span>
      <span class="bf-hint-text">最大 10MB</span>
      <span class="bf-hint-divider">•</span>
      <span class="bf-hint-text">最多 {{ maxImages }} 张</span>
    </div>
  </div>
</template>

<style scoped>
.bf-image-uploader {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-sm, 8px);
}

.bf-file-input {
  display: none;
}

.bf-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px dashed var(--bf-border-default, rgba(255, 255, 255, 0.15));
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary, #B3B3B3);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-upload-btn:hover:not(:disabled) {
  border-color: var(--bf-primary, #FF6B35);
  color: var(--bf-primary, #FF6B35);
  background: rgba(255, 107, 53, 0.05);
}

.bf-upload-btn--uploading {
  border-style: solid;
  border-color: var(--bf-primary, #FF6B35);
  cursor: default;
}

.bf-upload-progress {
  position: relative;
  width: 40px;
  height: 40px;
}

.bf-progress-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.bf-progress-ring-bg {
  stroke: rgba(255, 255, 255, 0.1);
}

.bf-progress-ring-fill {
  stroke: var(--bf-primary, #FF6B35);
  stroke-linecap: round;
  transition: stroke-dasharray 0.1s ease;
}

.bf-progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.625rem;
  font-weight: 600;
  color: var(--bf-text-primary, #FFFFFF);
}

.bf-upload-hint {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  font-size: 0.75rem;
  color: var(--bf-text-muted, #666666);
}

.bf-hint-divider {
  opacity: 0.5;
}

/* 拖拽状态 */
.bf-image-uploader.drag-over .bf-upload-btn {
  border-color: var(--bf-primary, #FF6B35);
  background: rgba(255, 107, 53, 0.1);
}
</style>
