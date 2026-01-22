<script setup lang="ts">
import { onMounted, ref, onUnmounted, nextTick } from 'vue'
import { PlayerService } from '@M/players/services/PlayerService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import type { PlayerName, PlayerNameList } from '@M/players/dtos/PlayerName.ts'
import { translate } from '@S/services/i18n'
import router from '@S/infra/router'

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 业务状态 ---------------- */
const playerService = new PlayerService()
const playerNameList = ref<PlayerNameList | null>(null)

/* ---------------- 头像加载管理器 ---------------- */
const avatarCache = ref(new Map<string, string>())
const loadingSet = ref(new Set<string>())
const errorSet = ref(new Set<string>())
const requestQueue = ref<string[]>([])
const isProcessingQueue = ref(false)

// 配置常量
const AVATAR_REQUEST_INTERVAL = 600 // 600ms，比 API 限制的 500ms 稍长
const MAX_RETRY_COUNT = 2 // 最大重试次数
const RETRY_DELAY = 1000 // 重试延迟 1秒

// 占位图（使用更小的 base64）
const placeholderImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9IiNlNWU1ZTUiLz48Y2lyY2xlIGN4PSIzMiIgY3k9IjIzIiByPSI4IiBmaWxsPSIjOTk5Ii8+PGNpcmNsZSBjeD0iMzIiIGN5PSI0NSIgcj0iMTIiIGZpbGw9IiM5OTkiLz48L3N2Zz4='

// 安全地构建头像 URL
const buildAvatarUrl = (playerName: string): string => {
  try {
    return `https://mc-heads.net/avatar/${encodeURIComponent(playerName)}/64`
  } catch (error) {
    console.error('Error building avatar URL:', error)
    return ''
  }
}

// 获取头像 URL（智能缓存）
const getAvatarUrl = (playerName: string): string => {
  // 1. 检查缓存
  if (avatarCache.value.has(playerName)) {
    return avatarCache.value.get(playerName)!
  }

  // 2. 检查是否正在加载或已出错
  if (loadingSet.value.has(playerName) || errorSet.value.has(playerName)) {
    return placeholderImage
  }

  // 3. 加入队列异步加载
  if (!requestQueue.value.includes(playerName)) {
    requestQueue.value.push(playerName)

    // 延迟处理队列，确保 DOM 已渲染
    nextTick(() => {
      processQueue().catch(console.error)
    })
  }

  return placeholderImage
}

// 处理队列（使用 async/await 和错误处理）
const processQueue = async (): Promise<void> => {
  if (isProcessingQueue.value || requestQueue.value.length === 0) {
    return
  }

  isProcessingQueue.value = true

  try {
    while (requestQueue.value.length > 0) {
      const playerName = requestQueue.value.shift()!

      // 跳过已经在处理或已缓存的
      if (avatarCache.value.has(playerName) || loadingSet.value.has(playerName)) {
        continue
      }

      await loadSingleAvatar(playerName)

      // 如果不是最后一个，等待间隔时间
      if (requestQueue.value.length > 0) {
        await delay(AVATAR_REQUEST_INTERVAL)
      }
    }
  } catch (error) {
    console.error('Error processing avatar queue:', error)
  } finally {
    isProcessingQueue.value = false
  }
}

// 加载单个头像（支持重试）
const loadSingleAvatar = async (playerName: string, retryCount = 0): Promise<void> => {
  loadingSet.value.add(playerName)

  try {
    const avatarUrl = buildAvatarUrl(playerName)
    if (!avatarUrl) {
      throw new Error('Invalid avatar URL')
    }

    // 使用 Fetch API 加载，可以更好地控制超时和重试
    const response = await fetch(avatarUrl, {
      mode: 'cors',
      headers: {
        'Accept': 'image/*'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    // 验证是否是有效的图片
    const blob = await response.blob()
    if (!blob.type.startsWith('image/')) {
      throw new Error('Response is not an image')
    }

    // 创建 Object URL（避免直接使用外部 URL）
    const objectUrl = URL.createObjectURL(blob)

    // 预加载验证图片有效性
    await new Promise<void>((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        URL.revokeObjectURL(objectUrl) // 释放 Object URL
        const validUrl = avatarUrl // 使用原始 URL
        avatarCache.value.set(playerName, validUrl)
        loadingSet.value.delete(playerName)
        errorSet.value.delete(playerName)
        resolve()
      }
      img.onerror = () => {
        URL.revokeObjectURL(objectUrl)
        reject(new Error('Image failed to load'))
      }
      img.src = objectUrl
    })

  } catch (error) {
    console.warn(`Failed to load avatar for ${playerName}:`, error)

    // 重试逻辑
    if (retryCount < MAX_RETRY_COUNT) {
      await delay(RETRY_DELAY * (retryCount + 1))
      return loadSingleAvatar(playerName, retryCount + 1)
    }

    // 重试失败，标记为错误
    errorSet.value.add(playerName)
    loadingSet.value.delete(playerName)
  }
}

// 延迟函数
const delay = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 头像加载成功处理函数
const handleAvatarLoad = (playerName: string, event: Event): void => {
  const target = event.target as HTMLImageElement

  if (!target || !target.src) {
    console.warn('Invalid load event target for', playerName)
    return
  }

  // 验证 src 是有效的 URL
  try {
    new URL(target.src)
  } catch {
    console.warn('Invalid image URL for', playerName)
    return
  }

  // 更新缓存
  avatarCache.value.set(playerName, target.src)
  loadingSet.value.delete(playerName)
  errorSet.value.delete(playerName)

  // 触发响应式更新（如果需要）
  avatarCache.value = new Map(avatarCache.value)
}

// 头像加载失败处理函数
const handleAvatarError = (playerName: string): void => {
  console.warn(`Avatar load error for: ${playerName}`)

  // 标记为错误
  errorSet.value.add(playerName)
  loadingSet.value.delete(playerName)

  // 如果仍在队列中，移除
  const queueIndex = requestQueue.value.indexOf(playerName)
  if (queueIndex > -1) {
    requestQueue.value.splice(queueIndex, 1)
  }
}

// 重置所有状态
const resetAvatarState = (): void => {
  // 清理 Object URLs（如果有的话）
  avatarCache.value.forEach(url => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url)
    }
  })
  avatarCache.value.clear()
  loadingSet.value.clear()
  errorSet.value.clear()
  requestQueue.value.length = 0
  isProcessingQueue.value = false
}

