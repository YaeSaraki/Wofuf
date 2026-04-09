<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useRoute, useRouter } from 'vue-router'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { forumService } from '@M/forum/services/ForumService.ts'
import type { PostDto } from '@M/forum/dtos/Post.ts'
import { PostStatus, PostCategory, getIsPinned, getIsFeatured } from '@M/forum/dtos/Post.ts'
import { translate } from '@S/services/i18n'
import router from '@S/infra/router'
import { PlayerService } from '@M/players/services/PlayerService'
import { cacheService } from '@S/infra/cache'

const toast = useToast()
const route = useRoute()
const internalRouter = useRouter()
const playerService = new PlayerService()

// 分类选项
const categoryOptions = computed(() => [
  { id: 'ALL', label: translate('forum', 'admin.statusAll') },
  { id: PostCategory.DISCUSSION, label: translate('forum', 'category.discussion') },
  { id: PostCategory.QUESTION, label: translate('forum', 'category.question') },
  { id: PostCategory.SHOWCASE, label: translate('forum', 'category.showcase') },
  { id: PostCategory.NEWS, label: translate('forum', 'category.news') },
  { id: PostCategory.GUIDE, label: translate('forum', 'category.guide') },
])

// ==================== 状态 - 从 URL 初始化 ====================
const posts = ref<PostDto[]>([])
const isLoading = ref(false)
const errorMsg = ref('')
const currentPage = ref(Number(route.query.page) || 0)
const pageSize = ref(20)
const totalPosts = ref(0)

// 筛选状态 - 从 URL 初始化
const filterStatus = ref<'ALL' | 'NORMAL' | 'HIDDEN' | 'UNDER_REVIEW'>(
  (route.query.status as any) || 'ALL'
)
const filterCategory = ref<'ALL' | PostCategory>(
  (route.query.category as any) || 'ALL'
)
const searchQuery = ref((route.query.search as string) || '')

// 操作中的帖子ID
const operatingPostId = ref<string | null>(null)

// 头像缓存
const avatarCache = ref<Record<string, string>>({})

// ==================== 辅助函数 ====================
function getPostStatus(post: PostDto): string {
  return post.status || PostStatus.NORMAL
}

function isHidden(post: PostDto): boolean {
  return getPostStatus(post) === PostStatus.HIDDEN
}

function isUnderReview(post: PostDto): boolean {
  return getPostStatus(post) === PostStatus.UNDER_REVIEW
}

// 格式化时间
function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return translate('forum', 'time.justNow')
  if (diffMins < 60) return `${diffMins} ${translate('forum', 'time.minutesAgo')}`
  if (diffHours < 24) return `${diffHours} ${translate('forum', 'time.hoursAgo')}`
  if (diffDays < 7) return `${diffDays} ${translate('forum', 'time.daysAgo')}`

  return date.toLocaleDateString()
}

// 获取头像
async function loadAvatar(playerId: string | undefined | null): Promise<string | null> {
  if (!playerId) return null
  if (avatarCache.value[playerId]) {
    return avatarCache.value[playerId]
  }

  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        const avatar = await playerService.renderAvatar(skinData.skin, 32)
        avatarCache.value[playerId] = avatar
        return avatar
      }
    }
  } catch (e) {
    console.warn('[PostsManagement] Failed to load avatar:', e)
  }
  return null
}

// ==================== 计算属性 ====================
const totalPages = computed(() => Math.ceil(totalPosts.value / pageSize.value))

// ==================== 数据加载 ====================
async function loadPosts() {
  isLoading.value = true
  errorMsg.value = ''

  // 清除缓存以获取最新数据
  cacheService.clearModule('forum_service')

  try {
    const result = await forumService.getRecentPosts(currentPage.value, pageSize.value)

    if (result.isSuccess) {
      let fetchedPosts = result.getValue().posts

      // 状态筛选
      if (filterStatus.value !== 'ALL') {
        fetchedPosts = fetchedPosts.filter(p => getPostStatus(p) === filterStatus.value)
      }

      // 分类筛选
      if (filterCategory.value !== 'ALL') {
        fetchedPosts = fetchedPosts.filter(p => p.category === filterCategory.value)
      }

      // 搜索筛选
      if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase()
        fetchedPosts = fetchedPosts.filter(p =>
          p.title.toLowerCase().includes(query)
        )
      }

      posts.value = fetchedPosts
      totalPosts.value = fetchedPosts.length

      // 加载头像
      for (const post of fetchedPosts) {
        const playerId = post.memberPostBy?.playerId
        if (playerId && !avatarCache.value[playerId]) {
          loadAvatar(playerId)
        }
      }
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
    }
  } catch (e) {
    errorMsg.value = translate('forum', 'error')
  } finally {
    isLoading.value = false
  }
}

