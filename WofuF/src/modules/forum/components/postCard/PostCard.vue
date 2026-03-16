/**
 * 帖子卡片组件
 * 显示单个帖子信息，支持投票、点击跳转
 */

<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post'
import { translate } from '@S/services/i18n'
import { authService } from '@M/auth/services/AuthService'
import Tag from 'primevue/tag'

const props = defineProps<{
  post: PostDto
}>()

const emit = defineEmits<{
  (e: 'upvote', post: PostDto): void
  (e: 'downvote', post: PostDto): void
}>()

const router = useRouter()

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 格式化时间
const formattedDate = computed(() => {
  const date = new Date(props.post.createdAt)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`

  return date.toLocaleDateString()
})

// 投票状态
const voteState = computed(() => {
  if (props.post.wasUpvotedByMe) return 'upvoted'
  if (props.post.wasDownvotedByMe) return 'downvoted'
  return 'none'
})

// 跳转到帖子详情
const goToPost = () => {
  router.push(`/forum/posts/${props.post.slug}`)
}

// 处理投票
const handleUpvote = (event: Event) => {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  emit('upvote', props.post)
}

const handleDownvote = (event: Event) => {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  emit('downvote', props.post)
}

// 访问链接
const visitLink = (event: Event) => {
  event.stopPropagation()
  if (props.post.link) {
    window.open(props.post.link, '_blank', 'noopener,noreferrer')
  }
}
</script>

<template>
  <article
    class="post-card group cursor-pointer"
    @click="goToPost"
    tabindex="0"
    @keydown.enter="goToPost"
  >
    <!-- 投票区域 -->
    <div class="vote-section">
      <button
        class="vote-btn upvote"
        :class="{ active: voteState === 'upvoted' }"
        :title="translate('forum', 'upvote')"
        @click="handleUpvote"
        :aria-label="translate('forum', 'upvote')"
      >
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path
            d="M10 3.5l-6.5 8h4v5h5v-5h4l-6.5-8z"
            :class="{ 'text-orange-500': voteState === 'upvoted' }"
          />
        </svg>
      </button>
      <span class="vote-count" :class="{ positive: post.points > 0, negative: post.points < 0 }">
        {{ post.points }}
      </span>
      <button
        class="vote-btn downvote"
        :class="{ active: voteState === 'downvoted' }"
        :title="translate('forum', 'downvote')"
        @click="handleDownvote"
        :aria-label="translate('forum', 'downvote')"
      >
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path
            d="M10 16.5l6.5-8h-4v-5h-5v5h-4l6.5 8z"
            :class="{ 'text-violet-500': voteState === 'downvoted' }"
          />
        </svg>
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 元信息 -->
      <div class="post-meta">
        <span class="author">
          {{ translate('forum', 'by') }}
          <span class="author-name">{{ post.memberPostBy.nickname }}</span>
        </span>
        <span class="separator">·</span>
        <span class="time">{{ formattedDate }}</span>
        <Tag
          v-if="post.type === 'LINK'"
          value="Link"
          severity="info"
          class="link-tag"
        />
      </div>

      <!-- 标题 -->
      <h2 class="post-title">
        {{ post.title }}
      </h2>

      <!-- 内容预览 -->
      <p v-if="post.type === 'TEXT' && post.text" class="post-preview">
        {{ post.text.slice(0, 200) }}{{ post.text.length > 200 ? '...' : '' }}
      </p>

      <!-- 链接预览 -->
      <div v-if="post.type === 'LINK' && post.link" class="link-preview" @click="visitLink">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
          />
        </svg>
        <span class="link-url">{{ post.link }}</span>
      </div>

      <!-- 底部统计 -->
      <div class="post-stats">
        <span class="stat-item">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
          {{ post.numComments }} {{ translate('forum', 'comments') }}
        </span>
        <span class="stat-item reputation">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
            />
          </svg>
          {{ post.memberPostBy.reputation }}
        </span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.post-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: var(--w-surface-card);
  border-radius: var(--w-radius-lg);
  border: 1px solid var(--w-border);
  transition: all var(--w-transition-normal);
  box-shadow: var(--w-shadow-sm);
}

.post-card:hover {
  border-color: var(--w-primary);
  box-shadow: var(--w-shadow-md);
  transform: translateY(-1px);
}

.post-card:focus {
  outline: 2px solid var(--w-primary);
  outline-offset: 2px;
}

/* 暗色模式增强 */
:global(.dark) .post-card {
  background: #1c1c1e;
  border-color: #3a3a3c;
}

:global(.dark) .post-card:hover {
  border-color: var(--w-primary);
}

/* 投票区域 */
.vote-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  min-width: 2.5rem;
}

.vote-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: var(--w-radius-sm);
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--w-text-muted);
  transition: all var(--w-transition-fast);
}

.vote-btn:hover {
  background: var(--w-surface-hover);
  color: var(--w-text);
}

.vote-btn.active {
  background: rgba(var(--w-primary-rgb, 59, 130, 246), 0.1);
}

:global(.dark) .vote-btn:hover {
  background: #2c2c2e;
}

.vote-count {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--w-text-muted);
}

.vote-count.positive {
  color: var(--w-vote-up);
}

.vote-count.negative {
  color: var(--w-vote-down);
}

/* 内容区域 */
.content-section {
  flex: 1;
  min-width: 0;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--w-text-muted);
  margin-bottom: 0.5rem;
}

.author-name {
  font-weight: 500;
  color: var(--w-text);
}

.separator {
  color: var(--w-text-muted);
}

.link-tag {
  font-size: 0.625rem;
  padding: 0.125rem 0.375rem;
}

.post-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--w-text);
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.group:hover .post-title {
  color: var(--w-primary);
}

.post-preview {
  font-size: 0.875rem;
  color: var(--w-text-muted);
  line-height: 1.5;
  margin: 0 0 0.75rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.link-preview {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--w-surface);
  border-radius: var(--w-radius-sm);
  font-size: 0.875rem;
  color: var(--w-primary);
  margin-bottom: 0.75rem;
  transition: background var(--w-transition-fast);
}

.link-preview:hover {
  background: var(--w-surface-hover);
}

:global(.dark) .link-preview {
  background: #2c2c2e;
}

:global(.dark) .link-preview:hover {
  background: #3a3a3c;
}

.link-url {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底部统计 */
.post-stats {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.75rem;
  color: var(--w-text-muted);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.stat-item.reputation {
  color: var(--w-reputation);
}

/* 响应式 */
@media (max-width: 640px) {
  .post-card {
    padding: 0.75rem;
  }

  .vote-section {
    min-width: 2rem;
  }

  .post-title {
    font-size: 1rem;
  }

  .link-url {
    max-width: 200px;
  }
}
</style>
