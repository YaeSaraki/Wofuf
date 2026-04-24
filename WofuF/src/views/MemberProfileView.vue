<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { PlayerService } from '@M/players/services/PlayerService'

const playerService = new PlayerService()
import type { MemberProfileDto, PostSummary, CommentSummary } from '@M/forum/dtos/Member'
import { translate } from '@S/services/i18n'
import router from '@S/infra/router'
import { useToast } from 'primevue/usetoast'
import PrimeDialog from 'primevue/dialog'
import PrimeButton from 'primevue/button'
import InputText from 'primevue/inputtext'
import { getLocale, currentLocale } from '@S/services/i18n/useLocale'

const route = useRoute()
const router_vue = useRouter()
const toast = useToast()

// 成员昵称
const nickname = computed(() => route.params.nickname as string)

// 检测是否为 UUID（成员ID）
const isUUID = (str: string) => /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str)

// resolvedNickname - 如果是 UUID 则重定向到 nickname URL
const resolvedNickname = ref<string | null>(null)

async function resolveNicknameFromId(idOrNickname: string): Promise<string | null> {
  if (!isUUID(idOrNickname)) {
    return idOrNickname // 本身就是 nickname
  }
  // 是 UUID，需要通过 API 获取 nickname
  const result = await forumService.getMemberById(idOrNickname)
  if (result.isSuccess) {
    return result.getValue().nickname
  }
  return null
}

// 成员资料
const profile = ref<MemberProfileDto | null>(null)
const isLoading = ref(true)
const error = ref<string | null>(null)

// 评论列表
const comments = ref<CommentSummary[]>([])
const commentsLoading = ref(false)
const commentsTotal = ref(0)

// 当前登录用户的 userId（响应式）
const currentUserIdRef = ref<string | null>(null)

// 是否是本人
const isOwner = computed(() => {
  if (!profile.value) return false
  // 优先用已记录的 userId（登录后立即可用），其次用 authService
  const userId = currentUserIdRef.value ?? authService.getTokens()?.userId
  if (!userId) return false
  return userId === profile.value.userId
})

// 初始化时记录当前用户 ID
currentUserIdRef.value = authService.getTokens()?.userId ?? null

// 头像
const avatarUrl = ref<string | null>(null)

async function loadAvatar() {
  if (!profile.value?.playerId) {
    avatarUrl.value = null
    return
  }
  try {
    const result = await playerService.getPlayerSkin(profile.value.playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        avatarUrl.value = await playerService.renderAvatar(skinData.skin, 128)
      }
    }
  } catch (e) {
    avatarUrl.value = null
  }
}

// 标签页
const activeTab = ref<'posts' | 'comments'>('posts')

// 搜索与筛选
const searchQuery = ref('')
const selectedCategory = ref<string | null>(null)

// 分类选项（直接引用 currentLocale.value 确保 Vue 响应式追踪）
const categories = computed(() => {
  // 显式追踪 Vue ref，建立响应式依赖
  const _locale = currentLocale.value
  return [
    { id: null, label: translate('forum', 'admin.categoryAll') },
    { id: 'DISCUSSION', label: translate('forum', 'category.discussion') },
    { id: 'QUESTION', label: translate('forum', 'category.question') },
    { id: 'SHOWCASE', label: translate('forum', 'category.showcase') },
    { id: 'NEWS', label: translate('forum', 'category.news') },
    { id: 'GUIDE', label: translate('forum', 'category.guide') },
  ]
})

// 弹窗状态
const showNicknameEdit = ref(false)
const nicknameValue = ref('')
const nicknameLoading = ref(false)
const nicknameError = ref('')


const showDeleteConfirm = ref(false)
const deleteTarget = ref<{ type: 'post' | 'comment'; id: string; title?: string } | null>(null)