// ==================== 帖子操作（乐观更新） ====================

// 置顶/取消置顶
async function pinPost(postId: string, isCurrentlyPinned: boolean) {
  if (!postId) return

  // 乐观更新：立即翻转状态
  const index = posts.value.findIndex(p => p.postId === postId)
  if (index === -1) return

  const post = posts.value[index]!
  const previousIsPinned = post.isPinned
  post.isPinned = !isCurrentlyPinned

  // 显示提示
  toast.add({
    severity: 'info',
    summary: isCurrentlyPinned ? translate('forum', 'admin.unpinning') : translate('forum', 'admin.pinning'),
    life: 2000,
  })

  // 防止重复点击
  operatingPostId.value = postId

  try {
    // 调用 API
    const result = isCurrentlyPinned
      ? await adminService.unpinPost(postId)
      : await adminService.pinPost(postId)

    if (result.isSuccess) {
      // 成功：清除缓存
      cacheService.clearModule('forum_service')
    } else {
      // 失败：回滚状态
      post.isPinned = previousIsPinned
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    // 异常：回滚状态
    post.isPinned = previousIsPinned
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingPostId.value = null
  }
}

// 加精/取消加精
async function featurePost(postId: string, isCurrentlyFeatured: boolean) {
  if (!postId) return

  // 乐观更新：立即翻转状态
  const index = posts.value.findIndex(p => p.postId === postId)
  if (index === -1) return

  const post = posts.value[index]!
  const previousIsFeatured = post.isFeatured
  post.isFeatured = !isCurrentlyFeatured

  // 显示提示
  toast.add({
    severity: 'info',
    summary: isCurrentlyFeatured ? translate('forum', 'admin.unfeaturing') : translate('forum', 'admin.featuring'),
    life: 2000,
  })

  // 防止重复点击
  operatingPostId.value = postId

  try {
    // 调用 API
    const result = isCurrentlyFeatured
      ? await adminService.unfeaturePost(postId)
      : await adminService.featurePost(postId)

    if (result.isSuccess) {
      // 成功：清除缓存
      cacheService.clearModule('forum_service')
    } else {
      // 失败：回滚状态
      post.isFeatured = previousIsFeatured
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    // 异常：回滚状态
    post.isFeatured = previousIsFeatured
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingPostId.value = null
  }
}

// 隐藏/显示帖子
async function hidePost(postId: string, isCurrentlyHidden: boolean) {
  if (!postId) return

  // 乐观更新：立即翻转状态
  const index = posts.value.findIndex(p => p.postId === postId)
  if (index === -1) return

  const post = posts.value[index]!
  const previousStatus = post.status
  post.status = isCurrentlyHidden ? PostStatus.NORMAL : PostStatus.HIDDEN

  // 显示提示
  toast.add({
    severity: 'info',
    summary: isCurrentlyHidden ? translate('forum', 'admin.showing') : translate('forum', 'admin.hiding'),
    life: 2000,
  })

  // 防止重复点击
  operatingPostId.value = postId

  try {
    // 调用 API
    const result = isCurrentlyHidden
      ? await adminService.showPost(postId)
      : await adminService.hidePost(postId)

    if (result.isSuccess) {
      // 成功：清除缓存
      cacheService.clearModule('forum_service')
    } else {
      // 失败：回滚状态
      post.status = previousStatus
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    // 异常：回滚状态
    post.status = previousStatus
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingPostId.value = null
  }
}

async function reviewPost(postId: string) {
  if (!postId) {
    errorMsg.value = 'Post ID is missing'
    return
  }
  operatingPostId.value = postId
  try {
    const result = await adminService.setPostUnderReview(postId)

    if (result.isSuccess) {
      toast.add({
        severity: 'warn',
        summary: translate('forum', 'admin.setReviewSuccess'),
        life: 3000,
      })
      cacheService.clearModule('forum_service')
      await loadPosts()
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    console.error('[PostsManagement] reviewPost error:', e)
    errorMsg.value = String(e) || translate('forum', 'error')
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingPostId.value = null
  }
}

async function approvePost(postId: string) {
  if (!postId) {
    errorMsg.value = 'Post ID is missing'
    return
  }
  operatingPostId.value = postId
  try {
    const result = await adminService.approvePost(postId)

    if (result.isSuccess) {
      toast.add({
        severity: 'success',
        summary: translate('forum', 'admin.approveSuccess'),
        life: 3000,
      })
      filterStatus.value = 'ALL'
      cacheService.clearModule('forum_service')
      await loadPosts()
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
      toast.add({
        severity: 'error',
        summary: translate('forum', 'admin.operationFailed'),
        detail: String(result.error),
        life: 5000,
      })
    }
  } catch (e) {
    console.error('[PostsManagement] approvePost error:', e)
    errorMsg.value = String(e) || translate('forum', 'error')
    toast.add({
      severity: 'error',
      summary: translate('forum', 'admin.operationFailed'),
      detail: String(e),
      life: 5000,
    })
  } finally {
    operatingPostId.value = null
  }
}

function goToPost(slug: string) {
  sessionStorage.setItem('forum_post_from', '/forum/admin')
  router.push(`/forum/posts/${slug}`)
}

function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadPosts()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadPosts()
  }
}

// 防抖搜索
let searchTimeout: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadPosts()
  }, 300)
}

