<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import type { PermissionPoint } from '@M/forum/admin/dtos/Admin.ts'
import { translate } from '@S/services/i18n'
import router from '@S/infra/router'
import PostsManagement from '@M/forum/admin/components/PostsManagement.vue'

const route = useRoute()
const internalRouter = useRouter()

// 当前选中的标签 - 从 URL 同步
const activeTab = ref<'overview' | 'posts' | 'comments' | 'members'>(
  (route.query.tab as any) || 'overview'
)

// 监听 activeTab 变化，同步到 URL
watch(activeTab, (newTab) => {
  internalRouter.replace({ query: { ...route.query, tab: newTab } })
})

// 监听浏览器前进/后退，响应 URL 变化
watch(() => route.query.tab, (newTab) => {
  if (newTab && newTab !== activeTab.value) {
    activeTab.value = newTab as any
  }
})

// 权限状态
const hasAdminAccess = ref(false)
const isLoading = ref(true)

// 检查权限
async function checkPermissions() {
  isLoading.value = true
  hasAdminAccess.value = await adminService.hasAnyPermission([
    'ADMIN_ACCESS',
    'POST_PIN',
    'POST_FEATURE',
    'POST_HIDE',
    'POST_REVIEW',
    'COMMENT_DELETE_ANY',
    'COMMENT_VIEW_HIDDEN',
    'USER_BAN',
    'PERMISSION_GRANT'
  ], true)
  isLoading.value = false
}

// 动画效果
const isVisible = ref(false)

onMounted(async () => {
  await checkPermissions()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})

// 导航项
const navItems = [
  { key: 'overview', labelKey: 'admin.nav.overview', icon: 'chart' },
  { key: 'posts', labelKey: 'admin.nav.posts', icon: 'document' },
  { key: 'comments', labelKey: 'admin.nav.comments', icon: 'chat' },
  { key: 'members', labelKey: 'admin.nav.members', icon: 'users' },
]

// 权限标签颜色
const permissionColors: Record<PermissionPoint, string> = {
  POST_PIN: 'bf-perm-blue',
  POST_FEATURE: 'bf-perm-purple',
  POST_HIDE: 'bf-perm-red',
  POST_REVIEW: 'bf-perm-yellow',
  POST_DELETE_ANY: 'bf-perm-red-dark',
  COMMENT_DELETE_ANY: 'bf-perm-orange',
  COMMENT_VIEW_HIDDEN: 'bf-perm-amber',
  CATEGORY_MANAGE: 'bf-perm-cyan',
  USER_BAN: 'bf-perm-rose',
  USER_VIEW_BANNED: 'bf-perm-pink',
  ADMIN_ACCESS: 'bf-perm-emerald',
  PERMISSION_GRANT: 'bf-perm-indigo',
}

// 权限标签名称
const permissionNames: Record<PermissionPoint, string> = {
  POST_PIN: translate('forum', 'admin.perm.pin'),
  POST_FEATURE: translate('forum', 'admin.perm.feature'),
  POST_HIDE: translate('forum', 'admin.perm.hide'),
  POST_REVIEW: translate('forum', 'admin.perm.review'),
  POST_DELETE_ANY: translate('forum', 'admin.perm.deletePost'),
  COMMENT_DELETE_ANY: translate('forum', 'admin.perm.deleteComment'),
  COMMENT_VIEW_HIDDEN: translate('forum', 'admin.perm.viewHidden'),
  CATEGORY_MANAGE: translate('forum', 'admin.perm.manageCategory'),
  USER_BAN: translate('forum', 'admin.perm.banUser'),
  USER_VIEW_BANNED: translate('forum', 'admin.perm.viewBanned'),
  ADMIN_ACCESS: translate('forum', 'admin.perm.adminAccess'),
  PERMISSION_GRANT: translate('forum', 'admin.perm.grantPermission'),
}
</script>