// 加载成员资料
async function loadProfile() {
  if (!nickname.value) return

  isLoading.value = true
  error.value = null

  // 如果是 UUID，先解析为 nickname 并重定向
  if (isUUID(nickname.value)) {
    const resolved = await resolveNicknameFromId(nickname.value)
    if (resolved) {
      // 重定向到 nickname URL
      router_vue.replace(`/forum/members/${resolved}`)
      isLoading.value = false
      return
    } else {
      error.value = 'Member not found'
      isLoading.value = false
      return
    }
  }

  const result = await forumService.getMemberProfile(nickname.value)
  if (result.isSuccess) {
    profile.value = result.getValue()
  } else {
    error.value = String(result.error)
  }

  isLoading.value = false
}

// 加载评论列表
async function loadComments() {
  if (!nickname.value) return

  commentsLoading.value = true
  const result = await forumService.getMemberComments(nickname.value, 0, 20)
  if (result.isSuccess) {
    const data = result.getValue()
    comments.value = data.comments
    commentsTotal.value = data.total
  }
  commentsLoading.value = false
}

// 加载当前用户
// 格式化日期
const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

// 格式化相对时间
const formatRelativeTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return translate('forum', 'time.justNow')
  if (diffMins < 60) return `${diffMins} ${translate('forum', 'time.minutesAgo')}`
  if (diffHours < 24) return `${diffHours} ${translate('forum', 'time.hoursAgo')}`
  if (diffDays < 30) return `${diffDays} ${translate('forum', 'time.daysAgo')}`

  return date.toLocaleDateString()
}

// 计算等级 (声望每100点一级)
const level = computed(() => {
  if (!profile.value) return 1
  return Math.floor(profile.value.reputation / 100) + 1
})

// 计算当前等级进度
const levelProgress = computed(() => {
  if (!profile.value) return 0
  return profile.value.reputation % 100
})

// 翻译分类
const translateCategory = (category: string) => {
  const map: Record<string, string> = {
    DISCUSSION: translate('forum', 'category.discussion'),
    QUESTION: translate('forum', 'category.question'),
    SHOWCASE: translate('forum', 'category.showcase'),
    NEWS: translate('forum', 'category.news'),
    GUIDE: translate('forum', 'category.guide'),
  }
  return map[category] ?? category
}

// 过滤后的帖子列表
const filteredPosts = computed(() => {
  if (!profile.value) return []
  let posts = profile.value.postHistory

  // 按分类筛选
  if (selectedCategory.value) {
    posts = posts.filter(p => p.category === selectedCategory.value)
  }

  // 按搜索词筛选
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    posts = posts.filter(p => p.title.toLowerCase().includes(q))
  }

  return posts
})

// 清除搜索
const clearSearch = () => {
  searchQuery.value = ''
}

// 切换分类
const setCategory = (categoryId: string | null) => {
  selectedCategory.value = categoryId
}

// 跳转到玩家数据页
const goToPlayer = () => {
  if (profile.value?.playerId) {
    router.push(`/players/${profile.value.playerId}`)
  }
}

// 跳转到帖子详情
const goToPost = (slug: string) => {
  router.push(`/forum/posts/${slug}`)
}

// 打开昵称编辑弹窗
const openNicknameEdit = () => {
  nicknameValue.value = profile.value?.nickname || ''
  nicknameError.value = ''
  showNicknameEdit.value = true
}

// 保存昵称
async function saveNickname() {
  if (!nicknameValue.value.trim()) {
    nicknameError.value = translate('forum', 'titleRequired')
    return
  }

  if (nicknameValue.value === profile.value?.nickname) {
    showNicknameEdit.value = false
    return
  }

  nicknameLoading.value = true
  nicknameError.value = ''

  const result = await forumService.updateNickname(nickname.value, nicknameValue.value.trim())
  nicknameLoading.value = false

  if (result.isSuccess) {
    if (profile.value) {
      profile.value.nickname = nicknameValue.value.trim()
    }
    showNicknameEdit.value = false
    toast.add({
      severity: 'success',
      summary: translate('forum', 'profile.nicknameUpdated'),
      life: 3000,
    })
  } else {
    nicknameError.value = String(result.error)
  }
}

// 打开帖子编辑页面
const openPostEdit = (post: PostSummary) => {
  router.push(`/forum/edit/${post.slug}`)
}

// 确认删除
const confirmDelete = (type: 'post' | 'comment', id: string, title?: string) => {
  deleteTarget.value = { type, id, title }
  showDeleteConfirm.value = true
}