function onFilterChange() {
  currentPage.value = 0
  loadPosts()
}

// ==================== URL 状态同步 ====================
// 同步筛选状态到 URL
function syncStateToUrl() {
  const query: Record<string, string> = {}
  if (currentPage.value > 0) query.page = String(currentPage.value)
  if (filterStatus.value !== 'ALL') query.status = filterStatus.value
  if (filterCategory.value !== 'ALL') query.category = filterCategory.value
  if (searchQuery.value.trim()) query.search = searchQuery.value
  internalRouter.replace({ query })
}

// 监听状态变化，同步到 URL
watch(currentPage, () => syncStateToUrl())
watch(filterStatus, () => syncStateToUrl())
watch(filterCategory, () => syncStateToUrl())
watch(searchQuery, () => syncStateToUrl())

// 监听浏览器前进/后退，响应 URL 变化
watch(() => route.query, (newQuery) => {
  const newPage = Number(newQuery.page) || 0
  const newStatus = (newQuery.status as any) || 'ALL'
  const newCategory = (newQuery.category as any) || 'ALL'
  const newSearch = (newQuery.search as string) || ''

  if (newPage !== currentPage.value) currentPage.value = newPage
  if (newStatus !== filterStatus.value) filterStatus.value = newStatus
  if (newCategory !== filterCategory.value) filterCategory.value = newCategory
  if (newSearch !== searchQuery.value) {
    searchQuery.value = newSearch
  }
  // 如果 URL 变化了（不是我们主动修改的），重新加载
  if (route.name === 'admin') {
    loadPosts()
  }
}, { immediate: false })

// ==================== 生命周期 ====================
onMounted(() => {
  loadPosts()
})
</script>

