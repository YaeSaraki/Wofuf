<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import PageBackground from '@S/components/PageBackground.vue'
import YesterdayOnlineList from '@M/players/components/yesterdayOnlineList/YesterdayOnlineList.vue'
import PlayerSearch from '@M/players/components/playerSearch/PlayerSearch.vue'
import { playerService } from '@M/players'
import type { ServerStatus } from '@M/players/dtos/ServerStatus.ts'

const { translate } = useLocale()

// 服务器状态
const serverStatus = ref<ServerStatus | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
let refreshInterval: number | null = null

// 计算属性
const isOnline = computed(() => serverStatus.value?.heartbeatStatus ?? false)
const statusClass = computed(() => isOnline.value ? 'status-online' : 'status-offline')
const statusText = computed(() => isOnline.value ? translate('app', 'status.online') : translate('app', 'status.offline'))

const tpsColor = computed(() => {
  if (!serverStatus.value) return 'text-gray-500'
  const tps = parseFloat(serverStatus.value.tps)
  if (tps >= 18) return 'text-green-500'
  if (tps >= 15) return 'text-yellow-500'
  return 'text-red-500'
})

// 动画效果
const isVisible = ref(false)

// 获取服务器状态
const fetchServerStatus = async () => {
  // 防止重复请求
  if (loading.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const result = await playerService.getServerStatus()
    
    if (result.isSuccess) {
      serverStatus.value = result.getValue()
    } else {
      error.value = result.error
      console.error('获取服务器状态失败:', result.error)
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '获取服务器状态失败'
    console.error('获取服务器状态异常:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
  
  // 获取服务器状态
  fetchServerStatus()
  
  // 每30秒自动刷新
  refreshInterval = window.setInterval(fetchServerStatus, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<template>
  <PageBackground variant="default" :show-pattern="true">
    <!-- 页面头部 - 与论坛风格一致 -->
    <header class="bf-page-header">
      <div class="bf-header-content">
        <div class="bf-header-text">
          <h1 class="bf-page-title">
            <span class="bf-title-gradient">WofuF</span>
          </h1>
          <p class="bf-page-subtitle">{{ translate('app', 'status.description') }}</p>
        </div>
        <div class="status-indicator" :class="statusClass">
          <div class="status-dot"></div>
          <span>{{ statusText }}</span>
        </div>
      </div>
    </header>

    <div class="status-page">
      <!-- 玩家搜索 -->
      <section class="search-section" :class="{ 'visible': isVisible }">
        <div class="card">
          <div class="card-header">
            <h2>{{ translate('app', 'status.playerSearch') }}</h2>
          </div>
          <div class="card-content">
            <PlayerSearch />
          </div>
        </div>
      </section>

      <!-- 昨日在线玩家 -->
      <section class="yesterday-section" :class="{ 'visible': isVisible }">
        <div class="card">
          <div class="card-header">
            <h2>{{ translate('app', 'status.yesterdayOnline') }}</h2>
          </div>
          <div class="card-content">
            <YesterdayOnlineList />
          </div>
        </div>
      </section>

      <!-- 服务器信息卡片 -->
      <section class="info-cards" :class="{ 'visible': isVisible }">
        <div class="card primary-card">
          <div class="card-header">
            <h2>{{ translate('app', 'status.serverInfo') }}</h2>
          </div>
          <div class="card-content">
            <!-- 加载状态 -->
            <div v-if="loading && !serverStatus" class="loading-state">
              <div class="loading-spinner"></div>
              <p>{{ translate('app', 'common.loading') }}</p>
            </div>
            
            <!-- 错误提示 -->
            <div v-else-if="error" class="error-state">
              <div class="error-icon">⚠️</div>
              <p>{{ error }}</p>
              <button @click="fetchServerStatus" class="retry-button">
                {{ translate('app', 'common.retry') }}
              </button>
            </div>
            
            <!-- 服务器状态信息 -->
            <div v-else-if="serverStatus" class="server-status-grid">
              <div class="status-item">
                <div class="status-icon">👥</div>
                <div class="status-details">
                  <div class="status-label">{{ translate('app', 'status.onlinePlayers') }}</div>
                  <div class="status-value">
                    <span class="online-count">{{ serverStatus.onlinePlayers }}</span>
                    <span class="separator">/</span>
                    <span class="max-count">{{ serverStatus.maxPlayers }}</span>
                  </div>
                </div>
              </div>
              
              <div class="status-item">
                <div class="status-icon">⚡</div>
                <div class="status-details">
                  <div class="status-label">{{ translate('app', 'status.tps') }}</div>
                  <div class="status-value" :class="tpsColor">{{ serverStatus.tps }}</div>
                </div>
              </div>
              
              <div class="status-item">
                <div class="status-icon">💓</div>
                <div class="status-details">
                  <div class="status-label">{{ translate('app', 'status.heartbeat') }}</div>
                  <div class="status-value">
                    <span :class="serverStatus.heartbeatStatus ? 'status-online-text' : 'status-offline-text'">
                      {{ serverStatus.heartbeatStatus ? '在线' : '离线' }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="status-item">
                <div class="status-icon">🕐</div>
                <div class="status-details">
                  <div class="status-label">{{ translate('app', 'status.lastUpdate') }}</div>
                  <div class="status-value time-value">
                    {{ new Date(serverStatus.updateTime).toLocaleString('zh-CN') }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </PageBackground>
</template>

<style scoped>
/* 头部 - 与论坛风格一致的橙色渐变 */
.bf-page-header {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  padding: 3rem 1rem;
  position: relative;
  overflow: hidden;
}

.bf-page-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.1) 100%);
  pointer-events: none;
}

/* 暗色模式 */
:global(.dark) .bf-page-header {
  background: linear-gradient(135deg, #E55A25 0%, #E88A1C 30%, #FF6B35 60%, #FF9F1C 100%);
}

:global(.dark) .bf-page-header::before {
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.2) 100%);
}

.bf-header-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.bf-header-text {
  flex: 1;
}

.bf-page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  line-height: 1.2;
}

.bf-title-gradient {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF5EB 50%, #FFE4CC 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* 暗色模式 - 标题更亮 */
:global(.dark) .bf-title-gradient {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFFFFF 50%, #FFF5EB 100%);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 15px rgba(0, 0, 0, 0.3);
}

.bf-page-subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 暗色模式 - 副标题 */
:global(.dark) .bf-page-subtitle {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 状态指示器 */
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1rem;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:global(.dark) .status-indicator {
  background: rgba(30, 30, 30, 0.95);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.status-online {
  color: #059669;
}

.status-offline {
  color: #dc2626;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-online .status-dot {
  background: #10b981;
}

.status-offline .status-dot {
  background: #ef4444;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 页面内容 */
.status-page {
  min-height: 60vh;
  padding: 2rem 1rem;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 卡片样式 */
.card {
  border-radius: var(--bf-radius-lg, 16px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
  transition: all 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.card-content {
  padding: 1.25rem;
}

/* 玩家搜索 */
.search-section {
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.1s;
}

.search-section.visible {
  opacity: 1;
  transform: translateY(0);
}

/* 昨日在线 */
.yesterday-section {
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.2s;
}

.yesterday-section.visible {
  opacity: 1;
  transform: translateY(0);
}

/* 信息卡片 */
.info-cards {
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.3s;
}

.info-cards.visible {
  opacity: 1;
  transform: translateY(0);
}

/* 信息网格 */
.server-status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  padding: 1rem 0;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: var(--bf-radius-md, 12px);
  background: var(--bf-surface-variant, rgba(255, 255, 255, 0.5));
  transition: all 0.2s ease;
}

.status-item:hover {
  background: var(--bf-surface-hover, rgba(255, 255, 255, 0.7));
  transform: translateY(-2px);
}

.status-icon {
  font-size: 2rem;
  line-height: 1;
  flex-shrink: 0;
}

.status-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.status-label {
  font-size: 0.875rem;
  color: var(--bf-text-tertiary, #6b7285);
  font-weight: 500;
}

.status-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--bf-text-primary, #1a1a2e);
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
}

.online-count {
  color: #10b981;
  font-size: 1.5rem;
}

.separator {
  color: var(--bf-text-tertiary, #6b7285);
  font-weight: 400;
}

.max-count {
  color: var(--bf-text-secondary, #6b7280);
  font-size: 1rem;
}

.time-value {
  font-size: 0.9rem;
  font-weight: 500;
}

.status-online-text {
  color: #10b981;
  font-weight: 600;
}

.status-offline-text {
  color: #ef4444;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-label {
  font-size: 0.875rem;
  color: var(--bf-text-tertiary, #6b7285);
}

.info-value {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--bf-text-primary, #1a1a2e);
}

/* TPS颜色 */
.text-green-500 {
  color: #10b981;
}

.text-yellow-500 {
  color: #f59e0b;
}

.text-red-500 {
  color: #ef4444;
}

.text-gray-500 {
  color: #6b7280;
}

/* 加载和错误状态 */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3rem 1rem;
  text-align: center;
  color: var(--bf-text-tertiary, #6b7285);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 107, 53, 0.2);
  border-top-color: #FF6B35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-icon {
  font-size: 3rem;
  line-height: 1;
}

.error-state {
  color: var(--bf-text-error, #ef4444);
}

.error-state p {
  max-width: 400px;
  word-wrap: break-word;
}

.retry-button {
  padding: 0.5rem 1.5rem;
  border-radius: var(--bf-radius-md, 8px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.1));
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  color: var(--bf-text-primary, #1a1a2e);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.retry-button:hover {
  background: var(--bf-surface-hover, rgba(255, 255, 255, 0.95));
  border-color: var(--bf-border, rgba(0, 0, 0, 0.15));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bf-page-header {
    padding: 2rem 1rem;
  }

  .bf-page-title {
    font-size: 1.75rem;
  }

  .bf-page-subtitle {
    font-size: 0.875rem;
  }

  .bf-header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .status-indicator {
    width: 100%;
    justify-content: center;
  }

  .status-page {
    padding: 1rem 0.5rem;
  }

  .card {
    padding: 1.25rem;
  }

  .server-status-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
