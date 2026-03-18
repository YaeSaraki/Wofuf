<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import { useTheme } from '@S/composables/useTheme.ts'
import YesterdayOnlineList from '@M/players/components/yesterdayOnlineList/YesterdayOnlineList.vue'

const { translate } = useLocale()
const { isDark } = useTheme()

// 服务器状态数据
const serverStatus = ref({
  online: true,
  version: '1.21.3',
  playerCount: 23,
  maxPlayers: 100,
  uptime: '7d 12h 34m',
  tps: 19.98,
  memoryUsage: '2.4GB / 4GB',
  ping: 45
})

// 在线玩家列表
const onlinePlayers = ref([
  { name: 'Steve', rank: 'Admin', joinTime: '2h ago' },
  { name: 'Alex', rank: 'Member', joinTime: '1h ago' },
  { name: 'Notch', rank: 'Member', joinTime: '30m ago' },
  { name: 'Herobrine', rank: 'VIP', joinTime: '15m ago' },
  { name: 'Jeb', rank: 'Member', joinTime: '5m ago' }
])

// 系统状态
const systemStatus = ref([
  { name: 'Minecraft Server', status: 'online', message: '运行正常' },
  { name: 'Database', status: 'online', message: '连接正常' },
  { name: 'Discord Bot', status: 'online', message: '响应正常' },
  { name: 'Website', status: 'online', message: '访问正常' },
  { name: 'Backup Service', status: 'online', message: '备份正常' }
])

// 服务器统计
const serverStats = ref([
  { label: '总玩家数', value: '1,245' },
  { label: '今日在线', value: '156' },
  { label: '创建时间', value: '2024-01-01' },
  { label: '世界大小', value: '15GB' }
])

// 计算属性
const statusClass = computed(() => serverStatus.value.online ? 'status-online' : 'status-offline')
const statusText = computed(() => serverStatus.value.online ? translate('app', 'status.online') : translate('app', 'status.offline'))
const playerPercentage = computed(() => (serverStatus.value.playerCount / serverStatus.value.maxPlayers) * 100)

