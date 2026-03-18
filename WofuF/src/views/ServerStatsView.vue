<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import PageBackground from '@S/components/PageBackground.vue'
import YesterdayOnlineList from '@M/forum/components/yesterdayOnline/YesterdayOnlineList.vue'
import { useServerStatsStore } from '@S/modules/players/stores/serverStats'

const { translate } = useLocale()

// 服务器状态
const isOnline = ref(true)
const serverStats = useServerStatsStore()

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
    <div class="status-page">
      <!-- 英雄区域 -->
      <section class="hero-section" :class="{ 'visible': isVisible }">
        <div class="hero-content">
          <h1 class="server-name">WofuF</h1>
          <p class="server-description">{{ translate('app', 'status.description') }}</p>
          <div class="status-indicator" :class="statusClass">
            <div class="status-dot"></div>
            <span>{{ statusText }}</span>
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
        <!-- 基本信息 -->
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
.status-page {
  min-height: 80vh;
  padding: 2rem 1rem;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  margin-bottom: 2rem;
  padding: 3rem 2rem;
  border-radius: var(--bf-radius-xl, 24px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.server-name {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: var(--bf-text-primary, #1a1a2e);
}

.server-description {
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
  color: var(--bf-text-secondary, #4a4a68);
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1rem;
}

.status-online {
  background: rgba(52, 211, 153, 0.2);
  color: #059669;
}

.status-offline {
  background: rgba(239, 68, 68, 0.2);
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
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
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

.primary-card {
  grid-column: span 2;
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

/* 匼应式设计 */
@media (max-width: 768px) {
  .status-page {
    padding: 1rem 0.5rem;
  }

  .hero-section {
    padding: 2rem 1rem;
  }

  .hero-content h1 {
    font-size: 2.5rem;
  }

  .hero-content p {
    font-size: 1rem;
  }

  .card {
    padding: 1.25rem;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }
}
</style>
