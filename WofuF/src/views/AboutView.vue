<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'

const { translate } = useLocale()

// 团队成员
const teamMembers = ref([
  { 
    name: 'YaeSaraki', 
    role: translate('app', 'about.team.owner'), 
    avatar: '👨‍💻',
    description: translate('app', 'about.team.ownerDesc')
  },
  { 
    name: 'Qing_Chen', 
    role: translate('app', 'about.team.developer'), 
    avatar: '👩‍💻',
    description: translate('app', 'about.team.developerDesc')
  }
])

// 服务器特点
const features = ref([
  {
    icon: '🎮',
    title: translate('app', 'about.features.gameplay'),
    description: translate('app', 'about.features.gameplayDesc')
  },
  {
    icon: '⚡',
    title: translate('app', 'about.features.performance'),
    description: translate('app', 'about.features.performanceDesc')
  },
  {
    icon: '🛡️',
    title: translate('app', 'about.features.security'),
    description: translate('app', 'about.features.securityDesc')
  },
  {
    icon: '🌍',
    title: translate('app', 'about.features.community'),
    description: translate('app', 'about.features.communityDesc')
  }
])

// 服务器统计
const stats = ref([
  { label: translate('app', 'about.stats.version'), value: '1.21.8', icon: '📦' },
  { label: translate('app', 'about.stats.core'), value: 'Leaves', icon: '🍃' },
  { label: translate('app', 'about.stats.uptime'), value: '24/7', icon: '⏰' },
  { label: translate('app', 'about.stats.founded'), value: '2024', icon: '🎉' }
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
        <div class="badge">关于我们</div>
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
          <div class="badge liquid-glass">简介</div>
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
          <div class="badge liquid-glass">特色</div>
          <h2 class="section-title">{{ translate('app', 'about.features.title') }}</h2>
        </div>
        <div class="features-grid">
          <div v-for="(feature, index) in features" :key="index" class="feature-card liquid-glass">
            <div class="feature-icon">{{ feature.icon }}</div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </section>

      <!-- 服务器统计 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">数据</div>
          <h2 class="section-title">{{ translate('app', 'about.stats.title') }}</h2>
        </div>
        <div class="stats-grid">
          <div v-for="(stat, index) in stats" :key="index" class="stat-card liquid-glass">
            <div class="stat-icon">{{ stat.icon }}</div>
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <!-- 团队成员 -->
      <section class="section" :class="{ visible: isVisible }">
        <div class="section-header">
          <div class="badge liquid-glass">团队</div>
          <h2 class="section-title">{{ translate('app', 'about.team.title') }}</h2>
        </div>
        <div class="team-grid">
          <div v-for="(member, index) in teamMembers" :key="index" class="team-card liquid-glass">
            <div class="member-avatar">{{ member.avatar }}</div>
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
          <div class="badge liquid-glass">联系</div>
          <h2 class="section-title">{{ translate('app', 'about.contact.title') }}</h2>
        </div>
        <div class="glass-card contact-content">
          <p>{{ translate('app', 'about.contact.description') }}</p>
          <div class="contact-links">
            <a href="mailto:contact@wofuf.com" class="contact-link liquid-glass-strong">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
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
  color: hsl(var(--foreground));
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
  padding: 0.75rem 2rem;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.06), 0 8px 32px rgba(0, 0, 0, 0.15);
}

.logo {
  font-size: 1.5rem;
  font-weight: 600;
  font-family: var(--font-heading);
  font-style: italic;
  color: white;
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
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--transition-base) ease;
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

.badge {
  display: inline-block;
  padding: 0.25rem 0.875rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.06), 0 8px 32px rgba(0, 0, 0, 0.15);
  color: white;
}

.hero-title {
  font-family: var(--font-heading);
  font-size: 3.5rem;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  font-weight: 400;
  color: white;
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

.section:nth-child(2) { transition-delay: 0.1s; }
.section:nth-child(3) { transition-delay: 0.2s; }
.section:nth-child(4) { transition-delay: 0.3s; }
.section:nth-child(5) { transition-delay: 0.4s; }

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-family: var(--font-heading);
  font-size: 2rem;
  font-weight: 400;
  font-style: italic;
  letter-spacing: -0.01em;
  color: white;
}

@media (min-width: 768px) {
  .section-title {
    font-size: 2.5rem;
  }
}

/* 玻璃卡片 */
.glass-card {
  padding: 2rem;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.06), 0 8px 32px rgba(0, 0, 0, 0.15);
}

/* 介绍内容 */
.intro-content {
  line-height: 1.8;
}

.intro-content p {
  margin-bottom: 1.5rem;
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
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
  padding: 2rem;
  border-radius: var(--radius-xl);
  transition: all var(--transition-base) ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  line-height: 1;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: white;
}

.feature-card p {
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  font-size: 0.9375rem;
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
  padding: 1.5rem;
  border-radius: var(--radius-xl);
  text-align: center;
  transition: all var(--transition-base) ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  font-size: 2rem;
  margin-bottom: 0.75rem;
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: white;
  font-family: var(--font-heading);
  font-style: italic;
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.5);
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
  padding: 2rem;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: all var(--transition-base) ease;
}

.team-card:hover {
  transform: translateY(-4px);
}

.member-avatar {
  font-size: 4rem;
  flex-shrink: 0;
  line-height: 1;
}

.member-info {
  flex: 1;
}

.member-info h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: white;
}

.member-role {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.member-description {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9375rem;
  line-height: 1.5;
}

/* 联系区域 */
.contact-content {
  text-align: center;
}

.contact-content p {
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
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
  padding: 1rem 2rem;
  border-radius: var(--radius-full);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-base) ease;
  color: white;
}

.contact-link:hover {
  transform: translateY(-2px);
}

.contact-link svg {
  width: 20px;
  height: 20px;
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
  transition: color var(--transition-base) ease;
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.8);
}
</style>
