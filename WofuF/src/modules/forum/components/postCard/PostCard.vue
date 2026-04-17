/**
 * 帖子卡片组件 - 增强版
 * 显示单个帖子信息，支持投票、点击跳转
 */

<script lang="ts" setup>
import { computed, watch, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post'
import { getIsPinned, getIsFeatured, getIsHidden } from '@M/forum/dtos/Post'
import { translate } from '@S/services/i18n'
import { authService } from '@M/auth/services/AuthService'
import { PlayerService } from '@M/players/services/PlayerService'

const playerService = new PlayerService()

const props = defineProps<{
  post: PostDto
}>()

const emit = defineEmits<{
  (e: 'upvote', post: PostDto): void
  (e: 'downvote', post: PostDto): void
  (e: 'unvote', post: PostDto): void
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

// 投票数字颜色
const voteColor = computed(() => {
  if (props.post.points > 0) return 'var(--bf-primary)'
  if (props.post.points < 0) return '#8B5CF6'
  return 'var(--bf-text-muted)'
})

// 跳转到帖子详情
const goToPost = () => {
  router.push(`/forum/posts/${props.post.slug}`)
}

// 跳转到成员资料页
const goToMemberProfile = (event: Event) => {
  event.stopPropagation()
  const nickname = props.post.memberPostBy?.nickname
  if (nickname) {
    router.push(`/forum/members/${nickname}`)
  }
}

// 处理投票
const handleUpvote = (event: Event) => {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  // 如果已经点赞，则取消点赞
  if (voteState.value === 'upvoted') {
    emit('unvote', props.post)
  } else {
    emit('upvote', props.post)
  }
}

const handleDownvote = (event: Event) => {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  // 如果已经点踩，则取消点踩
  if (voteState.value === 'downvoted') {
    emit('unvote', props.post)
  } else {
    emit('downvote', props.post)
  }
}

// 访问链接
const visitLink = (event: Event) => {
  event.stopPropagation()
  if (props.post.link) {
    window.open(props.post.link, '_blank', 'noopener,noreferrer')
  }
}

// 头像相关
const avatarUrl = ref<string | null>(null)

async function loadAvatar() {
  const playerId = props.post.memberPostBy?.playerId
  if (!playerId) return

  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        avatarUrl.value = await playerService.renderAvatar(skinData.skin, 32)
      }
    }
  } catch (e) {
    console.warn('[PostCard] Failed to load avatar:', e)
  }
}

// 监听 post 变化加载头像
watch(() => props.post.memberPostBy?.playerId, loadAvatar, { immediate: true })

