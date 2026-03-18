<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import { useTheme } from '@S/composables/useTheme.ts'

const { translate } = useLocale()
const { isDark } = useTheme()

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
  { label: '总玩家数', value: '1,245' },
  { label: '在线时间', value: '24/7' },
  { label: '服务器版本', value: '1.21.3' },
  { label: '创建时间', value: '2024-01-01' }
])

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
  <div class="about-page">
    <!-- 英雄区域 -->
    <section class="hero-section" :class="{ 'visible': isVisible }">
      <div class="hero-content">
        <h1>{{ translate('app', 'nav.about') }}</h1>
        <p>{{ translate('app', 'about.description') }}</p>
      </div>
    </section>

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
              <span>联系我们</span>
            </a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 80vh;
  padding: 2rem 1rem;
  max-width: 1000px;
  margin: 0 auto;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 3.5rem 2rem;
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

.hero-content h1 {
  font-size: 2.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-content p {
  font-size: 1.2rem;
  max-width: 800px;
  margin: 0 auto;
  color: rgba(0, 0, 0, 0.7);
}

html.dark .hero-content p {
  color: rgba(255, 255, 255, 0.7);
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
}

.intro-content {
  line-height: 1.6;
  color: rgba(0, 0, 0, 0.8);
}

html.dark .intro-content {
  color: rgba(255, 255, 255, 0.8);
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
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  padding: 1.75rem;
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
  transition: all 0.3s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shimmer 3s infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover::before {
  opacity: 1;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
  }
}

html.dark .feature-card {
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

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

html.dark .feature-card:hover {
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.25);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.feature-card p {
  color: rgba(0, 0, 0, 0.7);
  line-height: 1.5;
}

html.dark .feature-card p {
  color: rgba(255, 255, 255, 0.7);
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
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  padding: 1.75rem;
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
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shimmer 3s infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

html.dark .stat-card {
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

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

html.dark .stat-card:hover {
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.25);
}

.stat-value {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 1rem;
  color: rgba(0, 0, 0, 0.7);
}

html.dark .stat-label {
  color: rgba(255, 255, 255, 0.7);
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
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
}

.team-card {
  padding: 1.75rem;
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
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.team-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shimmer 3s infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.team-card:hover::before {
  opacity: 1;
}

html.dark .team-card {
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

.team-card:hover {
  transform: translateY(-5px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

html.dark .team-card:hover {
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.25);
}

.member-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 1rem;
  border: 3px solid rgba(67, 97, 238, 0.3);
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
}

.team-card p {
  color: rgba(0, 0, 0, 0.7);
  font-size: 0.875rem;
}

html.dark .team-card p {
  color: rgba(255, 255, 255, 0.7);
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
}

.contact-content {
  text-align: center;
}

.contact-content p {
  margin-bottom: 2rem;
  color: rgba(0, 0, 0, 0.7);
  font-size: 1.1rem;
}

html.dark .contact-content p {
  color: rgba(255, 255, 255, 0.7);
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
  border-radius: 12px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  background: linear-gradient(
    135deg,
    rgba(67, 97, 238, 0.1) 0%,
    rgba(58, 12, 163, 0.1) 100%
  );
  color: #4361ee;
  border-color: rgba(67, 97, 238, 0.3);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.1);
}

.contact-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(67, 97, 238, 0.2);
  background: linear-gradient(
    135deg,
    rgba(67, 97, 238, 0.15) 0%,
    rgba(58, 12, 163, 0.15) 100%
  );
}

.contact-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.contact-link svg {
  width: 20px;
  height: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-page {
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
    max-width: 300px;
    justify-content: center;
  }
}
</style>