// 执行删除
async function executeDelete() {
  if (!deleteTarget.value) return

  const { type, id } = deleteTarget.value

  if (type === 'post') {
    const result = await forumService.deletePost(id)
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: translate('forum', 'admin.deleteSuccess'), life: 3000 })
      loadProfile()
    } else {
      toast.add({ severity: 'error', summary: String(result.error), life: 3000 })
    }
  } else {
    const result = await forumService.deleteOwnComment(id)
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: translate('forum', 'admin.deleteSuccess'), life: 3000 })
      loadComments()
      if (profile.value) profile.value.commentCount--
    } else {
      toast.add({ severity: 'error', summary: String(result.error), life: 3000 })
    }
  }

  showDeleteConfirm.value = false
  deleteTarget.value = null
}

// 切换标签
const switchTab = (tab: 'posts' | 'comments') => {
  activeTab.value = tab
  // 切换时重置搜索和筛选
  searchQuery.value = ''
  selectedCategory.value = null
  if (tab === 'comments' && comments.value.length === 0) {
    loadComments()
  }
}

onMounted(() => {
  loadProfile()
})

watch(nickname, () => {
  loadProfile()
  comments.value = []
  activeTab.value = 'posts'
  searchQuery.value = ''
  selectedCategory.value = null
})

// 头像加载
watch(() => profile.value?.playerId, (playerId) => {
  if (playerId) {
    loadAvatar()
  }
}, { immediate: true })
</script>

