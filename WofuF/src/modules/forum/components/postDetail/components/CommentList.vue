<script lang="ts" setup>
import { ref, watch, reactive } from 'vue'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { PlayerService } from '@M/players/services/PlayerService'
import MarkdownRenderer from '@M/forum/components/shared/MarkdownRenderer.vue'
import { translate } from '@S/services/i18n'

const props = defineProps<{
  comments: CommentDto[]
}>()

const playerService = new PlayerService()

// 评论头像
const commentAvatars = reactive(new Map<string, string>())

async function loadCommentAvatar(playerId: string) {
  if (!playerId || commentAvatars.has(playerId)) return

  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        const avatarUrl = await playerService.renderAvatar(skinData.skin, 24)
        commentAvatars.set(playerId, avatarUrl)
      }
    }
  } catch (e) {
    console.warn('[CommentList] Failed to load comment avatar:', e)
  }
}

const getCommentAvatar = (playerId: string | null): string | undefined => {
  if (!playerId) return undefined
  return commentAvatars.get(playerId)
}

// 监听评论变化加载头像
watch(
  () => props.comments,
  (newComments) => {
    newComments.forEach((comment) => {
      if (comment.playerId && !commentAvatars.has(comment.playerId)) {
        loadCommentAvatar(comment.playerId)
      }
    })
  },
  { immediate: true },
)
</script>

<template>
  <div class="bf-comments-list">
    <div v-for="comment in comments" :key="comment.commentId" class="bf-comment-card">
      <!-- 投票 -->
      <div class="bf-comment-vote">
        <button class="bf-vote-btn bf-vote-btn--small">
          <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
            <path d="M12 4l-8 8h6v8h4v-8h6z" />
          </svg>
        </button>
        <span class="bf-comment-points">{{ comment.points }}</span>
      </div>

      <!-- 评论内容 -->
      <div class="bf-comment-content">
        <div class="bf-comment-meta">
          <img
            v-if="getCommentAvatar(comment.playerId)"
            :src="getCommentAvatar(comment.playerId)"
            class="bf-comment-avatar"
            alt=""
          />
          <div v-else class="bf-comment-avatar-placeholder">
            {{ (comment.memberNickname || comment.memberId).charAt(0).toUpperCase() }}
          </div>
          <span class="bf-comment-author">{{
            comment.memberNickname || comment.memberId
          }}</span>
          <span class="bf-meta-divider">•</span>
          <span class="bf-comment-date">{{
            new Date(comment.createdAt).toLocaleDateString()
          }}</span>
        </div>
        <!-- 使用 Markdown 渲染评论 -->
        <div class="bf-comment-text">
          <MarkdownRenderer :content="comment.text" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bf-comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* 评论卡片 */
.bf-comment-card {
  display: flex;
  gap: var(--bf-space-md, 16px);
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius-sm, 12px);
  padding: var(--bf-space-md, 16px);
}

.bf-comment-vote {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.bf-vote-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--bf-text-muted, #666666);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-vote-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  color: var(--bf-primary, #ff6b35);
}

.bf-vote-btn--small {
  width: 24px;
  height: 24px;
}

.bf-comment-points {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--bf-text-secondary, #b3b3b3);
}

.bf-comment-content {
  flex: 1;
  min-width: 0;
}

.bf-comment-meta {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  margin-bottom: var(--bf-space-sm, 8px);
}

.bf-comment-avatar {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  image-rendering: pixelated;
  margin-right: var(--bf-space-xs, 4px);
}

.bf-comment-avatar-placeholder {
  width: 24px;
  height: 24px;
  background: var(
    --bf-fire-gradient,
    linear-gradient(135deg, #ff6b35 0%, #ff9f1c 50%, #ffbe0b 100%)
  );
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  margin-right: var(--bf-space-xs, 4px);
}

.bf-comment-author {
  font-weight: 500;
  color: var(--bf-primary, #ff6b35);
  font-size: 0.875rem;
}

.bf-meta-divider {
  color: var(--bf-text-muted, #666666);
}

.bf-comment-date {
  color: var(--bf-text-muted, #666666);
  font-size: 0.75rem;
}

.bf-comment-text {
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  line-height: 1.5;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-comment-card {
    padding: var(--bf-space-sm, 12px);
    overflow-x: hidden;
  }

  .bf-comment-text {
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
}
</style>
