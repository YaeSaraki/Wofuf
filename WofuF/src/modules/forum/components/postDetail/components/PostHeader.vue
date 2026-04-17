<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { PostDto } from '@M/forum/dtos/Post.ts'
import { getIsPinned, getIsFeatured, getIsHidden } from '@M/forum/dtos/Post.ts'

const props = defineProps<{
  post: PostDto
  authorAvatar: string | null
}>()

const router = useRouter()

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

// 跳转到成员资料页
function goToMemberProfile() {
  const nickname = props.post.memberPostBy?.nickname
  if (nickname) {
    router.push(`/forum/members/${nickname}`)
  }
}
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
          class="bf-author-avatar bf-author-avatar--img bf-clickable"
          alt=""
          @click="goToMemberProfile"
        />
        <div v-else class="bf-author-avatar bf-clickable" @click="goToMemberProfile">
          {{ getAuthorInitial(post) }}
        </div>
      </div>
      <div class="bf-author-info">
        <span class="bf-author-name bf-clickable" @click="goToMemberProfile">{{ post.memberPostBy?.nickname || 'Unknown' }}</span>
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

<style scoped>
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

.bf-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  padding: 0;
}

.bf-badge svg {
  width: 14px;
  height: 14px;
}

.bf-badge--pinned {
  font-size: 20px;
  color: #3B82F6;
  text-shadow: 0 0 6px rgba(59, 130, 246, 0.8);
  filter: drop-shadow(0 0 3px rgba(59, 130, 246, 0.6));
}

.bf-badge--featured {
  font-size: 20px;
  color: #f59e0b;
  text-shadow: 0 0 6px rgba(245, 158, 11, 0.8);
  filter: drop-shadow(0 0 3px rgba(245, 158, 11, 0.6));
}

.bf-badge--link {
  color: #3B82F6;
  opacity: 0.8;
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
  background: var(--bf-fire-gradient);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: white;
}

.bf-author-avatar--img {
  background: transparent;
  image-rendering: pixelated;
}

.bf-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bf-author-name {
  font-weight: 600;
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 14px;
}

/* 可点击元素 */
.bf-clickable {
  cursor: pointer;
  transition: opacity var(--bf-transition-fast, 0.15s ease);
}

.bf-clickable:hover {
  opacity: 0.7;
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

.bf-post-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--bf-text-muted, #666666);
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