<template>
  <div class="page-container">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="state-empty">
      <i class="pi pi-spin pi-spinner"></i>
      <span>{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="state-empty">
      <i class="pi pi-exclamation-triangle state-icon--error"></i>
      <span>{{ error }}</span>
      <PrimeButton :label="translate('forum', 'retry')" class="bf-btn" @click="loadProfile" />
    </div>

    <!-- 成员资料 -->
    <div v-else-if="profile" class="profile-wrapper">

      <!-- ====== Banner ====== -->
      <div class="profile-banner">
        <div class="banner-glow"></div>
      </div>

      <!-- ====== 资料卡片 ====== -->
      <div class="profile-card bf-card">
        <!-- 头像 -->
        <div class="profile-avatar" @click="goToPlayer">
          <img
            v-if="avatarUrl"
            :src="avatarUrl"
            :alt="profile.nickname"
            class="avatar-img"
          />
          <div v-else class="avatar-placeholder">
            {{ profile.nickname.charAt(0).toUpperCase() }}
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="profile-info">
          <!-- 昵称行 -->
          <div class="info-header">
            <div class="name-group">
              <h1 class="profile-name">{{ profile.nickname }}</h1>
              <span v-if="profile.isBanned" class="bf-badge bf-badge--hidden">
                {{ translate('forum', 'admin.hidden') }}
              </span>
              <button v-if="isOwner" class="action-btn action-btn--edit name-edit-btn" @click="openNicknameEdit" :title="translate('forum', 'profile.editNickname')">
                <span class="action-icon">✏</span>
              </button>
            </div>
            <div class="level-badge">
              <span>Lv.{{ level }}</span>
            </div>
          </div>

          <!-- XP 进度 -->
          <div class="xp-row">
            <div class="xp-bar">
              <div class="xp-fill" :style="{ width: levelProgress + '%' }"></div>
            </div>
            <span class="xp-text">{{ levelProgress }} / 100 XP</span>
          </div>

          <!-- 统计 -->
          <div class="stat-row">
            <div class="stat-item">
              <span class="stat-num">{{ profile.reputation }}</span>
              <span class="stat-label">{{ translate('forum', 'profile.reputation') }}</span>
            </div>
            <div class="stat-div"></div>
            <div class="stat-item">
              <span class="stat-num">{{ profile.totalPosts }}</span>
              <span class="stat-label">{{ translate('forum', 'profile.posts') }}</span>
            </div>
            <div class="stat-div"></div>
            <div class="stat-item">
              <span class="stat-num">{{ profile.commentCount }}</span>
              <span class="stat-label">{{ translate('forum', 'profile.comments') }}</span>
            </div>
          </div>

          <!-- 元信息 -->
          <div class="meta-row">
            <span v-if="profile.joinedAt" class="meta-item">
              <i class="pi pi-calendar"></i>
              {{ translate('forum', 'profile.joinedAt') }} {{ formatDate(profile.joinedAt) }}
            </span>
            <button v-if="profile.playerId" class="bf-btn bf-btn--ghost meta-btn" @click="goToPlayer">
              <i class="pi pi-external-link"></i>
              {{ translate('forum', 'profile.viewPlayerStats') }}
            </button>
          </div>
        </div>
      </div>

      <!-- ====== 标签切换 ====== -->
      <div class="tabs-bar">
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'posts' }"
          @click="switchTab('posts')"
        >
          {{ translate('forum', 'profile.posts') }}
          <span class="tab-count">{{ profile.totalPosts }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'comments' }"
          @click="switchTab('comments')"
        >
          {{ translate('forum', 'profile.comments') }}
          <span class="tab-count">{{ profile.commentCount }}</span>
        </button>
      </div>

      <!-- ====== 帖子列表 ====== -->
      <div v-if="activeTab === 'posts'" class="tab-content">

        <!-- 搜索栏 -->
        <div class="bf-search-bar">
          <div class="bf-search-input-wrapper">
            <svg class="bf-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" stroke-linecap="round" />
            </svg>
            <input
              v-model="searchQuery"
              class="bf-search-input"
              :placeholder="translate('forum', 'searchPosts')"
              @keydown.enter="() => {}"
            />
            <button v-if="searchQuery" class="bf-search-clear" @click="clearSearch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6 6 18M6 6l12 12" stroke-linecap="round" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 分类筛选 -->
        <div class="bf-categories">
          <button
            v-for="(cat, idx) in categories"
            :key="cat.id ?? `cat-all-${idx}`"
            class="bf-category-btn"
            :class="{ 'bf-category-btn--active': selectedCategory === cat.id }"
            @click="setCategory(cat.id)"
          >
            {{ cat.label }}
          </button>
        </div>

        <!-- 列表 -->
        <div class="content-list">
          <div v-if="filteredPosts.length === 0" class="state-empty">
            <i class="pi pi-inbox state-icon"></i>
            <span>{{ translate('forum', 'profile.noPosts') }}</span>
          </div>

          <article
            v-for="post in filteredPosts"
          :key="post.postId"
          class="post-card bf-card"
          @click="goToPost(post.slug)"
        >
          <!-- 顶部火焰条 -->
          <div class="card-fire-bar"></div>

          <div class="card-body">
            <!-- 标题区 -->
            <div class="card-header">
              <div class="card-badges">
                <span v-if="post.isPinned" class="bf-badge bf-badge--pinned">
                  <i class="pi pi-star-fill"></i>
                </span>
                <span v-if="post.isFeatured" class="bf-badge bf-badge--featured">
                  <i class="pi pi-crown"></i>
                </span>
                <span class="bf-badge">{{ translateCategory(post.category) }}</span>
              </div>
              <h2 class="card-title">{{ post.title }}</h2>
            </div>

            <!-- 底部统计 -->
            <div class="card-footer">
              <div class="card-stats">
                <span class="card-stat">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
                  </svg>
                  {{ post.points }}
                </span>
                <span class="card-stat">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  {{ post.numComments }}
                </span>
                <span class="card-time">
                  <i class="pi pi-clock"></i>
                  {{ formatRelativeTime(post.createdAt) }}
                </span>
              </div>

              <!-- 所有者操作 -->
              <div v-if="isOwner" class="card-actions" @click.stop>
                <button class="action-btn action-btn--edit" @click="openPostEdit(post)" :title="translate('forum', 'profile.editPost')">
                  <span class="action-icon">■</span>
                </button>
                <button class="action-btn action-btn--delete" @click="confirmDelete('post', post.postId, post.title)" :title="translate('forum', 'profile.deletePost')">
                  <span class="action-icon">✕</span>
                </button>
              </div>
            </div>
          </div>
        </article>
        </div>
      </div>

      <!-- ====== 评论列表 ====== -->
      <div v-if="activeTab === 'comments'" class="tab-content">
        <div class="content-list">
        <div v-if="commentsLoading" class="state-empty">
          <i class="pi pi-spin pi-spinner"></i>
        </div>

        <div v-else-if="comments.length === 0" class="state-empty">
          <i class="pi pi-inbox state-icon"></i>
          <span>{{ translate('forum', 'profile.noComments') || '暂无评论' }}</span>
        </div>

        <article
          v-for="c in comments"
          :key="c.commentId"
          class="post-card bf-card"
          @click="goToPost(c.postSlug)"
        >
          <div class="card-fire-bar"></div>
          <div class="card-body">
            <div class="card-header">
              <div class="reply-to">
                <i class="pi pi-reply"></i>
                {{ c.postTitle }}
              </div>
              <p class="card-content">{{ c.content }}</p>
            </div>
            <div class="card-footer">
              <div class="card-stats">
                <span class="card-stat">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
                  </svg>
                  {{ c.points }}
                </span>
                <span class="card-time">
                  <i class="pi pi-clock"></i>
                  {{ formatRelativeTime(c.createdAt) }}
                </span>
              </div>
              <div v-if="isOwner" class="card-actions" @click.stop>
                <button class="action-btn action-btn--delete" @click="confirmDelete('comment', c.commentId)" :title="translate('forum', 'profile.deleteComment')">
                  <span class="action-icon">✕</span>
                </button>
              </div>
            </div>
          </div>
        </article>
        </div>
      </div>
    </div>

    <!-- ====== 昵称编辑弹窗 ====== -->
    <PrimeDialog
      v-model:visible="showNicknameEdit"
      :modal="true"
      :style="{ width: '400px' }"
      :closable="!nicknameLoading"
    >
      <template #header>
        <span class="dialog-header-text">{{ translate('forum', 'profile.editNickname') }}</span>
      </template>
      <div class="dialog-body">
        <label class="dialog-label">{{ translate('forum', 'profile.newNickname') }}</label>
        <InputText
          v-model="nicknameValue"
          class="bf-input dialog-input"
          :disabled="nicknameLoading"
          :placeholder="translate('forum', 'enterTitle')"
          @keyup.enter="saveNickname"
        />
        <small v-if="nicknameError" class="dialog-error">{{ nicknameError }}</small>
      </div>
      <template #footer>
        <PrimeButton
          :label="translate('forum', 'cancel')"
          severity="secondary"
          :disabled="nicknameLoading"
          @click="showNicknameEdit = false"
        />
        <PrimeButton
          :label="translate('forum', 'save')"
          :loading="nicknameLoading"
          @click="saveNickname"
        />
      </template>
    </PrimeDialog>

    <!-- ====== 删除确认弹窗 ====== -->
    <PrimeDialog
      v-model:visible="showDeleteConfirm"
      :modal="true"
      :style="{ width: '360px' }"
    >
      <template #header>
        <span class="dialog-header-text">{{ translate('forum', 'profile.confirmDelete') }}</span>
      </template>
      <div class="dialog-body">
        <p class="dialog-confirm-text">
          {{ deleteTarget?.type === 'post' ? translate('forum', 'profile.confirmDeletePost') : translate('forum', 'profile.confirmDeleteComment') }}
          <span v-if="deleteTarget?.type === 'post' && deleteTarget?.title" class="dialog-confirm-target">"{{ deleteTarget.title }}"</span>
        </p>
      </div>
      <template #footer>
        <PrimeButton
          :label="translate('forum', 'cancel')"
          severity="secondary"
          @click="showDeleteConfirm = false"
        />
        <PrimeButton
          :label="translate('forum', 'profile.delete')"
          severity="danger"
          @click="executeDelete"
        />
      </template>
    </PrimeDialog>
  </div>
