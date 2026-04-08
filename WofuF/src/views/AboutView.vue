<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'

const { translate } = useLocale()

// 团队成员
const teamMembers = ref([
  { 
    name: 'YaeSaraki', 
    role: translate('app', 'about.team.owner'), 
    description: translate('app', 'about.team.ownerDesc'),
    icon: 'crown'
  },
  { 
    name: 'Qing_Chen', 
    role: translate('app', 'about.team.developer'), 
    description: translate('app', 'about.team.developerDesc'),
    icon: 'code'
  }
])

// 服务器特点
const features = ref([
  {
    title: translate('app', 'about.features.gameplay'),
    description: translate('app', 'about.features.gameplayDesc'),
    icon: 'gamepad'
  },
  {
    title: translate('app', 'about.features.performance'),
    description: translate('app', 'about.features.performanceDesc'),
    icon: 'zap'
  },
  {
    title: translate('app', 'about.features.security'),
    description: translate('app', 'about.features.securityDesc'),
    icon: 'shield'
  },
  {
    title: translate('app', 'about.features.community'),
    description: translate('app', 'about.features.communityDesc'),
    icon: 'users'
  }
])

// 服务器统计
const stats = ref([
  { label: translate('app', 'about.stats.version'), value: '1.21.8', icon: 'box' },
  { label: translate('app', 'about.stats.core'), value: 'Leaves', icon: 'server' },
  { label: translate('app', 'about.stats.uptime'), value: '24/7', icon: 'clock' },
  { label: translate('app', 'about.stats.founded'), value: '2024', icon: 'calendar' }
])

// 动画效果
const isVisible = ref(false)
const textVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
  
  setTimeout(() => {
    textVisible.value = true
  }, 300)
})
</script>