// 防抖队列处理
const queueProcessingTimeout: number | null = null
/* ---------------- 业务行为 ---------------- */
async function loadYesterdayOnlinePlayers() {
  // 重置头像状态
  resetAvatarState()

  const result = await executeAsync(
    async (signal) => {
      const result = await playerService.getPlayerYesterdayOnline({ signal })
      if (result.isSuccess) {
        return result.getValue()
      }
      return null
    },
    translate('players', 'error.loading-yesterday-online'),
  )

  if (result) {
    playerNameList.value = result
  }
}

function goToPlayerProfile(playerName: PlayerName): void {
  router.push(`/players/${encodeURIComponent(playerName)}`)
}

// 组件生命周期
onMounted(() => {
  loadYesterdayOnlinePlayers()
})

onUnmounted(() => {
  resetAvatarState()
  if (queueProcessingTimeout) {
    clearTimeout(queueProcessingTimeout)
  }
})
</script>

<template>
  <section class="yesterday-online">
    <h2 class="title text-center"> {{ translate('players', 'yesterday_online_players') }} </h2>

    <div v-if="isLoading" class="hint text-center">
      {{ translate('players', 'loading-yesterday-online') }}
    </div>

    <div v-else-if="errorMsg" class="hint text-center">
      <p>{{ errorMsg }}</p>
      <button
        class="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600 transition-colors"
        @click="loadYesterdayOnlinePlayers"
      >
        {{ translate('app', 'actions.retry') }}
      </button>
    </div>

    <div v-else class="card-list ml-8 mr-8">
      <div
        v-for="item in playerNameList?.playerNames"
        :key="item"
        class="player-card"
        @click="goToPlayerProfile(item)"
      >
        <img
          class="avatar"
          :src="getAvatarUrl(item)"
          :alt="`${item}'s avatar`"
          :class="{ 'opacity-50': loadingSet.has(item), 'grayscale': errorSet.has(item) }"
          loading="lazy"
          decoding="async"
          @load="(event: Event) => handleAvatarLoad(item, event)"
          @error="() => handleAvatarError(item)"
        />
        <div class="name">{{ item }}</div>

        <!-- 加载状态指示器 -->
        <div v-if="loadingSet.has(item)" class="loading-indicator">
          <div class="loading-spinner"></div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.yesterday-online {
  padding: 24px;
}

.title {
  font-size: 18px;
  margin-bottom: 16px;
  font-weight: 600;
}

.hint {
  color: #6b7280;
  font-size: 14px;
  padding: 20px;
}

.card-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.player-card {
  border-radius: 12px;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.2s ease;
  position: relative;
  width: 100px;
  cursor: pointer;
}

.player-card:hover {
  transform: translateY(-2px);
}

.avatar {
  border-radius: 10px;
  margin-bottom: 8px;
  width: 64px;
  height: 64px;
  object-fit: cover;
  background-color: #f3f4f6;
  transition: opacity 0.3s ease, filter 0.3s ease;
}

.avatar.grayscale {
  filter: grayscale(100%);
}

.name {
  font-size: 12px;
  text-align: center;
  word-break: break-word;
  max-width: 80px;
  color: #374151;
  font-weight: 500;
  line-height: 1.4;
}

.loading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.dark .player-card {
  background: #1f2937;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark .name {
  color: #d1d5db;
}

.dark .avatar {
  background-color: #374151;
}

@media (max-width: 640px) {
  .card-list {
    gap: 12px;
  }

  .player-card {
    width: 88px;
    padding: 12px 8px;
  }

  .avatar {
    width: 56px;
    height: 56px;
  }
}
</style>