// 动画效果
const isVisible = ref(false)
onMounted(() => {
  // 触发动画
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
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
          <h2>{{ translate('players', 'yesterday_online') }}</h2>
        </div>
        <div class="card-content">
          <YesterdayOnlineList/>
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
              <span class="info-label">{{ translate('app', 'status.version') }}</span>
              <span class="info-value">{{ serverStatus.version }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ translate('app', 'status.uptime') }}</span>
              <span class="info-value">{{ serverStatus.uptime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ translate('app', 'status.tps') }}</span>
              <span class="info-value">{{ serverStatus.tps }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ translate('app', 'status.ping') }}</span>
              <span class="info-value">{{ serverStatus.ping }}ms</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 玩家信息 -->
      <div class="card">
        <div class="card-header">
          <h2>{{ translate('app', 'status.players') }}</h2>
          <span class="player-count">{{ serverStatus.playerCount }}/{{ serverStatus.maxPlayers }}</span>
        </div>
        <div class="card-content">
          <div class="player-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: playerPercentage + '%' }"></div>
            </div>
          </div>
          <div class="player-list">
            <div v-for="(player, index) in onlinePlayers" :key="index" class="player-item">
              <span class="player-name">{{ player.name }}</span>
              <span class="player-rank" :class="player.rank.toLowerCase()">{{ player.rank }}</span>
              <span class="player-join-time">{{ player.joinTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统状态 -->
      <div class="card">
        <div class="card-header">
          <h2>{{ translate('app', 'status.system') }}</h2>
        </div>
        <div class="card-content">
          <div class="system-status-list">
            <div v-for="(service, index) in systemStatus" :key="index" class="system-status-item">
              <span class="service-name">{{ service.name }}</span>
              <div class="service-status" :class="service.status">
                <div class="status-dot"></div>
                <span>{{ service.message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 服务器统计 -->
      <div class="card">
        <div class="card-header">
          <h2>{{ translate('app', 'status.stats') }}</h2>
        </div>
        <div class="card-content">
          <div class="stats-grid">
            <div v-for="(stat, index) in serverStats" :key="index" class="stat-item">
              <span class="stat-label">{{ stat.label }}</span>
              <span class="stat-value">{{ stat.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.status-page {
  min-height: 80vh;
  padding: 2rem 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  margin-bottom: 2rem;
  padding: 3rem 2rem;
  border-radius: 24px;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.35) 0%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.35) 100%
  );
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

html.dark .hero-section {
  background: linear-gradient(
    135deg,
    rgba(70, 70, 80, 0.45) 0%,
    rgba(60, 60, 67, 0.4) 50%,
    rgba(70, 70, 80, 0.45) 100%
  );
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.35),
    0 2px 8px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.12),
    inset 0 -1px 0 rgba(255, 255, 255, 0.05);
}

.hero-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.server-name {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.server-description {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  color: rgba(0, 0, 0, 0.7);
}

html.dark .server-description {
  color: rgba(255, 255, 255, 0.7);
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1rem;
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
  width: 12px;
  height: 12px;
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

/* 信息卡片 */
.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.2s;
}

.info-cards.visible {
  opacity: 1;
  transform: translateY(0);
}

.card {
  border-radius: 16px;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.3) 0%,
    rgba(255, 255, 255, 0.2) 100%
  );
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.1),
    0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

html.dark .card {
  background: linear-gradient(
    135deg,
    rgba(70, 70, 80, 0.4) 0%,
    rgba(60, 60, 67, 0.35) 100%
  );
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.3),
    0 1px 4px rgba(0, 0, 0, 0.15);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

html.dark .card:hover {
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.25);
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

html.dark .card-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.player-count {
  font-size: 1rem;
  font-weight: 500;
  color: #007AFF;
}

.card-content {
  padding: 1.25rem;
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
  gap: 0.25rem;
}

.info-label {
  font-size: 0.875rem;
  color: rgba(0, 0, 0, 0.6);
}

html.dark .info-label {
  color: rgba(255, 255, 255, 0.6);
}

.info-value {
  font-size: 1.125rem;
  font-weight: 600;
}

/* 玩家进度条 */
.player-progress {
  margin-bottom: 1rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

html.dark .progress-bar {
  background: rgba(255, 255, 255, 0.1);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-radius: 4px;
  transition: width 0.5s ease;
}

/* 玩家列表 */
.player-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.player-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
}

html.dark .player-item {
  background: rgba(255, 255, 255, 0.05);
}

.player-name {
  font-weight: 500;
}

.player-rank {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.player-rank.admin {
  background: rgba(239, 68, 68, 0.2);
  color: #dc2626;
}

.player-rank.vip {
  background: rgba(245, 158, 11, 0.2);
  color: #d97706;
}

.player-rank.member {
  background: rgba(59, 130, 246, 0.2);
  color: #2563eb;
}

.player-join-time {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.5);
}

html.dark .player-join-time {
  color: rgba(255, 255, 255, 0.5);
}

/* 系统状态 */
.system-status-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.system-status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
}

html.dark .system-status-item {
  background: rgba(255, 255, 255, 0.05);
}

.service-name {
  font-weight: 500;
}

.service-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.service-status.online {
  color: #059669;
}

.service-status.offline {
  color: #dc2626;
}

.service-status.maintenance {
  color: #d97706;
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  text-align: center;
  padding: 1rem;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
}

html.dark .stat-item {
  background: rgba(255, 255, 255, 0.05);
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(0, 0, 0, 0.6);
}

html.dark .stat-label {
  color: rgba(255, 255, 255, 0.6);
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 600;
}

/* 昨日在线玩家 */
.yesterday-section {
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.1s;
}

.yesterday-section.visible {
  opacity: 1;
  transform: translateY(0);
}



/* 响应式设计 */
@media (max-width: 768px) {
  .status-page {
    padding: 1rem 0.5rem;
  }

  .hero-section {
    padding: 2rem 1rem;
  }

  .server-name {
    font-size: 2.5rem;
  }

  .server-description {
    font-size: 1rem;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }

  .quick-links {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .link-card {
    width: 90%;
    max-width: 300px;
  }
}
</style>