<template>
  <div class="page-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <router-link to="/" class="logo">WofuF</router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/forum" class="nav-link">论坛</router-link>
          <router-link to="/about" class="nav-link active">关于</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-gradient"></div>
      
      <div class="hero-content" :class="{ visible: textVisible }">
        <div class="badge liquid-glass">
          <div class="badge-icon global-icon global-icon-sm">
            <svg viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </div>
          <span>关于我们</span>
        </div>
        <h1 class="hero-title">
          <span class="title-line">WofuF</span>
          <span class="title-line italic">Minecraft Server</span>
        </h1>
        <p class="hero-subtitle">
          {{ translate('app', 'about.heroSubtitle') }}
        </p>
      </div>

      <div class="hero-fade"></div>
    </section>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 服务器介绍 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">
            <div class="badge-icon global-icon global-icon-sm">
              <svg viewBox="0 0 24 24">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <span>简介</span>
          </div>
          <h2 class="section-title">{{ translate('app', 'about.intro') }}</h2>
        </div>
        <div class="glass-card intro-content">
          <p>{{ translate('app', 'about.introText1') }}</p>
          <p>{{ translate('app', 'about.introText2') }}</p>
          <p>{{ translate('app', 'about.introText3') }}</p>
        </div>
      </section>

      <!-- 服务器特点 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">
            <div class="badge-icon global-icon global-icon-sm">
              <svg viewBox="0 0 24 24">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </div>
            <span>特色</span>
          </div>
          <h2 class="section-title">{{ translate('app', 'about.features.title') }}</h2>
        </div>
        <div class="features-grid">
          <div v-for="(feature, index) in features" :key="index" class="feature-card liquid-glass">
            <div class="feature-icon-wrapper liquid-glass-strong">
              <div class="feature-icon global-icon global-icon-lg">
                <!-- Gamepad -->
                <svg v-if="feature.icon === 'gamepad'" viewBox="0 0 24 24">
                  <line x1="6" y1="12" x2="10" y2="12"/>
                  <line x1="8" y1="10" x2="8" y2="14"/>
                  <line x1="15" y1="13" x2="15.01" y2="13"/>
                  <line x1="18" y1="11" x2="18.01" y2="11"/>
                  <rect x="2" y="6" width="20" height="12" rx="2"/>
                </svg>
                <!-- Zap -->
                <svg v-else-if="feature.icon === 'zap'" viewBox="0 0 24 24">
                  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                </svg>
                <!-- Shield -->
                <svg v-else-if="feature.icon === 'shield'" viewBox="0 0 24 24">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
                <!-- Users -->
                <svg v-else-if="feature.icon === 'users'" viewBox="0 0 24 24">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </section>

      <!-- 服务器统计 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">
            <div class="badge-icon global-icon global-icon-sm">
              <svg viewBox="0 0 24 24">
                <line x1="18" y1="20" x2="18" y2="10"/>
                <line x1="12" y1="20" x2="12" y2="4"/>
                <line x1="6" y1="20" x2="6" y2="14"/>
              </svg>
            </div>
            <span>数据</span>
          </div>
          <h2 class="section-title">{{ translate('app', 'about.stats.title') }}</h2>
        </div>
        <div class="stats-grid">
          <div v-for="(stat, index) in stats" :key="index" class="stat-card liquid-glass">
            <div class="stat-icon-wrapper liquid-glass-strong">
              <div class="stat-icon global-icon global-icon-lg">
                <!-- Box -->
                <svg v-if="stat.icon === 'box'" viewBox="0 0 24 24">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                  <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                  <line x1="12" y1="22.08" x2="12" y2="12"/>
                </svg>
                <!-- Server -->
                <svg v-else-if="stat.icon === 'server'" viewBox="0 0 24 24">
                  <rect x="2" y="2" width="20" height="8" rx="2" ry="2"/>
                  <rect x="2" y="14" width="20" height="8" rx="2" ry="2"/>
                  <line x1="6" y1="6" x2="6.01" y2="6"/>
                  <line x1="6" y1="18" x2="6.01" y2="18"/>
                </svg>
                <!-- Clock -->
                <svg v-else-if="stat.icon === 'clock'" viewBox="0 0 24 24">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                <!-- Calendar -->
                <svg v-else-if="stat.icon === 'calendar'" viewBox="0 0 24 24">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
            </div>
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <!-- 团队成员 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">
            <div class="badge-icon global-icon global-icon-sm">
              <svg viewBox="0 0 24 24">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <span>团队</span>
          </div>
          <h2 class="section-title">{{ translate('app', 'about.team.title') }}</h2>
        </div>
        <div class="team-grid">
          <div v-for="(member, index) in teamMembers" :key="index" class="team-card liquid-glass">
            <div class="member-icon-wrapper liquid-glass-strong">
              <div class="member-icon global-icon global-icon-xl">
                <!-- Crown -->
                <svg v-if="member.icon === 'crown'" viewBox="0 0 24 24">
                  <path d="M2 4l3 12h14l3-12-6 7-4-7-4 7-6-7zm3 16h14"/>
                </svg>
                <!-- Code -->
                <svg v-else-if="member.icon === 'code'" viewBox="0 0 24 24">
                  <polyline points="16 18 22 12 16 6"/>
                  <polyline points="8 6 2 12 8 18"/>
                </svg>
              </div>
            </div>
            <div class="member-info">
              <h3>{{ member.name }}</h3>
              <p class="member-role">{{ member.role }}</p>
              <p class="member-description">{{ member.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 联系我们 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">
            <div class="badge-icon global-icon global-icon-sm">
              <svg viewBox="0 0 24 24">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
            </div>
            <span>联系</span>
          </div>
          <h2 class="section-title">{{ translate('app', 'about.contact.title') }}</h2>
        </div>
        <div class="glass-card contact-content">
          <p>{{ translate('app', 'about.contact.description') }}</p>
          <div class="contact-links">
            <a href="mailto:contact@wofuf.com" class="contact-link liquid-glass-strong">
              <div class="contact-icon global-icon global-icon-md">
                <svg viewBox="0 0 24 24">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
              </div>
              <span>{{ translate('app', 'about.contact.email') }}</span>
            </a>
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
@import '@S/styles/global.css';

/* 页面容器 */
.page-container {
  min-height: 100vh;
  background: hsl(var(--background));
  color: var(--text-primary);
  font-family: var(--font-body);
  overflow-x: hidden;
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
  padding: 0.875rem 2rem;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.08), 0 8px 32px rgba(0, 0, 0, 0.2);
}

.logo {
  font-size: 1.625rem;
  font-weight: 600;
  font-family: var(--font-heading);
  font-style: italic;
  color: var(--text-primary);
  text-decoration: none;
}

.nav-links {
  display: none;
  gap: 0.5rem;
  padding: 0.25rem;
  border-radius: var(--radius-full);
}

@media (min-width: 768px) {
  .nav-links {
    display: flex;
  }
}

.nav-link {
  padding: 0.625rem 1.25rem;
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--transition-base) ease;
}

