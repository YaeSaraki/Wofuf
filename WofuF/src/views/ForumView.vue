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
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <router-link to="/" class="logo">WofuF</router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/forum" class="nav-link active">论坛</router-link>
          <router-link to="/about" class="nav-link">关于</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-gradient"></div>
      
      <div class="hero-content" :class="{ visible: textVisible }">
        <div class="badge liquid-glass">社区</div>
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
  padding: 0.25rem 0.875rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
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
</style>
}

.bf-header-text {
  flex: 1;
}

.bf-forum-title {
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

.bf-forum-subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 暗色模式 - 副标题 */
:global(.dark) .bf-forum-subtitle {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 发帖按钮 - 亮色模式 */
.bf-create-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: var(--bf-btn-radius, 12px);
  color: var(--bf-primary, #FF6B35);
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.bf-create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  background: #FFFFFF;
}

/* 暗色模式 - 按钮样式调整 */
:global(.dark) .bf-create-btn {
  background: rgba(30, 30, 30, 0.95);
  color: var(--bf-primary, #FF8C5A);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 140, 90, 0.2);
}

:global(.dark) .bf-create-btn:hover {
  background: rgba(45, 45, 45, 0.98);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 140, 90, 0.3);
}

.bf-create-btn:active {
  transform: translateY(0);
}

.bf-btn-icon {
  width: 18px;
  height: 18px;
}

/* === 两栏布局 === */
.bf-forum-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--bf-space-lg, 24px) var(--bf-space-md, 16px);
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: var(--bf-space-lg, 24px);
  align-items: start;
  
  /* 淡入动画 */
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.bf-forum-layout.visible {
  opacity: 1;
  transform: translateY(0);
}

.bf-forum-main {
  min-width: 0;
}

.bf-forum-sidebar {
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.2s;
}

.bf-forum-sidebar.visible {
  opacity: 1;
  transform: translateX(0);
}

/* 响应式 */
@media (max-width: 1024px) {
  .bf-forum-layout {
    grid-template-columns: 1fr;
    max-width: 800px;
  }

  .bf-header-content {
    max-width: 800px;
  }

  .bf-forum-sidebar {
    display: none;
  }
}

@media (max-width: 640px) {
  .bf-forum-header {
    padding: 2rem 1rem;
  }

  .bf-forum-title {
    font-size: 1.75rem;
  }

  .bf-forum-subtitle {
    font-size: 0.875rem;
  }

  .bf-header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .bf-create-btn {
    width: 100%;
    justify-content: center;
  }

  .bf-forum-layout {
    padding: var(--bf-space-md, 16px) 12px;
  }
}
</style>