</template>

<style scoped>
/* ====== 页面容器 ====== */
.page-container {
  min-height: 100vh;
  padding-top: 5rem;
  background: hsl(var(--background));
  color: hsl(var(--foreground));
}

.profile-wrapper {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 1rem 3rem;
}

/* ====== Banner ====== */
.profile-banner {
  position: relative;
  height: 180px;
  margin: 0 -1rem;
  overflow: hidden;
}

.banner-glow {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 70% 60% at 50% 100%, var(--bf-primary-glow, rgba(255, 107, 53, 0.2)) 0%, transparent 70%),
    linear-gradient(180deg, transparent 0%, hsl(var(--background)) 100%);
}

/* ====== 资料卡片 ====== */
.profile-card {
  position: relative;
  margin-top: -90px;
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  z-index: 10;
}

.profile-card::before {
  display: none;
}

/* ====== 头像 ====== */
.profile-avatar {
  flex-shrink: 0;
  cursor: pointer;
}

.avatar-img {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  object-fit: cover;
  border: 3px solid var(--bf-card-bg);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  image-rendering: pixelated;
  transition: all 0.25s ease;
}

.avatar-img:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 28px rgba(255, 107, 53, 0.35);
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  background: var(--bf-surface);
  border: 3px solid var(--bf-card-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--bf-text-muted);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
}