.nav-link:hover,
.nav-link.active {
  color: var(--text-primary);
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
    rgba(99, 102, 241, 0.12) 0%,
    transparent 50%
  );
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 900px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 0.875rem;
  color: var(--text-primary);
}

.badge-icon {
  opacity: 0.9;
  color: var(--primary-color);
}

.hero-title {
  font-family: var(--font-heading);
  font-size: 4rem;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  font-weight: 400;
  color: var(--text-primary);
  letter-spacing: -0.03em;
}

@media (min-width: 768px) {
  .hero-title {
    font-size: 5.5rem;
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
  font-size: 1.25rem;
  font-weight: 400;
  color: var(--text-secondary);
  line-height: 1.7;
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

.section:nth-child(2) { transition-delay: 0.1s; }
.section:nth-child(3) { transition-delay: 0.2s; }
.section:nth-child(4) { transition-delay: 0.3s; }
.section:nth-child(5) { transition-delay: 0.4s; }

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-family: var(--font-heading);
  font-size: 2.25rem;
  font-weight: 400;
  font-style: italic;
  letter-spacing: -0.01em;
  color: var(--text-primary);
}

@media (min-width: 768px) {
  .section-title {
    font-size: 2.75rem;
  }
}

/* 玻璃卡片 */
.glass-card {
  padding: 2.5rem;
  border-radius: var(--radius-2xl);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.08), 0 8px 32px rgba(0, 0, 0, 0.2);
}

/* 介绍内容 */
.intro-content {
  line-height: 1.9;
}

.intro-content p {
  margin-bottom: 1.75rem;
  font-size: 1.0625rem;
  color: var(--text-secondary);
}

.intro-content p:last-child {
  margin-bottom: 0;
}

/* 特点网格 */
.features-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.feature-card {
  padding: 2.5rem;
  border-radius: var(--radius-2xl);
  transition: all var(--transition-base) ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: var(--radius-xl);
  margin-bottom: 1.75rem;
}

.feature-icon {
  color: var(--primary-color);
}

.feature-card h3 {
  font-size: 1.375rem;
  font-weight: 700;
  margin-bottom: 0.875rem;
  color: var(--text-primary);
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 1rem;
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-card {
  padding: 2rem;
  border-radius: var(--radius-2xl);
  text-align: center;
  transition: all var(--transition-base) ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  margin-bottom: 1.25rem;
}

.stat-icon {
  color: var(--primary-color);
}

.stat-value {
  display: block;
  font-size: 2.25rem;
  font-weight: 700;
  margin-bottom: 0.625rem;
  color: var(--text-primary);
  font-family: var(--font-heading);
  font-style: italic;
}

.stat-label {
  font-size: 0.9375rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* 团队网格 */
.team-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .team-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.team-card {
  padding: 2.5rem;
  border-radius: var(--radius-2xl);
  display: flex;
  align-items: center;
  gap: 2rem;
  transition: all var(--transition-base) ease;
}

.team-card:hover {
  transform: translateY(-4px);
}

.member-icon-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xl);
}

.member-icon {
  color: var(--primary-color);
}

.member-info {
  flex: 1;
}

.member-info h3 {
  font-size: 1.625rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.member-role {
  font-size: 0.9375rem;
  color: var(--text-tertiary);
  margin-bottom: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 600;
}

.member-description {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
}

/* 联系区域 */
.contact-content {
  text-align: center;
}

.contact-content p {
  margin-bottom: 2rem;
  color: var(--text-secondary);
  font-size: 1.0625rem;
  line-height: 1.7;
}

.contact-links {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.contact-link {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2.25rem;
  border-radius: var(--radius-full);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9375rem;
  transition: all var(--transition-base) ease;
  color: var(--text-primary);
}

.contact-link:hover {
  transform: translateY(-2px);
}

.contact-icon {
  opacity: 0.9;
}

/* Footer */
.footer {
  border-top: 1px solid rgba(255, 255, 255, 0.12);
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
  font-size: 0.875rem;
  color: var(--text-muted);
}

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-link {
  font-size: 0.875rem;
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition-base) ease;
}

.footer-link:hover {
  color: var(--text-secondary);
}
</style>
