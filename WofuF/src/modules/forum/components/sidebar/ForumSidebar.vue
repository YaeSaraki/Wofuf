/** * 论坛首页侧边栏组件 * 显示热门帖子、论坛统计、公告等信息 */
<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { translate } from '@S/services/i18n'

const router = useRouter()

// 热门帖子
const hotPosts = ref<PostDto[]>([])
const isLoadingHot = ref(false)

// 论坛统计
const stats = ref({
  totalPosts: 0,
  totalMembers: 0,
  onlineMembers: 0,
})

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 获取热门帖子（修复版：page=0, size=5）
async function fetchHotPosts() {
  isLoadingHot.value = true
  // 🔥 修复：第1页(0)、取5条、不限制分类
  const result = await forumService.getPopularPosts(0, 5)
  if (result.isSuccess) {
    hotPosts.value = result.getValue().posts
  }
  isLoadingHot.value = false
}

// 跳转到帖子
function goToPost(slug: string) {
  router.push(`/forum/posts/${slug}`)
}

// 跳转到发帖
function goToCreatePost() {
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  router.push('/forum/create')
}

onMounted(() => {
  fetchHotPosts()
})
</script>

<template>
  <aside class="bf-sidebar">
    <!-- 发帖按钮 -->
    <div class="bf-sidebar__section bf-sidebar__cta">
      <button class="bf-cta-btn" @click="goToCreatePost">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        <span>{{
          isLoggedIn ? translate('forum', 'create_post') : translate('forum', 'loginToPost')
        }}</span>
      </button>
    </div>

    <!-- 公告区域 -->
    <div class="bf-sidebar__section bf-sidebar__notice">
      <div class="bf-section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z"
          />
        </svg>
        <span class="bf-section-title">公告</span>
      </div>
      <div class="bf-notice-content">
        <p class="bf-notice-text">欢迎来到 Wofuf 社区！请遵守社区规则，友善交流。</p>
        <a href="#" class="bf-notice-link">查看社区规则 →</a>
      </div>
    </div>

    <!-- 热门帖子 -->
    <div class="bf-sidebar__section">
      <div class="bf-section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343a7.975 7.975 0 010 11.314z"
          />
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M9.879 16.121A3 3 0 1012.015 11L11 14H9c0 .768.293 1.536.879 2.121z"
          />
        </svg>
        <span class="bf-section-title">热门帖子</span>
      </div>
      <div v-if="isLoadingHot" class="bf-sidebar-loading">
        <div class="bf-loading-spinner"></div>
      </div>
      <ul v-else-if="hotPosts.length > 0" class="bf-hot-list">
        <li
          v-for="(post, index) in hotPosts"
          :key="post.slug"
          class="bf-hot-item"
          @click="goToPost(post.slug)"
        >
          <span class="bf-hot-rank" :class="{ 'bf-hot-rank--top': index < 3 }">{{
            index + 1
          }}</span>
          <span class="bf-hot-title">{{ post.title }}</span>
        </li>
      </ul>
      <div v-else class="bf-sidebar-empty">暂无热门帖子</div>
    </div>

    <!-- 论坛统计 -->
    <div class="bf-sidebar__section bf-sidebar__stats">
      <div class="bf-section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
          />
        </svg>
        <span class="bf-section-title">社区统计</span>
      </div>
      <div class="bf-stats-grid">
        <div class="bf-stat-item">
          <span class="bf-stat-value">{{ stats.totalPosts }}</span>
          <span class="bf-stat-label">帖子</span>
        </div>
        <div class="bf-stat-item">
          <span class="bf-stat-value">{{ stats.totalMembers }}</span>
          <span class="bf-stat-label">成员</span>
        </div>
        <div class="bf-stat-item">
          <span class="bf-stat-value bf-stat-value--online">{{ stats.onlineMembers }}</span>
          <span class="bf-stat-label">在线</span>
        </div>
      </div>
    </div>

    <!-- 快捷链接 -->
    <div class="bf-sidebar__section">
      <div class="bf-section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
          />
        </svg>
        <span class="bf-section-title">快捷链接</span>
      </div>
      <ul class="bf-quick-links">
        <li>
          <a href="#" class="bf-quick-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            新手指南
          </a>
        </li>
        <li>
          <a href="#" class="bf-quick-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
              />
            </svg>
            服务器规则
          </a>
        </li>
        <li>
          <a href="#" class="bf-quick-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z"
              />
            </svg>
            联系管理员
          </a>
        </li>
      </ul>
    </div>
  </aside>