<template>
  <div class="admin-container">
    <!-- 页面标题栏 -->
    <div class="admin-header">
      <div class="admin-header__title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M10.343 3.94c.09-.542.56-.94 1.11-.94h1.093c.55 0 1.02.398 1.11.94l.149 1.846c.07.63.44 1.117 1.02 1.414l2.106 1.052c.67.336 1.273.22 1.627-.28l.843-1.062c.354-.44.293-1.13.083-1.533l-.388-.776a1.107 1.107 0 00-.358-.442l-1.69-1.692a1.975 1.975 0 00-1.398-.583H11.45c-.55 0-1.02-.397-1.11-.94l-.15-1.845z" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 12v.01" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M13.657 6.343a3.857 3.857 0 00-4.114 0" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M17.829 8.172a4.01 4.01 0 00-3.828-2.172" />
        </svg>
        <h1>{{ translate('admin.management') }}</h1>
      </div>
      <span class="admin-header__count">{{ translate('forum', 'posts') }}: {{ totalPosts }}</span>
    </div>

    <!-- 工具栏 -->
    <div class="admin-toolbar">
      <!-- 搜索 -->
      <div class="admin-search">
        <svg
          class="admin-search__icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="translate('forum', 'searchPosts')"
          class="admin-search__input"
          @input="onSearchInput"
        />
      </div>

      <!-- 筛选 -->
      <div class="admin-filters">
        <select v-model="filterStatus" class="admin-select" @change="onFilterChange">
          <option value="ALL">{{ translate('forum', 'admin.statusAll') }}</option>
          <option value="NORMAL">{{ translate('forum', 'admin.statusNormal') }}</option>
          <option value="HIDDEN">{{ translate('forum', 'admin.statusHidden') }}</option>
          <option value="UNDER_REVIEW">{{ translate('forum', 'admin.statusReview') }}</option>
        </select>

        <select v-model="filterCategory" class="admin-select" @change="onFilterChange">
          <option v-for="cat in categoryOptions" :key="cat.id" :value="cat.id">
            {{ cat.label }}
          </option>
        </select>
      </div>

      <!-- 刷新按钮 -->
      <button class="admin-btn admin-btn--secondary" @click="loadPosts" :disabled="isLoading">
        <svg
          class="admin-btn__icon"
          :class="{ spinning: isLoading }"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M23 4v6h-6M1 20v-6h6" />
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
        </svg>
        {{ translate('forum', 'refresh') }}
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMsg" class="admin-error">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      <span>{{ errorMsg }}</span>
      <button class="admin-btn admin-btn--ghost" @click="errorMsg = ''">×</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && posts.length === 0" class="admin-loading">
      <div class="admin-loading__spinner"></div>
      <span>{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!isLoading && posts.length === 0" class="admin-empty">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
      </svg>
      <h3>{{ translate('forum', 'noPosts') }}</h3>
    </div>

    <!-- 帖子列表 -->
    <div v-else class="admin-list">
      <div
        v-for="post in posts"
        :key="post.postId"
        class="admin-card"
        :class="{ 'admin-card--hidden': isHidden(post), 'admin-card--review': isUnderReview(post) }"
      >
        <!-- 卡片主体 -->
        <div class="admin-card__main" @click="goToPost(post.slug)">
          <!-- 顶部：头像 + 标题 + 徽章 -->
          <div class="admin-card__header">
            <!-- 头像 -->
            <img
              v-if="avatarCache[post.memberPostBy?.playerId || '']"
              :src="avatarCache[post.memberPostBy?.playerId || '']"
              :alt="post.memberPostBy?.nickname"
              class="admin-card__avatar"
            />
            <div v-else class="admin-card__avatar admin-card__avatar--placeholder">
              {{ (post.memberPostBy?.nickname || 'U').charAt(0).toUpperCase() }}
            </div>

            <!-- 标题 -->
            <div class="admin-card__title-wrapper">
              <h4 class="admin-card__title">{{ post.title }}</h4>
              <!-- 徽章组 -->
              <div class="admin-card__badges">
                <span v-if="getIsPinned(post)" class="admin-badge admin-badge--pinned">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 17v5M9 10.5a3 3 0 115.196-3M5 21h14M12 3l-4 8h8l-4-8z" />
                  </svg>
                  置顶
                </span>
                <span v-if="getIsFeatured(post)" class="admin-badge admin-badge--featured">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                  </svg>
                  精华
                </span>
                <span v-if="isHidden(post)" class="admin-badge admin-badge--hidden">已隐藏</span>
                <span v-if="isUnderReview(post)" class="admin-badge admin-badge--review">待审核</span>
              </div>
            </div>
          </div>

          <!-- 底部：作者信息 -->
          <div class="admin-card__meta">
            <span class="admin-card__author">{{ post.memberPostBy?.nickname || '未知用户' }}</span>
            <span class="admin-card__dot">·</span>
            <span class="admin-card__time">{{ formatTime(post.createdAt) }}</span>
            <span class="admin-card__dot">·</span>
            <span class="admin-card__comments">{{ post.numComments }} 评论</span>
            <span class="admin-card__dot">·</span>
            <span class="admin-card__points">{{ post.points }} 点</span>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="admin-card__actions">
          <button
            class="admin-action-btn"
            :class="{ 'admin-action-btn--active': getIsPinned(post) }"
            @click.stop="pinPost(post.postId!, getIsPinned(post))"
            :disabled="operatingPostId === post.postId"
            title="置顶"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 17v5M9 10.5a3 3 0 115.196-3M5 21h14M12 3l-4 8h8l-4-8z" />
            </svg>
          </button>

          <button
            class="admin-action-btn"
            :class="{ 'admin-action-btn--active': getIsFeatured(post) }"
            @click.stop="featurePost(post.postId!, getIsFeatured(post))"
            :disabled="operatingPostId === post.postId"
            title="加精"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
          </button>

          <button
            class="admin-action-btn"
            :class="{ 'admin-action-btn--active': isHidden(post) }"
            @click.stop="hidePost(post.postId!, isHidden(post))"
            :disabled="operatingPostId === post.postId"
            title="隐藏"
          >
            <svg v-if="isHidden(post)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24" />
              <line x1="1" y1="1" x2="23" y2="23" />
            </svg>
          </button>

          <div class="admin-action-divider"></div>

          <button
            v-if="isUnderReview(post)"
            class="admin-action-btn admin-action-btn--approve"
            @click.stop="approvePost(post.postId!)"
            :disabled="operatingPostId === post.postId"
            title="审核通过"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </button>

          <button
            v-if="!isUnderReview(post)"
            class="admin-action-btn admin-action-btn--review"
            @click.stop="reviewPost(post.postId!)"
            :disabled="operatingPostId === post.postId"
            title="设为待审核"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 11l3 3L22 4" />
              <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="admin-pagination">
      <button class="admin-btn admin-btn--secondary" :disabled="currentPage === 0" @click="prevPage">
        {{ translate('forum', 'prevPage') }}
      </button>
      <span class="admin-pagination__info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button class="admin-btn admin-btn--secondary" :disabled="currentPage >= totalPages - 1" @click="nextPage">
        {{ translate('forum', 'nextPage') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
/* === 页面容器 === */
.admin-container {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 页面标题栏 === */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
}

.admin-header__title {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
}

.admin-header__title svg {
  width: 24px;
  height: 24px;
  color: var(--bf-primary);
}

.admin-header__title h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0;
}

.admin-header__count {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 工具栏 === */
.admin-toolbar {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 20px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  flex-wrap: wrap;
}

/* 搜索框 */
.admin-search {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.admin-search__icon {
  position: absolute;
  left: 0.875rem;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--bf-text-muted);
}

.admin-search__input {
  width: 100%;
  padding: 0.625rem 1rem 0.625rem 2.5rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  outline: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-search__input::placeholder {
  color: var(--bf-text-muted);
}

.admin-search__input:focus {
  border-color: var(--bf-border-accent);
  background: var(--bf-btn-secondary-bg);
}

/* 筛选器 */
.admin-filters {
  display: flex;
  gap: var(--bf-space-sm, 8px);
}

.admin-select {
  padding: 0.5rem 2rem 0.5rem 0.75rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary);
  font-size: 0.875rem;
  outline: none;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-select:focus {
  border-color: var(--bf-border-accent);
}

/* 按钮 */
.admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-btn:hover:not(:disabled) {
  background: var(--bf-btn-secondary-bg);
  border-color: var(--bf-border-accent);
  color: var(--bf-text-primary);
}

.admin-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-btn--secondary {
  background: var(--bf-card-bg);
}

.admin-btn--ghost {
  background: transparent;
  border: none;
  padding: 0.25rem 0.5rem;
}

.admin-btn__icon {
  width: 16px;
  height: 16px;
}

.admin-btn__icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* === 错误提示 === */
.admin-error {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: var(--bf-card-radius, 16px);
  color: #ef4444;
  font-size: 14px;
}

.admin-error svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* === 加载状态 === */
.admin-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xl, 40px);
  color: var(--bf-text-muted);
}

.admin-loading__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default);
  border-top-color: var(--bf-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* === 空状态 === */
.admin-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xl, 40px);
  color: var(--bf-text-muted);
}