<template>
  <div class="bf-admin-page">
    <!-- 加载中 -->
    <div v-if="isLoading" class="bf-loading-state">
      <div class="bf-loading-spinner"></div>
      <p class="bf-loading-text">{{ translate('forum', 'admin.verifying') }}</p>
    </div>

    <!-- 无权限 -->
    <div v-else-if="!hasAdminAccess" class="bf-no-access">
      <div class="bf-no-access-card">
        <svg class="bf-no-access-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
        </svg>
        <h2 class="bf-no-access-title">{{ translate('forum', 'admin.accessDenied') }}</h2>
        <p class="bf-no-access-text">{{ translate('forum', 'admin.accessDeniedDesc') }}</p>
        <button class="bf-back-btn" @click="router.push('/forum')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          {{ translate('forum', 'back') }}
        </button>
      </div>
    </div>

    <!-- 管理后台主界面 -->
    <div v-else class="bf-admin-layout" :class="{ 'bf-visible': isVisible }">
      <!-- 移动端标签栏 -->
      <div class="bf-mobile-tabs">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="bf-mobile-tab"
          :class="{ 'bf-active': activeTab === item.key }"
          @click="activeTab = item.key as any"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <template v-if="item.icon === 'chart'">
              <path d="M3 3v18h18"/>
              <path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/>
            </template>
            <template v-else-if="item.icon === 'document'">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </template>
            <template v-else-if="item.icon === 'chat'">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </template>
            <template v-else-if="item.icon === 'users'">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </template>
          </svg>
          <span>{{ translate('forum', item.labelKey) }}</span>
        </button>
      </div>

      <!-- 侧边栏 -->
      <aside class="bf-admin-sidebar">
        <div class="bf-sidebar-header">
          <div class="bf-sidebar-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </div>
          <div class="bf-sidebar-title-group">
            <h2 class="bf-sidebar-title">{{ translate('forum', 'admin.title') }}</h2>
            <p class="bf-sidebar-subtitle">Administration</p>
          </div>
        </div>
        
        <nav class="bf-sidebar-nav">
          <button
            v-for="item in navItems"
            :key="item.key"
            class="bf-nav-item"
            :class="{ 'bf-active': activeTab === item.key }"
            @click="activeTab = item.key as any"
          >
            <svg class="bf-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <template v-if="item.icon === 'chart'">
                <path d="M3 3v18h18"/>
                <path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/>
              </template>
              <template v-else-if="item.icon === 'document'">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </template>
              <template v-else-if="item.icon === 'chat'">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </template>
              <template v-else-if="item.icon === 'users'">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </template>
            </svg>
            <span class="bf-nav-label">{{ translate('forum', item.labelKey) }}</span>
          </button>
        </nav>

        <div class="bf-sidebar-footer">
          <button class="bf-nav-item bf-nav-back" @click="router.push('/forum')">
            <svg class="bf-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
            <span class="bf-nav-label">{{ translate('forum', 'admin.backToForum') }}</span>
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="bf-admin-main">
        <!-- 概览面板 -->
        <section v-if="activeTab === 'overview'" class="bf-content-section">
          <h3 class="bf-section-title">
            <span class="bf-text-gradient">{{ translate('forum', 'admin.overview') }}</span>
          </h3>
          <div class="bf-stats-grid">
            <div class="bf-stat-card">
              <div class="bf-stat-icon bf-stat-icon--blue">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="bf-stat-content">
                <span class="bf-stat-value">--</span>
                <span class="bf-stat-label">{{ translate('forum', 'admin.stats.pendingReview') }}</span>
              </div>
            </div>
            <div class="bf-stat-card">
              <div class="bf-stat-icon bf-stat-icon--red">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <div class="bf-stat-content">
                <span class="bf-stat-value">--</span>
                <span class="bf-stat-label">{{ translate('forum', 'admin.stats.hiddenComments') }}</span>
              </div>
            </div>
            <div class="bf-stat-card">
              <div class="bf-stat-icon bf-stat-icon--yellow">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
              </div>
              <div class="bf-stat-content">
                <span class="bf-stat-value">--</span>
                <span class="bf-stat-label">{{ translate('forum', 'admin.stats.bannedUsers') }}</span>
              </div>
            </div>
            <div class="bf-stat-card">
              <div class="bf-stat-icon bf-stat-icon--green">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
              <div class="bf-stat-content">
                <span class="bf-stat-value">--</span>
                <span class="bf-stat-label">{{ translate('forum', 'admin.stats.totalMembers') }}</span>
              </div>
            </div>
          </div>

          <div class="bf-info-section">
            <h4 class="bf-info-title">{{ translate('forum', 'admin.permissions') }}</h4>
            <div class="bf-permission-list">
              <span 
                v-for="(name, permission) in permissionNames" 
                :key="permission" 
                class="bf-permission-tag"
                :class="permissionColors[permission as PermissionPoint]"
              >
                {{ name }}
              </span>
            </div>
          </div>
        </section>

        <!-- 帖子管理 -->
        <section v-else-if="activeTab === 'posts'" class="bf-content-section bf-no-padding">
          <PostsManagement />
        </section>

        <!-- 评论管理 -->
        <section v-else-if="activeTab === 'comments'" class="bf-content-section">
          <h3 class="bf-section-title">
            <span class="bf-text-gradient">{{ translate('forum', 'admin.commentsManagement') }}</span>
          </h3>
          <div class="bf-coming-soon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <p>{{ translate('forum', 'admin.comingSoon') }}</p>
            <span>{{ translate('forum', 'admin.commentsComingSoonDesc') }}</span>
          </div>
        </section>

        <!-- 成员管理 -->
        <section v-else-if="activeTab === 'members'" class="bf-content-section">
          <h3 class="bf-section-title">
            <span class="bf-text-gradient">{{ translate('forum', 'admin.membersManagement') }}</span>
          </h3>
          <div class="bf-coming-soon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <p>{{ translate('forum', 'admin.comingSoon') }}</p>
            <span>{{ translate('forum', 'admin.membersComingSoonDesc') }}</span>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* === 页面容器 === */