</template>

<style scoped>
/* === 侧边栏容器 === */
.bf-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
  position: sticky;
  top: calc(var(--bf-header-height, 64px) + var(--bf-space-lg, 24px));
  max-height: calc(100vh - var(--bf-header-height, 64px) - var(--bf-space-lg, 48px));
  overflow-y: auto;
}

/* === 区块通用样式 === */
.bf-sidebar__section {
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-md, 16px);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* === 发帖按钮 === */
.bf-sidebar__cta {
  padding: 0;
  background: transparent;
  border: none;
  backdrop-filter: none;
}

.bf-cta-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: 14px var(--bf-space-lg, 20px);
  background: var(--bf-btn-primary-bg);
  color: white;
  border: none;
  border-radius: var(--bf-btn-radius, 12px);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
  box-shadow: 0 4px 16px var(--bf-primary-glow);
}

.bf-cta-btn svg {
  width: 20px;
  height: 20px;
}

.bf-cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px var(--bf-primary-glow);
}

/* === 区块头部 === */
.bf-section-header {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  margin-bottom: var(--bf-space-md, 12px);
  padding-bottom: var(--bf-space-sm, 8px);
  border-bottom: 1px solid var(--bf-border-default);
}

.bf-section-header svg {
  width: 18px;
  height: 18px;
  color: var(--bf-primary);
}

.bf-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--bf-text-primary);
}

/* === 公告区域 === */
.bf-notice-content {
  padding: var(--bf-space-sm, 8px);
  background: var(--bf-fire-gradient-subtle);
  border-radius: var(--bf-input-radius, 10px);
  border: 1px solid var(--bf-border-accent);
}

.bf-notice-text {
  font-size: 13px;
  color: var(--bf-text-secondary);
  margin: 0 0 var(--bf-space-sm, 8px) 0;
  line-height: 1.5;
}

.bf-notice-link {
  font-size: 12px;
  color: var(--bf-primary);
  text-decoration: none;
  font-weight: 500;
}

.bf-notice-link:hover {
  text-decoration: underline;
}

/* === 热门列表 === */
.bf-hot-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.bf-hot-item {
  display: flex;
  align-items: flex-start;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px);
  margin: 0 -4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background var(--bf-transition-fast, 0.15s ease);
}

.bf-hot-item:hover {
  background: var(--bf-btn-secondary-bg);
}

.bf-hot-rank {
  min-width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: var(--bf-text-muted);
  background: var(--bf-input-bg);
  border-radius: 4px;
}

.bf-hot-rank--top {
  background: var(--bf-fire-gradient);
  color: white;
}

.bf-hot-title {
  font-size: 13px;
  color: var(--bf-text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--bf-transition-fast, 0.15s ease);
}

.bf-hot-item:hover .bf-hot-title {
  color: var(--bf-primary);
}

/* === 统计区块 === */
.bf-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--bf-space-sm, 8px);
}

.bf-stat-item {
  text-align: center;
  padding: var(--bf-space-sm, 8px);
  background: var(--bf-input-bg);
  border-radius: 8px;
}

.bf-stat-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--bf-text-primary);
}

.bf-stat-value--online {
  color: #10b981;
}

.bf-stat-label {
  display: block;
  font-size: 11px;
  color: var(--bf-text-muted);
  margin-top: 2px;
}

/* === 快捷链接 === */
.bf-quick-links {
  list-style: none;
  margin: 0;
  padding: 0;
}

.bf-quick-link {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px);
  margin: 0 -4px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--bf-text-secondary);
  text-decoration: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-quick-link svg {
  width: 16px;
  height: 16px;
  color: var(--bf-text-muted);
}

.bf-quick-link:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-primary);
}

.bf-quick-link:hover svg {
  color: var(--bf-primary);
}

/* === 加载状态 === */
.bf-sidebar-loading {
  display: flex;
  justify-content: center;
  padding: var(--bf-space-md, 16px);
}

.bf-loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--bf-border-default);
  border-top-color: var(--bf-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* === 空状态 === */
.bf-sidebar-empty {
  text-align: center;
  padding: var(--bf-space-md, 16px);
  font-size: 13px;
  color: var(--bf-text-muted);
}

/* === 响应式 === */
@media (max-width: 1024px) {
  .bf-sidebar {
    display: none;
  }
}
</style>