.admin-empty svg {
  width: 48px;
  height: 48px;
  opacity: 0.5;
}

.admin-empty h3 {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

/* === 帖子列表 === */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* === 帖子卡片 === */
.admin-card {
  display: flex;
  align-items: center;
  padding: var(--bf-space-lg, 24px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  box-shadow: var(--bf-card-shadow);
  cursor: pointer;
  transition: all var(--bf-transition-normal, 0.25s ease);
  position: relative;
  overflow: hidden;
}

.admin-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--bf-fire-gradient);
  opacity: 0;
  transition: opacity var(--bf-transition-fast, 0.15s ease);
}

.admin-card:hover {
  border-color: var(--bf-border-accent);
  box-shadow: var(--bf-card-shadow-hover);
  transform: translateY(-2px);
}

.admin-card:hover::before {
  opacity: 1;
}

.admin-card--hidden {
  opacity: 0.6;
}

.admin-card--review {
  border-left: 3px solid #f59e0b;
}

.admin-card--review::before {
  background: #f59e0b;
  opacity: 1;
}

/* 卡片主体 */
.admin-card__main {
  flex: 1;
  min-width: 0;
}

.admin-card__header {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
  margin-bottom: 12px;
}

.admin-card__avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--bf-input-bg);
  image-rendering: pixelated;
  padding: 2px;
}

