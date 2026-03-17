/**
 * 帖子列表组件 - 支持分类筛选和无限滚动
 */

<script lang="ts" setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { translate } from '@S/services/i18n'
import PostCard from '@M/forum/components/postCard/PostCard.vue'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 帖子数据和排序
const posts = ref<PostDto[]>([])
const sortMode = ref<'recent' | 'popular'>('recent')
const currentOffset = ref(10)
const hasMore = ref(true)
const isLoadingMore = ref(false)

// 当前选中的分类
const selectedCategory = ref<string>('all')

// 分类列表
const categories = [
  { id: 'all', label: '全部', icon: 'grid' },
  { id: 'discussion', label: '讨论', icon: 'chat' },
  { id: 'share', label: '分享', icon: 'share' },
  { id: 'question', label: '求助', icon: 'question' },
  { id: 'announcement', label: '公告', icon: 'announcement' },
]

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 获取帖子列表
async function fetchPosts(append: boolean = false) {
  if (append) {
    isLoadingMore.value = true
  }

  const fetchFn = sortMode.value === 'recent'
    ? () => forumService.getRecentPosts(currentOffset.value)
    : () => forumService.getPopularPosts(currentOffset.value)

  const result = await executeAsync(fetchFn, translate('forum', 'error'))

  if (result && result.isSuccess) {
    const newPosts = result.getValue().posts

    if (append) {
      posts.value = [...posts.value, ...newPosts]
      // 如果返回的帖子数量少于请求数量，说明没有更多了
      if (newPosts.length < currentOffset.value) {
        hasMore.value = false
      }
    } else {
      posts.value = newPosts
      hasMore.value = newPosts.length >= currentOffset.value
    }
  }

  isLoadingMore.value = false
}

// 加载更多
async function loadMore() {
  if (!hasMore.value || isLoadingMore.value) return

  currentOffset.value += 10
  await fetchPosts(true)
}

// 切换排序
function setSortMode(mode: 'recent' | 'popular') {
  if (mode !== sortMode.value) {
    sortMode.value = mode
    currentOffset.value = 10
    hasMore.value = true
    fetchPosts()
  }
}

// 切换分类
function setCategory(categoryId: string) {
  if (categoryId !== selectedCategory.value) {
    selectedCategory.value = categoryId
    currentOffset.value = 10
    hasMore.value = true
    fetchPosts()
  }
}

// 处理投票
async function handleUpvote(post: PostDto) {
  if (!post.postId) return
  
  const result = await forumService.upvotePost(post.postId)
  if (result.isSuccess) {
    const data = result.getValue()
    // 更新帖子数据
    const index = posts.value.findIndex(p => p.postId === post.postId)
    if (index !== -1) {
      const currentPost = posts.value[index]
      // toggle 逻辑：如果已经 upvoted，再次点击会取消
      const wasUpvoted = currentPost.wasUpvotedByMe
      posts.value[index] = {
        ...currentPost,
        points: data.points ?? data.newPoints,
        wasUpvotedByMe: !wasUpvoted,
        wasDownvotedByMe: false
      } as PostDto
    }
  }
}

