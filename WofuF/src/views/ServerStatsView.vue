<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import PageBackground from '@S/components/PageBackground.vue'
import YesterdayOnlineList from '@M/players/components/yesterdayOnlineList/YesterdayOnlineList.vue'

const { translate } = useLocale()

// 服务器状态
const isOnline = ref(true)
const serverStats = ref()

// 计算属性
const statusClass = computed(() => isOnline.value ? 'status-online' : 'status-offline')
const statusText = computed(() => isOnline.value ? translate('app', 'status.online') : translate('app', 'status.offline'))

// 动画效果
const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
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
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">{{ translate('app', 'status.totalPlayers') }}</span>
                <span class="info-value">{{ serverStats?.totalPlayers ?? '1,245' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ translate('app', 'status.founded') }}</span>
                <span class="info-value">2024-01-01</span>
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

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
