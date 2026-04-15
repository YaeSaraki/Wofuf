<script lang="ts" setup>
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PostCategory, type PostDto } from '@M/forum/dtos/Post'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { translate } from '@S/services/i18n'
import PostCard from '@M/forum/components/postCard/PostCard.vue'
import { cacheService } from '@S/infra/cache'

const router = useRouter()
const route = useRoute()
const { isLoading, errorMsg } = useAsyncLoader()

// 帖子列表数据
const posts = ref<PostDto[]>([])

// 搜索关键词
const searchQuery = ref('')
const isSearching = ref(false)

// 当前排序模式：最新 / 热门 - 从 URL 初始化
const sortMode = ref<'recent' | 'popular'>(
  (route.query.sort as 'recent' | 'popular') || 'recent'
)

// 独立分页管理：最新和热门分开计数
const pageInfo = ref({
  recent: { current: 0, hasMore: true },
  popular: { current: 0, hasMore: true },
  search: { current: 0, hasMore: true },
})

// 每页加载数量
const pageSize = ref(10)

// 加载更多状态
const isLoadingMore = ref(false)

// 防止重复获取的状态
const isFetching = ref(false)

// 虚拟分类：全部（不在枚举内）
const ALL_CATEGORY = 'ALL' as const
type CategoryValue = PostCategory | typeof ALL_CATEGORY

// 当前选中的分类 - 从 URL 初始化
const selectedCategory = ref<CategoryValue>(
  (route.query.category as CategoryValue) || ALL_CATEGORY
)

// 分类展示列表
const categories = [
  { id: ALL_CATEGORY, label: '全部', icon: 'grid' },
  { id: PostCategory.DISCUSSION, label: '讨论', icon: 'chat' },
  { id: PostCategory.QUESTION, label: '求助', icon: 'question' },
  { id: PostCategory.SHOWCASE, label: '展示', icon: 'image' },
  { id: PostCategory.NEWS, label: '新闻', icon: 'news' },
  { id: PostCategory.GUIDE, label: '指南', icon: 'book' },
]

// ==================== URL 状态同步 ====================
// 同步状态到 URL Query
function syncStateToUrl() {
  const query: Record<string, string> = {}
  if (sortMode.value !== 'recent') query.sort = sortMode.value
  if (selectedCategory.value !== ALL_CATEGORY) query.category = selectedCategory.value
  // 使用 replace 避免污染历史栈
  router.replace({ query })
}

// 监听状态变化，同步到 URL
watch(sortMode, syncStateToUrl)
watch(selectedCategory, syncStateToUrl)

// 监听浏览器前进/后退，响应 URL 变化
watch(() => route.query, (newQuery) => {
  const newSort = (newQuery.sort as 'recent' | 'popular') || 'recent'
  const newCategory = (newQuery.category as CategoryValue) || ALL_CATEGORY

  if (newSort !== sortMode.value) sortMode.value = newSort
  if (newCategory !== selectedCategory.value) {
    selectedCategory.value = newCategory
    // 切换分类后，两个排序的分页都重置
    pageInfo.value.recent = { current: 0, hasMore: true }
    pageInfo.value.popular = { current: 0, hasMore: true }
    fetchPosts(false)
  }
}, { immediate: false })

// 登录状态计算属性
const isLoggedIn = computed(() => authService.isAuthenticated())

// 当前是否还有更多数据可加载
const hasMore = computed(() => pageInfo.value[sortMode.value].hasMore)

// 是否为搜索模式
const isInSearchMode = computed(() => searchQuery.value.trim().length > 0)