.bf-admin-page {
  min-height: 100vh;
  background: var(--bf-page-bg, #0a0a0a);
  padding-top: 5rem;
}

/* === 文字渐变 === */
.bf-text-gradient {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #ff6b35 0%, #ff9f1c 50%, #ffbe0b 100%));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* === 加载状态 === */
.bf-loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 1.25rem;
}

.bf-loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-top-color: var(--bf-primary, #ff6b35);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.bf-loading-text {
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.9375rem;
}

/* === 无权限 === */
.bf-no-access {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 1.5rem;
}

.bf-no-access-card {
  text-align: center;
  padding: 3rem 2.5rem;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
  max-width: 380px;
  width: 100%;
}

.bf-no-access-icon {
  width: 64px;
  height: 64px;
  color: #ef4444;
  margin: 0 auto 1.5rem;
}

.bf-no-access-title {
  font-size: 1.375rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--bf-text-primary, #ffffff);
}

.bf-no-access-text {
  color: var(--bf-text-secondary, #b3b3b3);
  margin-bottom: 2rem;
  font-size: 0.9375rem;
}

.bf-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: var(--bf-btn-primary-bg);
  color: white;
  border: none;
  border-radius: var(--bf-btn-radius, 12px);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bf-back-btn svg {
  width: 18px;
  height: 18px;
}

.bf-back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px var(--bf-primary-glow);
}

/* === 管理布局 === */
.bf-admin-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1.5rem 3rem;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.bf-admin-layout.bf-visible {
  opacity: 1;
  transform: translateY(0);
}

/* === 移动端标签栏 === */
.bf-mobile-tabs {
  display: none;
  overflow-x: auto;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border-bottom: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  -webkit-overflow-scrolling: touch;
}

.bf-mobile-tabs::-webkit-scrollbar {
  display: none;
}

.bf-mobile-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: 0.625rem 1rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--bf-radius-lg, 10px);
  color: var(--bf-text-muted, #666666);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 70px;
}