// Markdown 预览 - 移除图片，只显示文字，限制长度
const previewText = computed(() => {
  if (!props.post.text) return ''

  // 移除图片标记
  let text = props.post.text.replace(/!\[.*?\]\(.*?\)/g, '[图片]')

  // 移除其他 Markdown 语法，保留纯文本
  text = text
    .replace(/^#{1,6}\s+/gm, '')      // 标题
    .replace(/\*\*(.+?)\*\*/g, '$1')   // 粗体
    .replace(/\*(.+?)\*/g, '$1')       // 斜体
    .replace(/~~(.+?)~~/g, '$1')       // 删除线
    .replace(/`(.+?)`/g, '$1')         // 行内代码
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')  // 链接
    .replace(/^[-*+]\s+/gm, '')        // 无序列表
    .replace(/^\d+\.\s+/gm, '')        // 有序列表
    .replace(/^>\s+/gm, '')            // 引用
    .replace(/\n+/g, ' ')              // 换行转空格
    .trim()

  // 限制长度
  if (text.length > 200) {
    return text.substring(0, 200) + '...'
  }
  return text
})

// 检查是否有图片
const hasImages = computed(() => {
  return props.post.text && /!\[.*?\]\(.*?\)/.test(props.post.text)
})

// 图片数量
const imageCount = computed(() => {
  if (!props.post.text) return 0
  const matches = props.post.text.match(/!\[.*?\]\(.*?\)/g)
  return matches ? matches.length : 0
})

// 提取第一张图片URL作为缩略图
const thumbnailUrl = computed(() => {
  if (!props.post.text) return null
  const match = props.post.text.match(/!\[.*?\]\((.*?)\)/)
  return match ? match[1] : null
})
</script>

<template>
  <article
    class="bf-post-card"
    @click="goToPost"
    tabindex="0"
    @keydown.enter="goToPost"
  >
    <!-- 左侧投票区 -->
    <div class="bf-post-card__vote">
      <button
        class="bf-vote-btn bf-vote-btn--up"
        :class="{ 'bf-vote-btn--active': voteState === 'upvoted' }"
        :title="translate('forum', 'upvote')"
        @click="handleUpvote"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
        </svg>
      </button>
      <span class="bf-vote-count" :style="{ color: voteColor }">
        {{ post.points }}
      </span>
      <button
        class="bf-vote-btn bf-vote-btn--down"
        :class="{ 'bf-vote-btn--active': voteState === 'downvoted' }"
        :title="translate('forum', 'downvote')"
        @click="handleDownvote"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
    </div>

    <!-- 右侧内容区 -->
    <div class="bf-post-card__content">
      <!-- 标题 + 徽章 + 日期 -->
      <div class="bf-post-card__header">
        <h2 class="bf-post-card__title">
          {{ post.title }}
        </h2>
        <div class="bf-post-card__badges">
          <span v-if="getIsFeatured(post)" class="bf-badge bf-badge--featured" title="精华">★</span>
          <span v-if="getIsPinned(post)" class="bf-badge bf-badge--pinned" title="置顶">◆</span>
          <span v-if="getIsHidden(post)" class="bf-badge bf-badge--hidden" title="已隐藏">◉</span>
          <span v-if="post.type === 'LINK'" class="bf-badge bf-badge--link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
            </svg>
          </span>
        </div>
        <span class="bf-meta-time">{{ formattedDate }}</span>
      </div>

      <!-- 作者信息 -->
      <div class="bf-post-card__author">
        <img
          v-if="avatarUrl"
          :src="avatarUrl"
          class="bf-avatar bf-avatar--img bf-clickable"
          alt=""
          @click="goToMemberProfile"
        />
        <div v-else class="bf-avatar bf-clickable" @click="goToMemberProfile">
          {{ post.memberPostBy.nickname.charAt(0).toUpperCase() }}
        </div>
        <span class="bf-author-name bf-clickable" @click="goToMemberProfile">{{ post.memberPostBy.nickname }}</span>
        <span class="bf-meta-rep">
          <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12">
            <path d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
          </svg>
          {{ post.memberPostBy.reputation }}
        </span>
      </div>

      <!-- 内容预览 -->
      <div v-if="post.type === 'TEXT' && (previewText || hasImages)" class="bf-post-card__preview-wrapper">
        <!-- 缩略图 -->
        <div v-if="thumbnailUrl" class="bf-post-card__thumbnail">
          <img :src="thumbnailUrl" :alt="'缩略图'" class="bf-thumbnail-img" loading="lazy" />
          <span v-if="imageCount > 1" class="bf-thumbnail-count">+{{ imageCount - 1 }}</span>
        </div>
        <p class="bf-post-card__preview">{{ previewText }}</p>
        <span v-if="hasImages && !thumbnailUrl" class="bf-image-indicator">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21,15 16,10 5,21"/>
          </svg>
          {{ imageCount }}
        </span>
      </div>

      <!-- 链接预览 -->
      <a v-if="post.type === 'LINK' && post.link" class="bf-link-preview" @click="visitLink">
        <div class="bf-link-preview__icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
          </svg>
        </div>
        <div class="bf-link-preview__content">
          <span class="bf-link-preview__url">{{ post.link }}</span>
          <span class="bf-link-preview__hint">点击访问</span>
        </div>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="bf-link-preview__arrow">
          <path stroke-linecap="round" stroke-linejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
        </svg>
      </a>

      <!-- 底部统计 -->
      <div class="bf-post-card__footer">
        <div class="bf-post-card__stats">
          <span class="bf-stat">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            {{ post.numComments }} 评论
          </span>
        </div>
        <div class="bf-post-card__actions">
          <button class="bf-action-btn" @click.stop="goToPost">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            查看
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
/* === 卡片容器 === */
.bf-post-card {
  display: flex;
  gap: var(--bf-space-lg, 20px);
  padding: var(--bf-space-lg, 24px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: var(--bf-card-shadow);
  cursor: pointer;
  transition: all var(--bf-transition-normal, 0.25s ease);
  position: relative;
  overflow: hidden;
}

.bf-post-card::before {
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

.bf-post-card:hover {
  border-color: var(--bf-border-accent);
  box-shadow: var(--bf-card-shadow-hover);
  transform: translateY(-2px);
}

.bf-post-card:hover::before {
  opacity: 1;
}

.bf-post-card:focus {
  outline: 2px solid var(--bf-primary);
  outline-offset: 2px;
}

/* === 投票区 === */
.bf-post-card__vote {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  min-width: 44px;
  padding-top: 4px;
}

.bf-vote-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--bf-text-muted);
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-vote-btn svg {
  width: 20px;
  height: 20px;
}

.bf-vote-btn:hover {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-secondary);
}

.bf-vote-btn--up:hover,
.bf-vote-btn--up.bf-vote-btn--active {
  color: var(--bf-primary);
  background: var(--bf-fire-gradient-subtle);
}

.bf-vote-btn--down:hover,
.bf-vote-btn--down.bf-vote-btn--active {
  color: #8B5CF6;
  background: rgba(139, 92, 246, 0.1);
}

.bf-vote-count {
  font-weight: 700;
  font-size: 15px;
  line-height: 1;
  padding: 4px 0;
}

/* === 内容区 === */
.bf-post-card__content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* === 标题 + 徽章 === */
.bf-post-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.bf-post-card__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--bf-text-primary);
  margin: 0;
  line-height: 1.4;
  transition: color var(--bf-transition-fast, 0.15s ease);
}