// 获取帖子列表核心方法
async function fetchPosts(append: boolean = false) {
  const mode = sortMode.value

  // 防止重复获取
  if (isFetching.value) {
    console.debug('[PostList] fetchPosts 跳过：已有请求在进行中')
    return
  }

  isFetching.value = true

  // 如果不是加载更多，则重置到第一页（page=0）
  const page = append ? pageInfo.value[mode].current : 0

  // 非加载更多时清空数据并重置状态
  if (!append) {
    posts.value = []
    pageInfo.value[mode].hasMore = true
  }

  // 标记加载更多状态
  if (append) {
    isLoadingMore.value = true
  }

  try {
    // 全部分类传 undefined，后端查询所有
    const category = selectedCategory.value === ALL_CATEGORY ? undefined : selectedCategory.value

    let result
    if (isInSearchMode.value) {
      // 搜索模式
      isSearching.value = true
      result = await forumService.searchPosts(searchQuery.value.trim(), page, pageSize.value, category)
    } else {
      // 根据排序模式调用对应接口
      result = await (mode === 'recent'
        ? forumService.getRecentPosts(page, pageSize.value, category)
        : forumService.getPopularPosts(page, pageSize.value, category))
    }

    if (result?.isSuccess) {
      const newPosts = result.getValue().posts

      // 加载更多则追加，否则覆盖
      if (append) {
        posts.value.push(...newPosts)
      } else {
        posts.value = newPosts
      }

      // 如果返回数量不足一页，表示没有更多数据
      if (newPosts.length < pageSize.value) {
        pageInfo.value[mode].hasMore = false
      }

      // 有数据才翻页
      if (newPosts.length > 0) {
        pageInfo.value[mode].current += 1
      }
    }
  } catch (err) {
    console.error('加载帖子失败:', err)
  } finally {
    // 结束加载状态
    isFetching.value = false
    isSearching.value = false
    // 结束加载更多状态
    if (append) {
      isLoadingMore.value = false
    }
  }
}

// 滚动到底加载下一页
async function loadMore() {
  // 防重复触发
  if (!hasMore.value || isLoadingMore.value || isLoading.value || isFetching.value) return
  await fetchPosts(true)
}

// 切换最新/热门排序
function setSortMode(mode: 'recent' | 'popular') {
  if (mode === sortMode.value) return
  sortMode.value = mode
  fetchPosts(false)
}

// 切换分类
function setCategory(categoryId: CategoryValue) {
  if (categoryId === selectedCategory.value) return
  selectedCategory.value = categoryId

  // 切换分类后，所有分页都重置
  pageInfo.value.recent = { current: 0, hasMore: true }
  pageInfo.value.popular = { current: 0, hasMore: true }
  pageInfo.value.search = { current: 0, hasMore: true }

  fetchPosts(false)
}

// 执行搜索
function performSearch() {
  // 搜索时重置分页
  pageInfo.value.search = { current: 0, hasMore: true }
  fetchPosts(false)
}

// 清除搜索
function clearSearch() {
  searchQuery.value = ''
  // 清除搜索后重置分页
  pageInfo.value.search = { current: 0, hasMore: true }
  fetchPosts(false)
}

// 点赞
async function handleUpvote(post: PostDto) {
  if (!post.postId) return
  const result = await forumService.upvotePost(post.postId)
  if (result.isSuccess) {
    const data = result.getValue()
    const index = posts.value.findIndex((p) => p.postId === post.postId)
    if (index !== -1) {
      const item = posts.value[index]!
      posts.value[index] = {
        ...item,
        points: data.points ?? data.newPoints,
        wasUpvotedByMe: !item.wasUpvotedByMe,
        wasDownvotedByMe: false,
      } as PostDto
    }
  }
}

// 点踩
async function handleDownvote(post: PostDto) {
  if (!post.postId) return
  const result = await forumService.downvotePost(post.postId)
  if (result.isSuccess) {
    const data = result.getValue()
    const index = posts.value.findIndex((p) => p.postId === post.postId)
    if (index !== -1) {
      const item = posts.value[index]!
      posts.value[index] = {
        ...item,
        points: data.points ?? data.newPoints,
        wasUpvotedByMe: false,
        wasDownvotedByMe: !item.wasDownvotedByMe,
      } as PostDto
    }
  }
}

// 前往发帖页面
function goToCreatePost() {
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  router.push('/forum/create')
}

// 重试加载
function retry() {
  fetchPosts(false)
}

// 滚动监听
function handleScroll() {
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  if (scrollTop + clientHeight >= scrollHeight - 300) {
    loadMore()
  }
}