async function handleDownvote(post: PostDto) {
  if (!post.postId) return
  
  const result = await forumService.downvotePost(post.postId)
  if (result.isSuccess) {
    const data = result.getValue()
    // 更新帖子数据
    const index = posts.value.findIndex(p => p.postId === post.postId)
    if (index !== -1) {
      const currentPost = posts.value[index]
      // toggle 逻辑：如果已经 downvoted，再次点击会取消
      const wasDownvoted = currentPost.wasDownvotedByMe
      posts.value[index] = {
        ...currentPost,
        points: data.points ?? data.newPoints,
        wasUpvotedByMe: false,
        wasDownvotedByMe: !wasDownvoted
      } as PostDto
    }
  }
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

// 无限滚动处理
function handleScroll() {
  if (isLoadingMore.value || !hasMore.value) return

  const scrollHeight = document.documentElement.scrollHeight
  const scrollTop = window.scrollY
  const clientHeight = window.innerHeight

  // 距离底部 200px 时触发加载
  if (scrollTop + clientHeight >= scrollHeight - 200) {
    loadMore()
  }
}

onMounted(() => {
  fetchPosts()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="bf-post-list-container">
    <!-- 分类标签 -->
    <div class="bf-categories">
      <button
        v-for="category in categories"
        :key="category.id"
        class="bf-category-btn"
        :class="{ 'bf-category-btn--active': selectedCategory === category.id }"
        @click="setCategory(category.id)"
      >
        {{ category.label }}
      </button>
    </div>

    <!-- 顶部操作栏 -->
    <div class="bf-forum__header">
      <div class="bf-forum__tabs">
        <button
          class="bf-tab"
          :class="{ 'bf-tab--active': sortMode === 'recent' }"
          @click="setSortMode('recent')"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ translate('forum', 'sortRecent') }}
        </button>
        <button
          class="bf-tab"
          :class="{ 'bf-tab--active': sortMode === 'popular' }"
          @click="setSortMode('popular')"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343a7.975 7.975 0 010 11.314z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.879 16.121A3 3 0 1012.015 11L11 14H9c0 .768.293 1.536.879 2.121z" />
          </svg>
          {{ translate('forum', 'sortPopular') }}
        </button>
      </div>
      <button class="bf-btn bf-btn--primary bf-btn--create" @click="goToCreatePost">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        {{ translate('forum', 'create_post') }}
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="bf-loading">
      <div class="bf-loading__spinner"></div>
      <span class="bf-loading__text">{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="errorMsg" class="bf-error">
      <div class="bf-error__icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <p class="bf-error__text">{{ errorMsg }}</p>
      <button class="bf-btn bf-btn--secondary" @click="retry">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        {{ translate('forum', 'retry') }}
      </button>
    </div>

    <!-- 帖子列表 -->
    <div v-else-if="posts.length > 0" class="bf-post-list">
      <PostCard
        v-for="post in posts"
        :key="post.slug"
        :post="post"
        @upvote="handleUpvote"
        @downvote="handleDownvote"
        @unvote="handleUnvote"
      />

      <!-- 加载更多指示器 -->
      <div v-if="isLoadingMore" class="bf-load-more">
        <div class="bf-loading__spinner bf-loading__spinner--sm"></div>
        <span>加载中...</span>
      </div>

      <!-- 没有更多提示 -->
      <div v-else-if="!hasMore" class="bf-no-more">
        <span>已经到底啦 ~</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="bf-empty">
      <div class="bf-empty__icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
        </svg>
      </div>
      <h3 class="bf-empty__title">{{ translate('forum', 'noPosts') }}</h3>
      <p class="bf-empty__desc">{{ translate('forum', 'noPostsDesc') }}</p>
      <button class="bf-btn bf-btn--primary" @click="goToCreatePost">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        {{ translate('forum', 'create_post') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
/* === 容器 === */
.bf-post-list-container {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 分类标签 === */
.bf-categories {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  flex-wrap: wrap;
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.bf-category-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--bf-text-secondary);
  background: var(--bf-input-bg);
  border: 1px solid transparent;
  border-radius: 100px;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-category-btn:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-primary);
}

.bf-category-btn--active {
  background: var(--bf-fire-gradient);
  color: white;
  border-color: var(--bf-border-accent);
}

/* === 顶部操作栏 === */
.bf-forum__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: var(--bf-card-shadow);
}

.bf-forum__tabs {
  display: flex;
  gap: var(--bf-space-sm, 8px);
}

/* === 标签按钮 === */
.bf-tab {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: 10px var(--bf-space-md, 16px);
  background: var(--bf-input-bg);
  border: 1px solid transparent;
  border-radius: var(--bf-input-radius, 10px);
  font-size: 14px;
  font-weight: 500;
  color: var(--bf-text-muted);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-tab svg {
  width: 18px;
  height: 18px;
}

.bf-tab:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-secondary);
}

.bf-tab--active {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary);
}

/* === 按钮样式 === */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: 10px 18px;
  border-radius: var(--bf-btn-radius, 12px);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-btn svg {
  width: 18px;
  height: 18px;
}

.bf-btn--primary {
  background: var(--bf-btn-primary-bg);
  color: white;
}

.bf-btn--primary:hover {
  background: var(--bf-btn-primary-hover);
  box-shadow: 0 4px 16px var(--bf-primary-glow);
}

.bf-btn--secondary {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-primary);
  border: 1px solid var(--bf-border-default);
}

.bf-btn--secondary:hover {
  background: var(--bf-btn-secondary-hover);
}

.bf-btn--create {
  display: none;
}

/* === 帖子列表 === */
.bf-post-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 加载更多 === */
.bf-load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-lg, 24px);
  color: var(--bf-text-muted);
  font-size: 14px;
}

.bf-loading__spinner--sm {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

/* === 没有更多 === */
.bf-no-more {
  text-align: center;
  padding: var(--bf-space-lg, 24px);
  color: var(--bf-text-muted);
  font-size: 13px;
}

/* === 加载状态 === */
.bf-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.bf-loading__spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--bf-border-default);
  border-top-color: var(--bf-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: var(--bf-space-md, 16px);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.bf-loading__text {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 错误状态 === */
.bf-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
}

.bf-error__icon {
  width: 48px;
  height: 48px;
  color: #EF4444;
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-error__icon svg {
  width: 100%;
  height: 100%;
}

.bf-error__text {
  font-size: 14px;
  color: var(--bf-text-muted);
  margin: 0 0 20px 0;
}

/* === 空状态 === */
.bf-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
}

.bf-empty__icon {
  width: 64px;
  height: 64px;
  color: var(--bf-text-muted);
  margin-bottom: 20px;
}

.bf-empty__icon svg {
  width: 100%;
  height: 100%;
}

.bf-empty__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0 0 var(--bf-space-sm, 8px) 0;
}

.bf-empty__desc {
  font-size: 14px;
  color: var(--bf-text-muted);
  margin: 0 0 var(--bf-space-lg, 24px) 0;
  max-width: 300px;
}

/* === 响应式 === */
@media (max-width: 640px) {
  .bf-categories {
    padding: var(--bf-space-sm, 12px);
  }

  .bf-category-btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .bf-forum__header {
    flex-direction: column;
    gap: var(--bf-space-md, 16px);
    padding: 14px var(--bf-space-md, 16px);
  }

  .bf-forum__tabs {
    width: 100%;
  }

  .bf-tab {
    flex: 1;
    justify-content: center;
    padding: 10px 12px;
    font-size: 13px;
  }

  .bf-btn--create {
    display: inline-flex;
    width: 100%;
  }

  .bf-post-list {
    gap: 12px;
  }
}
</style>
