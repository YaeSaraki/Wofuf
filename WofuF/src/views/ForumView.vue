<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import PostList from '@M/forum/components/postList/PostList.vue'
import ForumSidebar from '@M/forum/components/sidebar/ForumSidebar.vue'
import { translate } from '@S/services/i18n'
import { authService } from '@M/auth/services/AuthService.ts'
import router from '@S/infra/router'

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 跳转到发帖页
function goToCreatePost() {
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  router.push('/forum/create')
}

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
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-gradient"></div>

      <div class="hero-content" :class="{ visible: textVisible }">
        <div class="badge liquid-glass">{{ translate('forum', 'community') || '社区' }}</div>
        <h1 class="hero-title">
          <span class="title-line">{{ translate('forum', 'forumTitle') }}</span>
          <span class="title-line italic">Community</span>
        </h1>
        <p class="hero-subtitle">
          {{ translate('forum', 'forumSubtitle') }}
        </p>
        <button class="hero-button liquid-glass-strong" @click="goToCreatePost">
          <svg class="button-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          <span>{{ isLoggedIn ? translate('forum', 'create_post') : translate('forum', 'loginToPost') }}</span>
        </button>
      </div>

      <div class="hero-fade"></div>
    </section>

    <!-- 主要内容区域 -->
    <div class="main-content" :class="{ visible: isVisible }">
      <div class="forum-layout">
        <main class="forum-main">
          <PostList />
        </main>
        <aside class="forum-sidebar" :class="{ visible: isVisible }">
          <ForumSidebar />
        </aside>
      </div>
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
  padding-top: 5rem;
}

/* Hero Section */
.hero-section {
  position: relative;
  min-height: 500px;
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
  padding: 0.5rem 1.25rem;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
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
  margin-bottom: 2rem;
}

.hero-button {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  border-radius: var(--radius-full);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base) ease;
  color: white;
  border: none;
  outline: none;
}

.hero-button:hover {
  transform: translateY(-2px);
}

.button-icon {
  width: 20px;
  height: 20px;
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1.5rem 4rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.forum-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
}

@media (min-width: 1024px) {
  .forum-layout {
    grid-template-columns: 1fr 320px;
  }
}

.forum-main {
  min-width: 0;
}

.forum-sidebar {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.2s;
}

.forum-sidebar.visible {
  opacity: 1;
  transform: translateY(0);
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

/* Light mode */
:root:not(.dark) .hero-subtitle {
  color: rgba(0, 0, 0, 0.6);
}

:root:not(.dark) .hero-gradient {
  background: radial-gradient(
    circle at 50% 30%,
    rgba(99, 102, 241, 0.2) 0%,
    transparent 50%
  );
}

:root:not(.dark) .hero-fade {
  background: linear-gradient(to top, hsl(var(--background)), transparent);
}

:root:not(.dark) .footer {
  border-top-color: rgba(0, 0, 0, 0.1);
}

:root:not(.dark) .footer-text,
:root:not(.dark) .footer-link {
  color: rgba(0, 0, 0, 0.4);
}

:root:not(.dark) .footer-link:hover {
  color: rgba(0, 0, 0, 0.8);
}
</style>
