<script lang="ts" setup>
import { computed } from 'vue'
import type { PostDto } from '@M/forum/dtos/Post.ts'
import { getIsPinned, getIsFeatured, getIsHidden } from '@M/forum/dtos/Post.ts'

const props = defineProps<{
  post: PostDto
  authorAvatar: string | null
}>()

// 获取作者首字母
function getAuthorInitial(post: PostDto | null): string {
  if (!post) return 'U'
  const nickname = post.memberPostBy?.nickname
  if (!nickname) return 'U'
  return nickname.charAt(0).toUpperCase()
}

// 格式化日期
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
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
})
</script>

<template>
  <div class="bf-post-header">
    <!-- 标题 + 徽章 -->
    <div class="bf-title-row">
      <h1 class="bf-post-title">{{ post.title }}</h1>
      <div class="bf-post-badges">
        <span v-if="getIsFeatured(post)" class="bf-badge bf-badge--featured" title="精华">★</span>
        <span v-if="getIsPinned(post)" class="bf-badge bf-badge--pinned" title="置顶">◆</span>
        <span v-if="getIsHidden(post)" class="bf-badge bf-badge--hidden" title="已隐藏">◉</span>
        <span v-if="post.type === 'LINK'" class="bf-badge bf-badge--link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
        </span>
      </div>
    </div>

    <!-- 作者信息 -->
    <div class="bf-author-row">
      <div class="bf-author-avatar-wrapper">
        <img
          v-if="authorAvatar"
          :src="authorAvatar"
          class="bf-author-avatar bf-author-avatar--img"
          alt=""
        />
        <div v-else class="bf-author-avatar">
          {{ getAuthorInitial(post) }}
        </div>
      </div>
      <div class="bf-author-info">
        <span class="bf-author-name">{{ post.memberPostBy?.nickname || 'Unknown' }}</span>
        <span class="bf-meta-rep">
          <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12">
            <path d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
          </svg>
          {{ post.memberPostBy?.reputation || 0 }}
        </span>
      </div>
      <span class="bf-post-time">{{ formattedDate }}</span>
    </div>
  </div>
</template>

<style src="@M/forum/assets/forum-shared.css" scoped>
.bf-post-header {
  margin-bottom: var(--bf-space-lg, 24px);
}

/* 标题 + 徽章行 */
.bf-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.bf-post-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--bf-text-primary, #ffffff);
  margin: 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

/* 徽章 */
.bf-post-badges {
  display: inline-flex;
  gap: 6px;
  flex-shrink: 0;
}

/* 作者信息行 */
.bf-author-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bf-author-avatar-wrapper {
  flex-shrink: 0;
}

.bf-author-avatar {
  width: 36px;
  height: 36px;
  font-size: 13px;
}

.bf-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bf-post-time {
  margin-left: auto;
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-post-title {
    font-size: 1.375rem;
  }

  .bf-author-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  .bf-author-name {
    font-size: 13px;
  }
}
</style>
