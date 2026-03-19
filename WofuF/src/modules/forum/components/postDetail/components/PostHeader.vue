<script lang="ts" setup>
import { computed } from 'vue'
import type { PostDto } from '@M/forum/dtos/Post.ts'

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

// 格式化信誉值
const reputationDisplay = computed(() => {
  const rep = props.post.memberPostBy?.reputation || 0
  if (rep >= 10000) return `${(rep / 1000).toFixed(1)}k`
  return rep.toString()
})

// 信誉等级
const reputationLevel = computed(() => {
  const rep = props.post.memberPostBy?.reputation || 0
  if (rep >= 10000) return 'legendary'
  if (rep >= 5000) return 'expert'
  if (rep >= 1000) return 'trusted'
  if (rep >= 100) return 'member'
  return 'newcomer'
})
</script>

<template>
  <div class="bf-post-header">
    <!-- 标题 -->
    <h1 class="bf-post-title">{{ post.title }}</h1>

    <!-- 元信息卡片 -->
    <div class="bf-post-meta-card">
      <!-- 作者信息 -->
      <div class="bf-author-section">
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
          <div class="bf-author-name-row">
            <span class="bf-author-name">{{ post.memberPostBy?.nickname || 'Unknown' }}</span>
            <span
              class="bf-reputation-badge"
              :class="`bf-reputation--${reputationLevel}`"
            >
              <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
              {{ reputationDisplay }}
            </span>
          </div>
          <div class="bf-meta-row">
            <span class="bf-post-time">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              {{ formattedDate }}
            </span>
            <span class="bf-meta-divider">•</span>
            <span class="bf-post-comments">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              {{ post.numComments }} 评论
            </span>
            <span class="bf-meta-divider">•</span>
            <span class="bf-post-points">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
              </svg>
              {{ post.points }} 赞
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bf-post-header {
  margin-bottom: var(--bf-space-lg, 24px);
}

/* 标题样式 */
.bf-post-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--bf-text-primary, #ffffff);
  margin: 0 0 var(--bf-space-lg, 24px);
  line-height: 1.3;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, var(--bf-text-primary) 0%, var(--bf-text-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 元信息卡片 */
.bf-post-meta-card {
  display: flex;
  align-items: flex-start;
  padding: var(--bf-space-md, 16px);
  background: var(--bf-surface, rgba(255, 255, 255, 0.03));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius-sm, 12px);
  gap: var(--bf-space-md, 16px);
}

/* 作者区域 */
.bf-author-section {
  display: flex;
  align-items: flex-start;
  gap: var(--bf-space-md, 16px);
  flex: 1;
}

.bf-author-avatar-wrapper {
  flex-shrink: 0;
}

.bf-author-avatar {
  width: 48px;
  height: 48px;
  background: var(
    --bf-fire-gradient,
    linear-gradient(135deg, #ff6b35 0%, #ff9f1c 50%, #ffbe0b 100%)
  );
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 700;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.bf-author-avatar--img {
  background: transparent;
  border-radius: 12px;
  image-rendering: pixelated;
  box-shadow: none;
}

/* 作者信息 */
.bf-author-info {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-xs, 4px);
  min-width: 0;
}

.bf-author-name-row {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  flex-wrap: wrap;
}

.bf-author-name {
  color: var(--bf-text-primary, #ffffff);
  font-weight: 600;
  font-size: 1rem;
}

/* 信誉徽章 */
.bf-reputation-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 600;
}

.bf-reputation--newcomer {
  background: rgba(156, 163, 175, 0.2);
  color: #9ca3af;
}

.bf-reputation--member {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.bf-reputation--trusted {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.bf-reputation--expert {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.bf-reputation--legendary {
  background: rgba(255, 107, 53, 0.2);
  color: var(--bf-primary, #ff6b35);
}

/* 元数据行 */
.bf-meta-row {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  flex-wrap: wrap;
}

.bf-post-time,
.bf-post-comments,
.bf-post-points {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--bf-text-muted, #666666);
  font-size: 0.8125rem;
}

.bf-post-time svg,
.bf-post-comments svg,
.bf-post-points svg {
  opacity: 0.6;
}

.bf-meta-divider {
  color: var(--bf-text-muted, #666666);
  opacity: 0.4;
  margin: 0 2px;
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-post-title {
    font-size: 1.375rem;
  }

  .bf-post-meta-card {
    padding: var(--bf-space-sm, 12px);
  }

  .bf-author-avatar {
    width: 40px;
    height: 40px;
    font-size: 1rem;
    border-radius: 10px;
  }

  .bf-author-avatar--img {
    border-radius: 10px;
  }

  .bf-author-name {
    font-size: 0.9375rem;
  }

  .bf-meta-row {
    gap: 2px;
  }

  .bf-post-time,
  .bf-post-comments,
  .bf-post-points {
    font-size: 0.75rem;
  }
}
</style>
