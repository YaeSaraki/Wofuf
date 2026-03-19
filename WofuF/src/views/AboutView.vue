<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import PageBackground from '@S/components/PageBackground.vue'

const { translate } = useLocale()

// 团队成员
const teamMembers = ref([
  { name: 'Steve', role: 'Server Owner', avatar: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=Minecraft%20Steve%20character%20avatar&size=200x200' },
  { name: 'Alex', role: 'Developer', avatar: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=Minecraft%20Alex%20character%20avatar&size=200x200' },
  { name: 'Notch', role: 'Builder', avatar: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=Minecraft%20Notch%20character%20avatar&size=200x200' },
  { name: 'Herobrine', role: 'Moderator', avatar: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=Minecraft%20Herobrine%20character%20avatar&size=200x200' }
])

// 服务器特点
const features = ref([
  {
    icon: '🏠',
    title: translate('app', 'about.features.survival'),
    description: translate('app', 'about.features.survivalDesc')
  },
  {
    icon: '✨',
    title: translate('app', 'about.features.custom'),
    description: translate('app', 'about.features.customDesc')
  },
  {
    icon: '🏆',
    title: translate('app', 'about.features.events'),
    description: translate('app', 'about.features.eventsDesc')
  },
  {
    icon: '🤝',
    title: translate('app', 'about.features.community'),
    description: translate('app', 'about.features.communityDesc')
  },
  {
    icon: '🛡️',
    title: translate('app', 'about.features.antiCheat'),
    description: translate('app', 'about.features.antiCheatDesc')
  },
  {
    icon: '🌍',
    title: translate('app', 'about.features.multiverse'),
    description: translate('app', 'about.features.multiverseDesc')
  }
])

// 服务器统计
const stats = ref([
  { label: translate('app', 'about.stats.totalPlayers'), value: '1,245' },
  { label: translate('app', 'about.stats.uptime'), value: '24/7' },
  { label: translate('app', 'about.stats.version'), value: '1.21.3' },
  { label: translate('app', 'about.stats.founded'), value: '2024-01-01' }
])

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
    <header class="bf-page-header">
      <div class="bf-header-content">
        <div class="bf-header-text">
          <h1 class="bf-page-title">
            <span class="bf-title-gradient">{{ translate('app', 'nav.about') }}</span>
          </h1>
          <p class="bf-page-subtitle">{{ translate('app', 'about.description') }}</p>
        </div>
      </div>
    </header>

    <div class="about-page">
      <!-- 服务器介绍 -->
      <section class="intro-section" :class="{ 'visible': isVisible }">
        <div class="container">
          <h2>{{ translate('app', 'about.serverIntro') }}</h2>
          <div class="intro-content">
            <p>{{ translate('app', 'about.serverDesc1') }}</p>
            <p>{{ translate('app', 'about.serverDesc2') }}</p>
            <p>{{ translate('app', 'about.serverDesc3') }}</p>
          </div>
        </div>
      </section>

      <!-- 服务器特点 -->
      <section class="features-section" :class="{ 'visible': isVisible }">
        <div class="container">
          <h2>{{ translate('app', 'about.features.title') }}</h2>
          <div class="features-grid">
            <div v-for="(feature, index) in features" :key="index" class="feature-card">
              <div class="feature-icon">{{ feature.icon }}</div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 服务器统计 -->
      <section class="stats-section" :class="{ 'visible': isVisible }">
        <div class="container">
          <h2>{{ translate('app', 'about.stats.title') }}</h2>
          <div class="stats-grid">
            <div v-for="(stat, index) in stats" :key="index" class="stat-card">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 团队成员 -->
      <section class="team-section" :class="{ 'visible': isVisible }">
        <div class="container">
          <h2>{{ translate('app', 'about.team.title') }}</h2>
          <div class="team-grid">
            <div v-for="(member, index) in teamMembers" :key="index" class="team-card">
              <div class="member-avatar">
                <img :src="member.avatar" :alt="member.name" />
              </div>
              <h3>{{ member.name }}</h3>
              <p>{{ member.role }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 联系我们 -->
      <section class="contact-section" :class="{ 'visible': isVisible }">
        <div class="container">
          <h2>{{ translate('app', 'about.contact.title') }}</h2>
          <div class="contact-content">
            <p>{{ translate('app', 'about.contact.description') }}</p>
            <div class="contact-links">
              <a href="mailto:contact@wofuf.com" class="contact-link email">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                  <polyline points="22,6 12,13 2,6" />
                </svg>
                <span>{{ translate('app', 'about.contact.email') }}</span>
              </a>
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

/* 页面内容 */
.about-page {
  min-height: 60vh;
  padding: 2rem 1rem;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 通用容器 */
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem 0;
}

/* 介绍区域 */
.intro-section {
  margin-bottom: 3rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.2s;
}

.intro-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.intro-section h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  text-align: center;
  color: var(--bf-text-primary, #1a1a2e);
}

.intro-content {
  line-height: 1.6;
  color: var(--bf-text-secondary, #4a4a68);
}

.intro-content p {
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
}

/* 特点区域 */
.features-section {
  margin-bottom: 3rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.3s;
}

.features-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.features-section h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  text-align: center;
  color: var(--bf-text-primary, #1a1a2e);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  padding: 1.75rem;
  border-radius: var(--bf-radius-lg, 16px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
  transition: all 0.3s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--bf-text-primary, #1a1a2e);
}

.feature-card p {
  color: var(--bf-text-secondary, #4a4a68);
  line-height: 1.5;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 3rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.4s;
}

.stats-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.stats-section h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  text-align: center;
  color: var(--bf-text-primary, #1a1a2e);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  padding: 1.75rem;
  border-radius: var(--bf-radius-lg, 16px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
}

.stat-value {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--bf-primary, #FF6B35);
}

.stat-label {
  font-size: 1rem;
  color: var(--bf-text-secondary, #4a4a68);
}

/* 团队区域 */
.team-section {
  margin-bottom: 3rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.5s;
}

.team-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.team-section h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  text-align: center;
  color: var(--bf-text-primary, #1a1a2e);
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
}

.team-card {
  padding: 1.75rem;
  border-radius: var(--bf-radius-lg, 16px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
  text-align: center;
  transition: all 0.3s ease;
}

.team-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
}

.member-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 1rem;
  border: 3px solid var(--bf-primary, #FF6B35);
  opacity: 0.3;
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--bf-text-primary, #1a1a2e);
}

.team-card p {
  color: var(--bf-text-secondary, #4a4a68);
  font-size: 0.875rem;
}

/* 联系区域 */
.contact-section {
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.6s;
}

.contact-section.visible {
  opacity: 1;
  transform: translateY(0);
}

.contact-section h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  text-align: center;
  color: var(--bf-text-primary, #1a1a2e);
}

.contact-content {
  text-align: center;
}

.contact-content p {
  margin-bottom: 2rem;
  color: var(--bf-text-secondary, #4a4a68);
  font-size: 1.1rem;
}

.contact-links {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.contact-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-radius: var(--bf-radius-md, 12px);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  background: var(--bf-surface, rgba(255, 255, 255, 0.8));
  color: var(--bf-primary, #FF6B35);
  border-color: var(--bf-border-subtle, rgba(0, 0, 0, 0.06));
  box-shadow: var(--bf-shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
}

.contact-link:hover {
  transform: translateY(-3px);
  box-shadow: var(--bf-shadow-md, 0 4px 20px rgba(0, 0, 0, 0.08));
}

.contact-link svg {
  width: 20px;
  height: 20px;
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

  .about-page {
    padding: 1rem 0.5rem;
  }

  .container {
    padding: 1.5rem 0;
  }

  .intro-section h2,
  .features-section h2,
  .stats-section h2,
  .team-section h2,
  .contact-section h2 {
    font-size: 1.75rem;
  }

  .features-grid,
  .stats-grid,
  .team-grid {
    grid-template-columns: 1fr;
  }

  .contact-links {
    flex-direction: column;
    align-items: center;
  }

  .contact-link {
    width: 90%;
  }
}
</style>