.bf-post-card:hover .bf-post-card__title {
  color: var(--bf-primary);
}

.bf-post-card__badges {
  display: inline-flex;
  gap: 4px;
}

.bf-meta-time {
  font-size: 12px;
  color: var(--bf-text-muted);
  margin-left: auto;
  flex-shrink: 0;
}

/* === 头像 === */
.bf-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--bf-fire-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.bf-avatar--img {
  background: var(--bf-input-bg);
  image-rendering: pixelated;
  padding: 2px;
}

/* === 徽章 === */
.bf-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: transparent;
  border: none;
  padding: 0;
}

.bf-badge svg {
  width: 12px;
  height: 12px;
}

.bf-badge--link {
  color: #3B82F6;
  opacity: 0.8;
}

.bf-badge--pinned {
  font-size: 20px;
  color: #3B82F6;
  text-shadow: 0 0 6px rgba(59, 130, 246, 0.8);
  filter: drop-shadow(0 0 3px rgba(59, 130, 246, 0.6));
}

.bf-badge--featured {
  font-size: 18px;
  color: #f59e0b;
  text-shadow: 0 0 6px rgba(245, 158, 11, 0.8);
  filter: drop-shadow(0 0 3px rgba(245, 158, 11, 0.6));
}

/* === 作者信息 === */
.bf-post-card__author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bf-author-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bf-author-name {
  font-weight: 600;
  color: var(--bf-text-secondary);
  font-size: 13px;
}

.bf-meta-rep {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #FFB800;
}

.bf-meta-rep svg {
  color: #FFB800;
}

/* === 可点击元素 === */
.bf-clickable {
  cursor: pointer;
  transition: opacity var(--bf-transition-fast, 0.15s ease);
}

.bf-clickable:hover {
  opacity: 0.7;
}

/* === 标题 + 徽章 === */
.bf-post-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.bf-post-card__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--bf-text-primary);
  margin: 0;
  line-height: 1.4;
  transition: color var(--bf-transition-fast, 0.15s ease);
}

.bf-post-card:hover .bf-post-card__title {
  color: var(--bf-primary);
}

.bf-post-card__badges {
  display: inline-flex;
  gap: 4px;
}