.admin-card__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bf-fire-gradient);
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.admin-card__avatar--placeholder svg {
  width: 18px;
  height: 18px;
}

.admin-card__title-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
}

.admin-card__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0;
  line-height: 1.4;
  transition: color var(--bf-transition-fast, 0.15s ease);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-card:hover .admin-card__title {
  color: var(--bf-primary);
}

.admin-card__badges {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.admin-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bf-fire-gradient-subtle);
  border: 1px solid var(--bf-border-accent);
  border-radius: 100px;
  font-size: 11px;
  font-weight: 500;
  color: var(--bf-primary);
}

.admin-badge svg {
  width: 12px;
  height: 12px;
}

.admin-badge--hidden {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.admin-badge--featured {
  background: rgba(168, 85, 247, 0.1);
  border-color: rgba(168, 85, 247, 0.3);
  color: #a855f7;
}

.admin-badge--pinned {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
}

.admin-badge--review {
  background: rgba(245, 158, 11, 0.1);
  border-color: rgba(245, 158, 11, 0.3);
  color: #f59e0b;
}

.admin-card__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--bf-text-muted);
  padding-left: 46px;
}

.admin-card__dot {
  color: var(--bf-text-muted);
  font-size: 10px;
}

.admin-card__author {
  color: var(--bf-text-secondary);
  font-weight: 500;
}

.admin-card__comments,
.admin-card__points {
  color: var(--bf-text-muted);
}

/* 操作按钮 */
.admin-card__actions {
  display: flex;
  gap: 8px;
  margin-left: var(--bf-space-md, 16px);
  flex-shrink: 0;
}

.admin-action-divider {
  width: 1px;
  height: 24px;
  background: var(--bf-border-default);
  margin: 0 4px;
}

.admin-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.admin-action-btn:hover:not(:disabled) {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary);
}

.admin-action-btn--active {
  background: rgba(59, 130, 246, 0.15);
  border-color: #3b82f6;
  color: #3b82f6;
}

.admin-action-btn--active:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.25);
  color: #3b82f6;
}

.admin-action-btn--approve {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.admin-action-btn--approve:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.admin-action-btn--review {
  background: rgba(245, 158, 11, 0.1);
  border-color: rgba(245, 158, 11, 0.3);
  color: #f59e0b;
}

.admin-action-btn--review:hover:not(:disabled) {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.admin-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-action-btn svg {
  width: 18px;
  height: 18px;
}

/* === 分页 === */
.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-lg, 24px);
}

.admin-pagination__info {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 响应式 === */
@media (max-width: 640px) {
  .admin-header {
    flex-direction: column;
    gap: var(--bf-space-sm, 12px);
    padding: var(--bf-space-sm, 12px);
    align-items: flex-start;
  }

  .admin-toolbar {
    flex-direction: column;
    gap: var(--bf-space-sm, 12px);
    padding: var(--bf-space-sm, 12px);
  }

  .admin-search {
    width: 100%;
  }

  .admin-filters {
    width: 100%;
    flex-wrap: wrap;
  }

  .admin-card {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--bf-space-sm, 12px);
  }

  .admin-card__header {
    flex-wrap: wrap;
  }

  .admin-card__title-wrapper {
    flex-wrap: wrap;
    width: 100%;
  }

  .admin-card__badges {
    width: 100%;
    flex-wrap: wrap;
  }

  .admin-card__actions {
    margin-left: 0;
    margin-top: var(--bf-space-sm, 8px);
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