.bf-mobile-tab svg {
  width: 20px;
  height: 20px;
}

.bf-mobile-tab.bf-active {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary, #ff6b35);
}

/* === 侧边栏 === */
.bf-admin-sidebar {
  position: sticky;
  top: 6rem;
  height: fit-content;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  backdrop-filter: blur(10px);
}

.bf-sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-sidebar-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bf-fire-gradient-subtle);
  border: 1px solid var(--bf-border-accent);
  border-radius: var(--bf-radius-lg, 10px);
}

.bf-sidebar-icon svg {
  width: 20px;
  height: 20px;
  color: var(--bf-primary, #ff6b35);
}

.bf-sidebar-title-group {
  flex: 1;
}

.bf-sidebar-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--bf-text-primary, #ffffff);
  margin: 0;
}

.bf-sidebar-subtitle {
  font-size: 0.6875rem;
  color: var(--bf-text-muted, #666666);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0.125rem 0 0;
}

.bf-sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.bf-nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--bf-radius-lg, 10px);
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
}

.bf-nav-item:hover {
  background: var(--bf-btn-secondary-bg, rgba(255, 255, 255, 0.05));
  color: var(--bf-text-primary, #ffffff);
}

.bf-nav-item.bf-active {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary, #ff6b35);
}

.bf-nav-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.bf-nav-label {
  flex: 1;
}

.bf-sidebar-footer {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-nav-back:hover {
  border-color: var(--bf-border-default, rgba(255, 255, 255, 0.1));
}

/* === 主内容区 === */
.bf-admin-main {
  min-width: 0;
}

.bf-content-section {
  animation: fadeIn 0.3s ease;
  padding: 1.5rem;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
}

.bf-content-section.bf-no-padding {
  padding: 0;
  background: transparent;
  border: none;
  backdrop-filter: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.bf-section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 1.5rem;
  color: var(--bf-text-primary, #ffffff);
}

/* === 统计卡片 === */
.bf-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.bf-stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-radius-xl, 14px);
  transition: all 0.2s ease;
}

.bf-stat-card:hover {
  border-color: var(--bf-border-accent);
}

.bf-stat-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--bf-radius-lg, 10px);
  flex-shrink: 0;
}

.bf-stat-icon svg {
  width: 22px;
  height: 22px;
}