/* ====== 用户信息 ====== */
.profile-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.name-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.profile-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--bf-text-primary);
  margin: 0;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px;
  border-radius: 100px;
  background: var(--bf-fire-gradient);
  color: white;
  font-weight: 700;
  font-size: 0.75rem;
  box-shadow: 0 2px 10px var(--bf-primary-glow);
}

/* XP 进度条 */
.xp-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.xp-bar {
  flex: 1;
  height: 8px;
  background: var(--bf-surface);
  border-radius: 100px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.xp-fill {
  height: 100%;
  background: var(--bf-fire-gradient);
  border-radius: 100px;
  transition: width 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 0 8px var(--bf-primary-glow);
}

.xp-text {
  font-size: 0.7rem;
  color: var(--bf-text-muted);
  font-weight: 500;
  white-space: nowrap;
}

/* 统计行 */
.stat-row {
  display: flex;
  align-items: center;
  background: var(--bf-surface);
  border-radius: var(--bf-card-radius-sm);
  padding: 0.5rem 0;
  border: 1px solid var(--bf-border-default);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--bf-primary);
}

.stat-label {
  font-size: 0.65rem;
  color: var(--bf-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-div {
  width: 1px;
  height: 32px;
  background: var(--bf-border-default);
}

/* 元信息 */
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: var(--bf-text-muted);
}

.meta-item i {
  font-size: 0.75rem;
}

.meta-btn {
  font-size: 0.8rem;
}

/* ====== 标签切换 ====== */
.tab-content {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ====== 搜索栏 ====== */
.bf-search-bar {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
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

/* ====== 分类标签 ====== */
.bf-categories {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  flex-wrap: wrap;
  padding: var(--bf-space-md, 16px);
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
.tabs-bar {
  display: flex;
  gap: 0.25rem;
  margin-top: 2rem;
  border-bottom: 1px solid var(--bf-border-default);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--bf-text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: var(--bf-text-primary);
}

.tab-btn--active {
  color: var(--bf-primary);
  border-bottom-color: var(--bf-primary);
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: var(--bf-surface);
  border-radius: 100px;
  font-size: 0.7rem;
  font-weight: 600;
}

.tab-btn--active .tab-count {
  background: var(--bf-fire-gradient-subtle);
  color: var(--bf-primary);
}

/* ====== 内容列表 ====== */
.content-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ====== 帖子/评论卡片 ====== */
.post-card {
  position: relative;
  padding: 0;
  cursor: pointer;
  overflow: hidden;
}

.post-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--bf-fire-gradient);
  opacity: 0;
  transition: opacity 0.15s ease;
}

.post-card:hover::before {
  opacity: 1;
}

.post-card:hover {
  border-color: var(--bf-border-accent);
  box-shadow: var(--bf-card-shadow-hover);
  transform: translateY(-2px);
}

.card-body {
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* 卡片头部 */
.card-header {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.card-badges {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.bf-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  background: var(--bf-fire-gradient-subtle);
  border: 1px solid var(--bf-border-accent);
  border-radius: 100px;
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--bf-primary);
  text-transform: uppercase;
}

.bf-badge i {
  font-size: 8px;
}

.bf-badge--pinned {
  background: var(--bf-primary);
  color: white;
  border-color: var(--bf-primary);
}

.bf-badge--featured {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  border-color: transparent;
}

.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.reply-to {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: var(--bf-primary);
}

.reply-to i {
  font-size: 0.75rem;
}

.card-content {
  font-size: 0.85rem;
  color: var(--bf-text-secondary);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.card-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-stat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.8rem;
  color: var(--bf-text-muted);
}

.card-stat svg {
  width: 14px;
  height: 14px;
}

.card-time {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.75rem;
  color: var(--bf-text-muted);
}

.card-time i {
  font-size: 0.7rem;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 0.25rem;
}

/* 通用操作按钮 - 适配亮/暗模式 */
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid var(--bf-border-default);
  background: var(--bf-bg-elevated);
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.action-icon {
  font-size: 14px;
  line-height: 1;
  font-weight: 400;
}

/* 编辑按钮 */
.action-btn--edit {
  color: var(--bf-primary);
  border-color: var(--bf-border-accent);
  background: var(--bf-fire-gradient-subtle);
}

.action-btn--edit:hover {
  background: var(--bf-fire-gradient);
  color: white;
  border-color: var(--bf-primary);
  transform: scale(1.05);
  box-shadow: 0 2px 8px var(--bf-primary-glow);
}

/* 删除按钮 */
.action-btn--delete {
  color: var(--bf-text-secondary);
  border-color: var(--bf-border-default);
  background: var(--bf-bg-elevated);
}

.action-btn--delete:hover {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.4);
  transform: scale(1.05);
}

/* 名片编辑按钮 - 悬浮时显示 */
.name-edit-btn {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.profile-card:hover .name-edit-btn {
  opacity: 1;
}

/* ====== 空状态 ====== */
.state-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 4rem 1rem;
  color: var(--bf-text-muted);
}

.state-icon {
  font-size: 3rem;
  opacity: 0.4;
}

.state-icon--error {
  color: var(--bf-danger);
}

/* ====== 弹窗 ====== */
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.5rem 0;
}

.dialog-label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--bf-text-secondary);
}