.bf-meta-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--bf-text-muted);
  flex-shrink: 0;
}

/* === 作者信息 === */
.bf-post-card__author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bf-author-name {
  font-weight: 600;
  color: var(--bf-text-secondary);
  font-size: 13px;
}

.bf-meta-rep {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #FFB800;
}

.bf-meta-rep svg {
  color: #FFB800;
}

/* === 内容预览 === */
.bf-post-card__preview-wrapper {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
}

.bf-post-card__preview {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--bf-text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bf-image-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  border-radius: 100px;
  font-size: 11px;
  font-weight: 500;
  color: #22C55E;
  flex-shrink: 0;
  white-space: nowrap;
}

.bf-image-indicator svg {
  width: 12px;
  height: 12px;
}

/* === 缩略图 === */
.bf-post-card__thumbnail {
  position: relative;
  width: 80px;
  height: 60px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.bf-thumbnail-img {
  display: block;
  width: 80px;
  height: 60px;
  object-fit: contain;
  border-radius: 12px;
  transition: transform 0.2s ease;
}

.bf-thumbnail-img:hover {
  transform: scale(1.05);
}

.bf-thumbnail-count {
  position: absolute;
  bottom: 2px;
  right: 2px;
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.65);
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: white;
}

/* === 链接预览 === */
.bf-link-preview {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
  padding: 12px 16px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 12px);
  text-decoration: none;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-link-preview:hover {
  background: var(--bf-btn-secondary-bg);
  border-color: var(--bf-border-accent);
}

.bf-link-preview__icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 10px;
  flex-shrink: 0;
}

.bf-link-preview__icon svg {
  width: 20px;
  height: 20px;
  color: #3B82F6;
}

.bf-link-preview__content {
  flex: 1;
  min-width: 0;
}

.bf-link-preview__url {
  display: block;
  font-size: 13px;
  color: var(--bf-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bf-link-preview__hint {
  display: block;
  font-size: 11px;
  color: var(--bf-text-muted);
  margin-top: 2px;
}

.bf-link-preview__arrow {
  width: 16px;
  height: 16px;
  color: var(--bf-text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: all var(--bf-transition-fast, 0.15s ease);
  flex-shrink: 0;
}

.bf-link-preview:hover .bf-link-preview__arrow {
  opacity: 1;
  transform: translateX(0);
  color: var(--bf-primary);
}

/* === 底部 === */
.bf-post-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--bf-space-md, 12px);
  padding-top: 8px;
  border-top: 1px solid var(--bf-border-default);
}

.bf-post-card__stats {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
}

.bf-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--bf-text-muted);
  transition: color var(--bf-transition-fast, 0.15s ease);
}

.bf-stat svg {
  width: 16px;
  height: 16px;
}

.bf-stat:hover {
  color: var(--bf-text-secondary);
}

.bf-post-card__actions {
  display: flex;
  gap: 8px;
}

.bf-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-action-btn svg {
  width: 14px;
  height: 14px;
}

.bf-action-btn:hover {
  background: var(--bf-fire-gradient-subtle);
  border-color: var(--bf-border-accent);
  color: var(--bf-primary);
}

/* === 响应式 === */
@media (max-width: 640px) {
  .bf-post-card {
    padding: 16px;
    gap: 14px;
  }

  .bf-post-card__vote {
    min-width: 36px;
  }

  .bf-vote-btn {
    width: 32px;
    height: 32px;
  }

  .bf-vote-btn svg {
    width: 18px;
    height: 18px;
  }

  .bf-post-card__title {
    font-size: 15px;
  }

  .bf-post-card__preview {
    font-size: 13px;
    -webkit-line-clamp: 2;
  }

  .bf-link-preview {
    padding: 10px 12px;
  }

  .bf-link-preview__icon {
    width: 32px;
    height: 32px;
  }

  .bf-link-preview__icon svg {
    width: 16px;
    height: 16px;
  }

  .bf-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  .bf-post-card__footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .bf-action-btn {
    width: 100%;
    justify-content: center;
    padding: 8px 12px;
  }
}
</style>