.bf-stat-icon--blue {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.bf-stat-icon--red {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.bf-stat-icon--yellow {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.bf-stat-icon--green {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.bf-stat-content {
  display: flex;
  flex-direction: column;
}

.bf-stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--bf-text-primary, #ffffff);
  line-height: 1.2;
}

.bf-stat-label {
  font-size: 0.8125rem;
  color: var(--bf-text-muted, #666666);
}

/* === 信息区域 === */
.bf-info-section {
  padding: 1.25rem;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-radius-xl, 14px);
}

.bf-info-title {
  font-size: 0.9375rem;
  font-weight: 600;
  margin: 0 0 1rem;
  color: var(--bf-text-primary, #ffffff);
}

.bf-permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.bf-permission-tag {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: var(--bf-radius-full, 999px);
  font-size: 0.75rem;
  font-weight: 500;
}

/* Permission colors */
.bf-perm-blue { background: rgba(59, 130, 246, 0.15); color: #60a5fa; }
.bf-perm-purple { background: rgba(168, 85, 247, 0.15); color: #a78bfa; }
.bf-perm-red { background: rgba(239, 68, 68, 0.15); color: #f87171; }
.bf-perm-yellow { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
.bf-perm-red-dark { background: rgba(220, 38, 38, 0.15); color: #fca5a5; }
.bf-perm-orange { background: rgba(249, 115, 22, 0.15); color: #fb923c; }
.bf-perm-amber { background: rgba(217, 119, 6, 0.15); color: #fcd34d; }
.bf-perm-cyan { background: rgba(6, 182, 212, 0.15); color: #22d3ee; }
.bf-perm-rose { background: rgba(244, 63, 94, 0.15); color: #fb7185; }
.bf-perm-pink { background: rgba(236, 72, 153, 0.15); color: #f472b6; }
.bf-perm-emerald { background: rgba(16, 185, 129, 0.15); color: #34d399; }
.bf-perm-indigo { background: rgba(99, 102, 241, 0.15); color: #818cf8; }

/* === 开发中提示 === */
.bf-coming-soon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--bf-text-secondary, #b3b3b3);
  gap: 1rem;
}

.bf-coming-soon svg {
  width: 48px;
  height: 48px;
  color: var(--bf-text-muted, #666666);
}

.bf-coming-soon p {
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
  color: var(--bf-text-primary, #ffffff);
}

.bf-coming-soon span {
  font-size: 0.875rem;
  color: var(--bf-text-muted, #666666);
}

/* === 响应式 === */
@media (max-width: 1024px) {
  .bf-admin-layout {
    grid-template-columns: 1fr;
  }

  .bf-admin-sidebar {
    display: none;
  }

  .bf-mobile-tabs {
    display: flex;
  }

  .bf-admin-page {
    padding-top: 4.5rem;
  }
}

@media (max-width: 640px) {
  .bf-admin-layout {
    padding: 0 0.75rem 2rem;
    gap: 0.75rem;
  }

  .bf-content-section {
    padding: 1rem;
  }

  .bf-stats-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .bf-stat-card {
    padding: 1rem;
  }

  .bf-info-section {
    padding: 1rem;
  }

  .bf-permission-list {
    gap: 0.375rem;
  }

  .bf-permission-tag {
    font-size: 0.6875rem;
    padding: 0.25rem 0.5rem;
  }

  .bf-no-access-card {
    padding: 2rem 1.5rem;
  }

  .bf-coming-soon {
    padding: 3rem 1.5rem;
  }
}

/* === 浅色模式 === */
:root:not(.dark) .bf-admin-page {
  background: var(--bf-page-bg, #fafafa);
}

:root:not(.dark) .bf-no-access-card,
:root:not(.dark) .bf-admin-sidebar,
:root:not(.dark) .bf-content-section,
:root:not(.dark) .bf-mobile-tabs {
  background: var(--bf-card-bg, rgba(255, 255, 255, 0.9));
  border-color: var(--bf-card-border, rgba(0, 0, 0, 0.08));
}

:root:not(.dark) .bf-sidebar-title,
:root:not(.dark) .bf-section-title,
:root:not(.dark) .bf-no-access-title,
:root:not(.dark) .bf-stat-value,
:root:not(.dark) .bf-info-title,
:root:not(.dark) .bf-coming-soon p {
  color: var(--bf-text-primary, #171717);
}

:root:not(.dark) .bf-loading-text,
:root:not(.dark) .bf-no-access-text,
:root:not(.dark) .bf-sidebar-subtitle,
:root:not(.dark) .bf-stat-label,
:root:not(.dark) .bf-coming-soon span,
:root:not(.dark) .bf-nav-item:not(.bf-active) {
  color: var(--bf-text-secondary, #525252);
}

:root:not(.dark) .bf-stat-card,
:root:not(.dark) .bf-info-section {
  background: var(--bf-input-bg, rgba(0, 0, 0, 0.02));
  border-color: var(--bf-border-default, rgba(0, 0, 0, 0.08));
}

:root:not(.dark) .bf-nav-item:hover:not(.bf-active) {
  background: var(--bf-btn-secondary-bg, rgba(0, 0, 0, 0.04));
  color: var(--bf-text-primary, #171717);
}

:root:not(.dark) .bf-nav-item.bf-active {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.3);
}

:root:not(.dark) .bf-mobile-tab.bf-active {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.3);
}
</style>