.dialog-header-text {
  font-size: 1rem;
  font-weight: 600;
  color: var(--bf-text-primary);
}

.dialog-input {
  width: 100%;
}

.dialog-error {
  color: var(--bf-danger);
  font-size: 0.8rem;
}

.dialog-confirm-text {
  margin: 0;
  font-size: 0.9rem;
  color: var(--bf-text-primary);
  line-height: 1.5;
}

.dialog-confirm-target {
  color: var(--bf-primary);
  font-weight: 500;
}

/* ====== 按钮 ====== */
.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  padding: 0.5rem 1rem;
  border-radius: var(--bf-btn-radius);
  font-weight: 500;
  font-size: 0.85rem;
  transition: all 0.15s ease;
  cursor: pointer;
  border: none;
  outline: none;
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

.bf-btn--ghost {
  background: transparent;
  color: var(--bf-text-secondary);
}

.bf-btn--ghost:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-primary);
}

.bf-btn--sm {
  padding: 0.35rem 0.6rem;
  font-size: 0.75rem;
}

.bf-btn--danger {
  color: var(--bf-danger);
}

.bf-btn--danger:hover {
  background: var(--bf-danger-bg);
  color: var(--bf-danger);
}

/* ====== 响应式 ====== */
@media (max-width: 640px) {
  .profile-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-top: -50px;
  }

  .profile-info {
    align-items: center;
  }

  .info-header {
    justify-content: center;
  }

  .xp-row {
    width: 100%;
    flex-direction: column;
    gap: 0.25rem;
  }

  .xp-bar {
    width: 100%;
  }

  .stat-row {
    width: 100%;
  }

  .meta-row {
    justify-content: center;
  }

  .tabs-bar {
    justify-content: center;
  }

  .post-card:hover {
    transform: none;
  }
}
</style>
