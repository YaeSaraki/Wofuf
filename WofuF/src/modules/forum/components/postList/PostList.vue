<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { translate } from '@S/services/i18n'
import PageBackground from '@S/components/PageBackground.vue'
import PostCard from '@M/forum/components/postCard/PostCard.vue'
import Button from 'primevue/button'
import TabMenu from 'primevue/tabmenu'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 帖子数据和排序
const posts = ref<PostDto[]>([])
const sortMode = ref<'recent' | 'popular'>('recent')
const currentOffset = ref(10)

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 排序选项
const sortTabs = computed(() => [
  {
    label: translate('forum', 'sortRecent'),
    icon: 'pi pi-clock',
    value: 'recent',
  },
  {
    label: translate('forum', 'sortPopular'),
    icon: 'pi pi-bolt',
    value: 'popular',
  },
])

// 当前选中的 Tab
const activeTab = computed(() => sortTabs.value.findIndex((tab) => tab.value === sortMode.value) ?? 0)

// 获取帖子列表
async function fetchPosts() {
  const fetchFn = sortMode.value === 'recent'
    ? () => forumService.getRecentPosts(currentOffset.value)
    : () => forumService.getPopularPosts(currentOffset.value)

  const result = await executeAsync(fetchFn, translate('forum', 'error'))

  if (result && result.isSuccess) {
    posts.value = result.getValue().posts
  }
}

// 切换排序
function handleTabChange(event: { index: number }) {
  const tab = sortTabs.value[event.index]
  if (!tab) return
  const newMode = tab.value as 'recent' | 'popular'
  if (newMode !== sortMode.value) {
    sortMode.value = newMode
    fetchPosts()
  }
}

// 处理投票
async function handleUpvote(post: PostDto) {
  console.log('Upvote post:', post.slug)
}

async function handleDownvote(post: PostDto) {
  console.log('Downvote post:', post.slug)
}

// 跳转到发帖页
function goToCreatePost() {
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  router.push('/forum/create')
}

// 重试
function retry() {
  fetchPosts()
}

onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <PageBackground variant="default" :show-pattern="true">
    <!-- 主内容区 -->
    <main class="forum-content">
      <!-- 排序标签 -->
      <div class="sort-tabs">
        <TabMenu :model="sortTabs" :activeIndex="activeTab" @tab-change="handleTabChange" />
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <span>{{ translate('forum', 'loading') }}</span>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="errorMsg" class="error-state">
        <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
        <p class="error-text">{{ errorMsg }}</p>
        <Button
          :label="translate('forum', 'retry')"
          icon="pi pi-refresh"
          @click="retry"
          outlined
        />
      </div>

      <!-- 帖子列表 -->
      <div v-else-if="posts.length > 0" class="post-list">
        <PostCard
          v-for="post in posts"
          :key="post.slug"
          :post="post"
          @upvote="handleUpvote"
          @downvote="handleDownvote"
        />
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
          />
        </svg>
        <h3 class="empty-title">{{ translate('forum', 'noPosts') }}</h3>
        <p class="empty-desc">{{ translate('forum', 'noPostsDesc') }}</p>
        <Button
          :label="translate('forum', 'create_post')"
          icon="pi pi-plus"
          @click="goToCreatePost"
        />
      </div>
    </main>
  </PageBackground>
</template>

<style scoped>
/* 主内容 */
.forum-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 1.5rem 1rem;
}

/* 排序标签 */
.sort-tabs {
  margin-bottom: 1.5rem;
  background: var(--w-surface-card);
  border-radius: var(--w-radius-lg);
  border: 1px solid var(--w-border);
  overflow: hidden;
  box-shadow: var(--w-shadow-sm);
}

/* 暗色模式增强 */
:global(.dark) .sort-tabs {
  background: #1c1c1e;
  border-color: #3a3a3c;
}

:deep(.p-tabmenu) {
  background: transparent;
}

:deep(.p-tabmenu-nav) {
  background: transparent;
  border: none;
}

:deep(.p-tabmenuitem) {
  margin: 0;
}

:deep(.p-menuitem-link) {
  padding: 1rem 1.5rem;
  border-radius: 0;
  color: var(--w-text-secondary);
}

:deep(.p-tabmenuitem.p-highlight .p-menuitem-link) {
  background: transparent;
  border-bottom: 2px solid var(--w-primary);
  color: var(--w-primary);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  color: var(--w-text-muted);
}

.spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid var(--w-border);
  border-top-color: var(--w-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  text-align: center;
}

.error-icon {
  width: 3rem;
  height: 3rem;
  color: var(--w-error);
  margin-bottom: 1rem;
}

.error-text {
  color: var(--w-text-muted);
  margin: 0 0 1.5rem 0;
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  text-align: center;
}

.empty-icon {
  width: 4rem;
  height: 4rem;
  color: var(--w-text-muted);
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--w-text);
  margin: 0 0 0.5rem 0;
}

.empty-desc {
  color: var(--w-text-muted);
  margin: 0 0 1.5rem 0;
}

/* 响应式 */
@media (max-width: 640px) {
  .forum-title {
    font-size: 1.75rem;
  }

  .forum-subtitle {
    font-size: 0.875rem;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  :deep(.p-menuitem-link) {
    padding: 0.75rem 1rem;
  }
}
</style>
