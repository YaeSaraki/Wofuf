<script lang="ts" setup>
import { ref, watch, reactive, computed } from 'vue'
import { useToast } from 'primevue/usetoast'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { PlayerService } from '@M/players/services/PlayerService'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { memberService } from '@M/auth/services/MemberService.ts'
import MarkdownRenderer from '@M/forum/components/shared/MarkdownRenderer.vue'
import { translate } from '@S/services/i18n'

const props = defineProps<{
  comments: CommentDto[]
}>()

const emit = defineEmits<{
  (e: 'commentUpdated', commentId: string): void
}>()

const toast = useToast()
const playerService = new PlayerService()

// 管理员状态
const isAdmin = ref(false)
const isLoadingAdmin = ref(true)

// 操作中的评论ID
const operatingCommentId = ref<string | null>(null)

// 评论头像
const commentAvatars = reactive(new Map<string, string>())

// 检查管理员权限
async function checkAdminPermission() {
  isLoadingAdmin.value = true
  try {
    isAdmin.value = await adminService.hasAnyPermission([
      'ADMIN_ACCESS',
      'COMMENT_DELETE_ANY',
      'COMMENT_VIEW_HIDDEN',
    ])
  } catch (e) {
    isAdmin.value = false
  } finally {
    isLoadingAdmin.value = false
  }
}

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

// 隐藏/显示评论（toggle）
async function toggleComment(commentId: string, isCurrentlyHidden: boolean) {
  operatingCommentId.value = commentId
  try {
    const result = isCurrentlyHidden
      ? await adminService.showComment(commentId)
      : await adminService.hideComment(commentId)

    if (result.isSuccess) {
      toast.add({
        severity: 'success',
        summary: isCurrentlyHidden ? translate('forum', 'admin.show') : translate('forum', 'admin.hide'),
        detail: translate('forum', 'admin.operationSuccess'),
        life: 2000,
      })
      // 通知父组件刷新评论列表
      emit('commentUpdated', commentId)
    } else {
      // API 返回失败，刷新评论列表以获取最新状态
      const errorMsg = String(result.error) || ''
      toast.add({
        severity: 'warn',
        summary: translate('forum', 'admin.operationFailed'),
        detail: errorMsg,
        life: 5000,
      })
      // 通知父组件刷新评论列表
      emit('commentUpdated', commentId)
    }
  } catch (e) {
    // 发生异常（可能是后端抛出的错误），刷新评论列表
    toast.add({
      severity: 'error',
      summary: translate('forum', 'error'),
      detail: String(e),
      life: 5000,
    })
    // 通知父组件刷新评论列表
    emit('commentUpdated', commentId)
  } finally {
    operatingCommentId.value = null
  }
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

// 初始化检查管理员权限
checkAdminPermission()
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
          <!-- 隐藏标记 -->
          <span v-if="comment.isHidden" class="bf-comment-hidden-badge">
            {{ translate('forum', 'admin.hiddenComments') }}
          </span>
        </div>
        <!-- 使用 Markdown 渲染评论 -->
        <div class="bf-comment-text" :class="{ 'bf-comment-text--hidden': comment.isHidden }">
          <MarkdownRenderer :content="comment.text" />
        </div>
      </div>

      <!-- 管理员操作按钮 -->
      <div v-if="isAdmin && !isLoadingAdmin" class="bf-comment-actions">
        <!-- 切换隐藏/显示按钮 -->
        <button
          class="bf-comment-action-btn"
          :class="{
            'bf-comment-action-btn--hidden': comment.isHidden,
            'bf-comment-action-btn--loading': operatingCommentId === comment.commentId
          }"
          :disabled="operatingCommentId === comment.commentId"
          :title="comment.isHidden ? translate('forum', 'admin.show') : translate('forum', 'admin.hide')"
          @click="toggleComment(comment.commentId, !!comment.isHidden)"
        >
          <!-- 隐藏状态：显示眼睛图标 -->
          <svg v-if="comment.isHidden" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
          <!-- 正常状态：显示闭眼图标 -->
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24" />
            <line x1="1" y1="1" x2="23" y2="23" />
          </svg>
        </button>
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

.bf-comment-text--hidden {
  opacity: 0.5;
  text-decoration: line-through;
}

.bf-comment-hidden-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 100px;
  font-size: 10px;
  font-weight: 500;
  color: #ef4444;
  margin-left: 8px;
}

/* 管理员操作按钮 */
.bf-comment-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: var(--bf-space-sm, 8px);
  flex-shrink: 0;
}

.bf-comment-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-input-radius, 8px);
  color: var(--bf-text-muted);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-comment-action-btn:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.bf-comment-action-btn--show:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.bf-comment-action-btn--hidden {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.bf-comment-action-btn--hidden:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.bf-comment-action-btn--loading {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-comment-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-comment-action-btn svg {
  width: 16px;
  height: 16px;
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
