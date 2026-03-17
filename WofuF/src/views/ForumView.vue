<script lang="ts" setup>
import PostList from '@M/forum/components/postList/PostList.vue'
import ForumSidebar from '@M/forum/components/sidebar/ForumSidebar.vue'
import { translate } from '@S/services/i18n'
import PageBackground from '@S/components/PageBackground.vue'
import { authService } from '@M/auth/services/AuthService.ts'
import { computed } from 'vue'
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
</script>

<template>
  <PageBackground variant="default" :show-pattern="true">
    <!-- 页面头部 -->
    <header class="bf-forum-header">
      <div class="bf-header-content">
        <div class="bf-header-text">
          <h1 class="bf-forum-title">
            <span class="bf-title-gradient">{{ translate('forum', 'forumTitle') }}</span>
          </h1>
          <p class="bf-forum-subtitle">{{ translate('forum', 'forumSubtitle') }}</p>
        </div>
        <button class="bf-create-btn" @click="goToCreatePost">
          <svg class="bf-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          <span>{{ isLoggedIn ? translate('forum', 'create_post') : translate('forum', 'loginToPost') }}</span>
        </button>
      </div>
    </header>

    <!-- 主要内容区域 - 两栏布局 -->
    <div class="bf-forum-layout">
      <main class="bf-forum-main">
        <PostList />
      </main>
      <ForumSidebar />
    </div>
  </PageBackground>
</template>

<style scoped>
/* 头部 - 亮色模式默认 */
.bf-forum-header {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  padding: 3rem 1rem;
  position: relative;
  overflow: hidden;
}

.bf-forum-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.1) 100%);
  pointer-events: none;
}

/* 暗色模式 - 更深的渐变和更强的对比 */
:global(.dark) .bf-forum-header {
  background: linear-gradient(135deg, #E55A25 0%, #E88A1C 30%, #FF6B35 60%, #FF9F1C 100%);
}

:global(.dark) .bf-forum-header::before {
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.2) 100%);
}

.bf-header-content {
  max-width: 1200px;
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
}

.bf-forum-main {
  min-width: 0;
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
