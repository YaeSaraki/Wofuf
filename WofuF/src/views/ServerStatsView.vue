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

// 动画控制
const isVisible = ref(false)
const textVisible = ref(false)

// 计算属性
const isOnline = computed(() => serverStatus.value?.heartbeatStatus ?? false)
const statusClass = computed(() => isOnline.value ? 'status-online' : 'status-offline')
const statusText = computed(() => isOnline.value ? translate('app', 'status.online') : translate('app', 'status.offline'))

const tpsColor = computed(() => {
  if (!serverStatus.value) return 'text-gray-400'
  const tps = parseFloat(serverStatus.value.tps)
  if (tps >= 18) return 'text-emerald-400'
  if (tps >= 15) return 'text-amber-400'
  return 'text-rose-400'
})

// 获取服务器状态
const fetchServerStatus = async () => {
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

// 格式化时间
const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
  
  setTimeout(() => {
    textVisible.value = true
  }, 300)
  
  fetchServerStatus()
  refreshInterval = window.setInterval(fetchServerStatus, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<template>
  <div class="page-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="logo">
          <span class="logo-text">WofuF</span>
        </div>
        <div class="nav-links">
          <a href="#" class="nav-link">首页</a>
          <a href="#" class="nav-link">玩家</a>
          <a href="#" class="nav-link active">服务器</a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <!-- 背景渐变 -->
      <div class="hero-gradient"></div>
      
      <!-- 内容 -->
      <div class="hero-content" :class="{ visible: textVisible }">
        <!-- 状态徽章 -->
        <div class="status-badge liquid-glass">
          <div class="badge-dot" :class="statusClass"></div>
          <span>{{ statusText }}</span>
        </div>

        <!-- 主标题 -->
        <h1 class="hero-title">
          <span class="title-line">Minecraft</span>
          <span class="title-line italic">服务器状态</span>
        </h1>

        <!-- 副标题 -->
        <p class="hero-subtitle">
          实时监控服务器性能 · 时刻掌握服务器状态
        </p>
      </div>

      <!-- 底部渐变 -->
      <div class="hero-fade"></div>
    </section>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 玩家搜索 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">搜索</div>
          <h2 class="section-title">玩家搜索</h2>
        </div>
        <div class="glass-card">
          <PlayerSearch />
        </div>
      </section>

      <!-- 昨日在线玩家 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">统计</div>
          <h2 class="section-title">昨日在线玩家</h2>
        </div>
        <div class="glass-card">
          <YesterdayOnlineList />
        </div>
      </section>

      <!-- 服务器状态 -->
      <section class="section server-status-section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">实时</div>
          <h2 class="section-title">服务器状态</h2>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="loading && !serverStatus" class="loading-container">
          <div class="liquid-glass loading-card">
            <div class="loading-spinner"></div>
            <p class="loading-text">正在获取服务器状态...</p>
          </div>
        </div>
        
        <!-- 错误提示 -->
        <div v-else-if="error" class="error-container">
          <div class="liquid-glass error-card">
            <div class="error-icon">⚠️</div>
            <p class="error-text">{{ error }}</p>
            <button @click="fetchServerStatus" class="retry-button liquid-glass-strong">
              重试
            </button>
          </div>
        </div>
        
        <!-- 服务器状态信息 -->
        <div v-else-if="serverStatus" class="server-stats-grid">
          <!-- 在线人数 -->
          <div class="stat-card liquid-glass">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
              <div class="stat-label">在线玩家</div>
              <div class="stat-value">
                <span class="online-count">{{ serverStatus.onlinePlayers }}</span>
                <span class="separator">/</span>
                <span class="max-count">{{ serverStatus.maxPlayers }}</span>
              </div>
              <div class="stat-bar">
                <div 
                  class="stat-bar-fill" 
                  :style="{ width: `${(serverStatus.onlinePlayers / serverStatus.maxPlayers) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>

          <!-- TPS -->
          <div class="stat-card liquid-glass">
            <div class="stat-icon">⚡</div>
            <div class="stat-content">
              <div class="stat-label">服务器 TPS</div>
              <div class="stat-value" :class="tpsColor">
                {{ serverStatus.tps }}
              </div>
              <div class="tps-indicator">
                <div class="tps-bar" :class="tpsColor"></div>
              </div>
            </div>
          </div>

          <!-- 心跳状态 -->
          <div class="stat-card liquid-glass">
            <div class="stat-icon">💓</div>
            <div class="stat-content">
              <div class="stat-label">心跳检测</div>
              <div class="stat-value">
                <span :class="serverStatus.heartbeatStatus ? 'status-online-text' : 'status-offline-text'">
                  {{ serverStatus.heartbeatStatus ? '在线' : '离线' }}
                </span>
              </div>
              <div class="heartbeat-animation" :class="{ active: serverStatus.heartbeatStatus }">
                <div class="heartbeat-pulse"></div>
              </div>
            </div>
          </div>

          <!-- 更新时间 -->
          <div class="stat-card liquid-glass">
            <div class="stat-icon">🕐</div>
            <div class="stat-content">
              <div class="stat-label">最后更新</div>
              <div class="stat-value time-value">
                {{ formatTime(serverStatus.updateTime) }}
              </div>
              <div class="auto-refresh">
                <span class="refresh-icon">🔄</span>
                <span class="refresh-text">每30秒自动刷新</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-content">
        <p class="footer-text">© 2026 WofuF. All rights reserved.</p>
        <div class="footer-links">
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">服务条款</a>
          <a href="#" class="footer-link">联系我们</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 字体引入 */
@import url('https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Barlow:wght@300;400;500;600&display=swap');

/* CSS变量 */
:root {
  --background: 213 45% 5%;
  --foreground: 0 0% 100%;
  --card: 213 45% 8%;
  --card-foreground: 0 0% 100%;
  --primary: 0 0% 100%;
  --primary-foreground: 213 45% 5%;
  --accent: 213 45% 15%;
  --accent-foreground: 0 0% 100%;
  --muted: 213 35% 20%;
  --muted-foreground: 0 0% 100% / 0.6;
  --border: 0 0% 100% / 0.1;
  --glass-bg: rgba(255, 255, 255, 0.03);
  --glass-border: rgba(255, 255, 255, 0.1);
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

/* 页面容器 */
.page-container {
  min-height: 100vh;
  background: hsl(var(--background));
  color: hsl(var(--foreground));
  font-family: 'Barlow', sans-serif;
  overflow-x: hidden;
}

/* 液态玻璃效果 */
.liquid-glass {
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 
    inset 0 1px 1px rgba(255, 255, 255, 0.06),
    0 8px 32px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.liquid-glass::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.3) 0%,
    rgba(255, 255, 255, 0.1) 20%,
    rgba(255, 255, 255, 0) 40%,
    rgba(255, 255, 255, 0) 60%,
    rgba(255, 255, 255, 0.1) 80%,
    rgba(255, 255, 255, 0.3) 100%
  );
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.liquid-glass-strong {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 
    inset 0 1px 1px rgba(255, 255, 255, 0.1),
    0 12px 40px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.liquid-glass-strong::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.4) 0%,
    rgba(255, 255, 255, 0.15) 20%,
    rgba(255, 255, 255, 0) 40%,
    rgba(255, 255, 255, 0) 60%,
    rgba(255, 255, 255, 0.15) 80%,
    rgba(255, 255, 255, 0.4) 100%
  );
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

/* 导航栏 */
.navbar {
  position: fixed;
  top: 1rem;
  left: 0;
  right: 0;
  z-index: 50;
  padding: 0 2rem;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 2rem;
  border-radius: 9999px;
}

.logo {
  font-size: 1.5rem;
  font-weight: 600;
  font-family: 'Instrument Serif', serif;
  font-style: italic;
}

.nav-links {
  display: none;
  gap: 0.5rem;
  padding: 0.25rem;
  border-radius: 9999px;
}

@media (min-width: 768px) {
  .nav-links {
    display: flex;
  }
}

.nav-link {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  border-radius: 9999px;
  transition: all 0.3s ease;
}

.nav-link:hover,
.nav-link.active {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

/* Hero Section */
.hero-section {
  position: relative;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6rem 2rem 4rem;
  overflow: hidden;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at 50% 30%,
    rgba(99, 102, 241, 0.15) 0%,
    transparent 50%
  );
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 800px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 2rem;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-online .badge-dot {
  background: #10b981;
  box-shadow: 0 0 12px #10b981;
}

.status-offline .badge-dot {
  background: #ef4444;
  box-shadow: 0 0 12px #ef4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-family: 'Instrument Serif', serif;
  font-size: 3.5rem;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  font-weight: 400;
}

@media (min-width: 768px) {
  .hero-title {
    font-size: 5rem;
  }
}

.title-line {
  display: block;
  letter-spacing: -0.02em;
}

.title-line.italic {
  font-style: italic;
}

.hero-subtitle {
  font-size: 1.125rem;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
}

.hero-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(to top, hsl(var(--background)), transparent);
  pointer-events: none;
}

/* 主内容区域 */
.main-content {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem 4rem;
}

/* Section */
.section {
  margin-bottom: 4rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.section.visible {
  opacity: 1;
  transform: translateY(0);
}

.section:nth-child(2) {
  transition-delay: 0.1s;
}

.section:nth-child(3) {
  transition-delay: 0.2s;
}

.section:nth-child(4) {
  transition-delay: 0.3s;
}

.section-header {
  margin-bottom: 1.5rem;
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.875rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
}

.section-title {
  font-family: 'Instrument Serif', serif;
  font-size: 2rem;
  font-weight: 400;
  font-style: italic;
  letter-spacing: -0.01em;
}

@media (min-width: 768px) {
  .section-title {
    font-size: 2.5rem;
  }
}

.glass-card {
  border-radius: 1.5rem;
  padding: 1.5rem;
}

@media (min-width: 768px) {
  .glass-card {
    padding: 2rem;
  }
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  padding: 3rem 1rem;
}

.loading-card,
.error-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 3rem 2rem;
  border-radius: 1.5rem;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 0.9375rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
}

.error-icon {
  font-size: 3rem;
  line-height: 1;
}

.error-text {
  font-size: 0.9375rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
  max-width: 400px;
}

.retry-button {
  padding: 0.75rem 2rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

/* 服务器状态网格 */
.server-stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .server-stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .server-stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-card {
  padding: 1.5rem;
  border-radius: 1.5rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  line-height: 1;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat-label {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 600;
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
}

@media (min-width: 768px) {
  .stat-value {
    font-size: 2rem;
  }
}

.online-count {
  color: #10b981;
  font-size: 2.5rem;
}

.separator {
  color: rgba(255, 255, 255, 0.3);
  font-weight: 300;
}

.max-count {
  color: rgba(255, 255, 255, 0.5);
  font-size: 1.25rem;
}

/* 进度条 */
.stat-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-top: 0.5rem;
}

.stat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #6366f1);
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* TPS 指示器 */
.tps-indicator {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-top: 0.5rem;
}

.tps-bar {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.tps-bar.text-emerald-400 {
  background: #10b981;
  box-shadow: 0 0 12px rgba(16, 185, 129, 0.5);
}

.tps-bar.text-amber-400 {
  background: #f59e0b;
  box-shadow: 0 0 12px rgba(245, 158, 11, 0.5);
}

.tps-bar.text-rose-400 {
  background: #f43f5e;
  box-shadow: 0 0 12px rgba(244, 63, 94, 0.5);
}

/* 心跳动画 */
.heartbeat-animation {
  position: relative;
  height: 20px;
  margin-top: 0.5rem;
}

.heartbeat-pulse {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #ef4444;
  border-radius: 50%;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  opacity: 0;
}

.heartbeat-animation.active .heartbeat-pulse {
  background: #10b981;
  animation: heartbeat 1.5s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
  50% {
    opacity: 0.5;
    transform: translateY(-50%) scale(1.2);
  }
}

/* 状态文字 */
.status-online-text {
  color: #10b981;
  font-weight: 600;
}

.status-offline-text {
  color: #ef4444;
  font-weight: 600;
}

/* 时间值 */
.time-value {
  font-size: 1rem;
  font-weight: 500;
}

/* 自动刷新提示 */
.auto-refresh {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.4);
}

.refresh-icon {
  font-size: 0.875rem;
}

.refresh-text {
  font-weight: 300;
}

/* Footer */
.footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 2rem 1.5rem;
  margin-top: 4rem;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

@media (min-width: 768px) {
  .footer-content {
    flex-direction: row;
    justify-content: space-between;
  }
}

.footer-text {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.4);
}

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-link {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.4);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.8);
}

/* 颜色类 */
.text-emerald-400 {
  color: #10b981;
}

.text-amber-400 {
  color: #f59e0b;
}

.text-rose-400 {
  color: #f43f5e;
}

.text-gray-400 {
  color: rgba(255, 255, 255, 0.5);
}
</style>