onMounted(() => {
  // 清除缓存以获取最新数据
  cacheService.clearModule('forum_service')
  fetchPosts()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="bf-post-list-container">
    <!-- 搜索栏 -->
    <div class="bf-search-bar">
      <div class="bf-search-input-wrapper">
        <svg class="bf-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索帖子标题或内容..."
          class="bf-search-input"
          @keydown.enter="performSearch"
        />
        <button v-if="searchQuery" class="bf-search-clear" @click="clearSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
      <button class="bf-search-btn" @click="performSearch">
        {{ translate('forum', 'search') }}
      </button>
    </div>

    <!-- 搜索结果提示 -->
    <div v-if="isInSearchMode" class="bf-search-result-hint">
      <span>搜索 "{{ searchQuery }}" 的结果</span>
      <button class="bf-clear-search" @click="clearSearch">清除</button>
    </div>

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

    <div class="bf-forum__header">
      <div class="bf-forum__tabs">
        <button
          class="bf-tab"
          :class="{ 'bf-tab--active': sortMode === 'recent' }"
          @click="setSortMode('recent')"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          {{ translate('forum', 'sortRecent') }}
        </button>
        <button
          class="bf-tab"
          :class="{ 'bf-tab--active': sortMode === 'popular' }"
          @click="setSortMode('popular')"
        >
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
          {{ translate('forum', 'sortPopular') }}
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="bf-loading">
      <div class="bf-loading__spinner"></div>
      <span class="bf-loading__text">{{ translate('forum', 'loading') }}</span>
    </div>

    <div v-else-if="errorMsg" class="bf-error">
      <div class="bf-error__icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>
      <p class="bf-error__text">{{ errorMsg }}</p>
      <button class="bf-btn bf-btn--secondary" @click="retry">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
          />
        </svg>
        {{ translate('forum', 'retry') }}
      </button>
    </div>

    <template v-else>
      <div class="bf-post-list">
        <PostCard
          v-for="post in posts"
          :key="post.slug"
          :post="post"
          @upvote="handleUpvote"
          @downvote="handleDownvote"
        />
      </div>

      <div v-if="isLoadingMore" class="bf-load-more">
        <div class="bf-loading__spinner bf-loading__spinner--sm"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="!hasMore && posts.length > 0" class="bf-no-more">
        <span>已经到底啦 ~</span>
      </div>

      <div v-if="posts.length === 0" class="bf-empty">
        <div class="bf-empty__icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            />
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
    </template>
  </div>
</template>

<style scoped>
/* === 容器 === */
.bf-post-list-container {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 搜索栏 === */
.bf-search-bar {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
}

.bf-search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: 10px 14px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 12px);
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-search-input-wrapper:focus-within {
  border-color: var(--bf-primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.15);
}

.bf-search-icon {
  width: 18px;
  height: 18px;
  color: var(--bf-text-muted);
  flex-shrink: 0;
}

.bf-search-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--bf-text-primary);
  font-size: 14px;
  outline: none;
}

.bf-search-input::placeholder {
  color: var(--bf-text-muted);
}

.bf-search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--bf-text-muted);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-search-clear:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-primary);
}

.bf-search-clear svg {
  width: 14px;
  height: 14px;
}

.bf-search-btn {
  padding: 10px 20px;
  background: var(--bf-fire-gradient);
  border: none;
  border-radius: var(--bf-input-radius, 12px);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-search-btn:hover {
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
  transform: translateY(-1px);
}

/* === 搜索结果提示 === */
.bf-search-result-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-sm, 10px) var(--bf-space-md, 16px);
  background: rgba(255, 107, 53, 0.08);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: var(--bf-card-radius-sm, 12px);
  font-size: 13px;
  color: var(--bf-primary);
}

.bf-clear-search {
  padding: 4px 12px;
  background: transparent;
  border: 1px solid var(--bf-primary);
  border-radius: 6px;
  color: var(--bf-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-clear-search:hover {
  background: var(--bf-primary);
  color: white;
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
  to {
    transform: rotate(360deg);
  }
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
  color: #ef4444;
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
