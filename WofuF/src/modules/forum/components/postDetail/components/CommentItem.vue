<script lang="ts" setup>
/**
 * CommentItem.vue - 单个评论组件（支持递归嵌套）
 *
 * 功能：
 * - 展示评论内容、作者信息、投票按钮
 * - 管理员视角下显示隐藏/显示控制
 * - 递归渲染子评论（children）
 * - 提供回复按钮
 */
import { ref, computed, watch, reactive } from 'vue'
import { useToast } from 'primevue/usetoast'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { PlayerService } from '@M/players/services/PlayerService'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import MarkdownRenderer from '@M/forum/components/shared/MarkdownRenderer.vue'
import { translate } from '@S/services/i18n'

// Props - 接收单个评论数据
const props = defineProps<{
  comment: CommentDto
  isAdmin: boolean
  depth?: number  // 嵌套层级，默认0
  isReplyingTo?: boolean  // 是否正在回复此评论
}>()

const emit = defineEmits<{
  (e: 'reply', commentId: string): void
  (e: 'replyCancelled'): void
  (e: 'commentUpdated', commentId: string): void
}>()

const toast = useToast()
const playerService = new PlayerService()

// 操作中的评论ID
const operatingCommentId = ref<string | null>(null)

// 评论头像 Map
const commentAvatars = reactive(new Map<string, string>())

// 计算子评论（兼容 childComments 和 children）
const childComments = computed(() => {
  return props.comment.children || props.comment.childComments || []
})

// 是否显示嵌套回复（最多显示4层，即 depth 0-3）
const maxDepth = 4
const effectiveDepth = computed(() => props.depth || 0)
const canShowChildren = computed(() => effectiveDepth.value < maxDepth && childComments.value.length > 0)

// 是否可以回复（最多3层：depth 0, 1 可回复，depth 2 禁止回复）
// 因为 depth 2 评论被回复会创建 depth 3（超过3层限制）
const canReply = computed(() => effectiveDepth.value < 2)

// 超过5条子评论时自动折叠
const autoCollapseThreshold = 5
const hasManyChildren = computed(() => childComments.value.length > autoCollapseThreshold)
const isAutoCollapsed = ref(false)

// 初始自动折叠状态（只在组件挂载时设置，不监听变化）
// 用户可以自由展开/折叠，不受数据变化影响
if (childComments.value.length > autoCollapseThreshold) {
  isAutoCollapsed.value = true
}

// 切换折叠状态
function toggleCollapse() {
  isAutoCollapsed.value = !isAutoCollapsed.value
}

// 层级颜色配置（4层嵌套：depth 0-3）
const depthColors = [
  'var(--bf-primary, #ff6b35)',      // 第0层 - 主色调
  '#ff9f1c',                          // 第1层 - 橙色
  '#ffbe0b',                          // 第2层 - 黄色
  '#22c55e',                          // 第3层 - 绿色（最深嵌套）
]

// 获取当前层级的颜色
function getDepthColor(depth: number): string {
  const index = Math.min(depth, depthColors.length - 1)
  return depthColors[index] ?? 'var(--bf-primary, #ff6b35)'
}

// 加载评论头像
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
    console.warn('[CommentItem] Failed to load avatar:', e)
  }
}

// 获取评论头像
function getCommentAvatar(playerId: string | null): string | undefined {
  if (!playerId) return undefined
  return commentAvatars.get(playerId)
}

// 监听评论变化加载头像
watch(
  () => props.comment.playerId,
  (playerId) => {
    if (playerId) {
      loadCommentAvatar(playerId)
    }
  },
  { immediate: true }
)

// 隐藏/显示评论（toggle）
async function toggleComment() {
  const commentId = props.comment.commentId
  const isCurrentlyHidden = !!props.comment.isHidden

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
      emit('commentUpdated', commentId)
    } else {
      const errorMsg = String(result.error) || ''
      toast.add({
        severity: 'warn',
        summary: translate('forum', 'admin.operationFailed'),
        detail: errorMsg,
        life: 5000,
      })
      emit('commentUpdated', commentId)
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'error'),
      detail: String(e),
      life: 5000,
    })
    emit('commentUpdated', commentId)
  } finally {
    operatingCommentId.value = null
  }
}

// 处理回复按钮点击（支持取消回复）
function handleReply() {
  // 如果正在回复此评论，则取消回复
  if (props.isReplyingTo) {
    emit('replyCancelled')
  } else {
    emit('reply', props.comment.commentId)
  }
}

// 格式化日期
function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
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
}
</script>

<template>
  <div
    class="bf-comment-item"
    :class="{
      'bf-comment-item--hidden': isAdmin && comment.isHidden,
      'bf-comment-item--nested': effectiveDepth > 0,
      'bf-comment-item--replying': isReplyingTo
    }"
    :style="{
      '--nest-level': effectiveDepth,
      '--depth-color': getDepthColor(effectiveDepth)
    }"
  >
    <!-- 评论主体 -->
    <div class="bf-comment-card">
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
          <span class="bf-comment-author">
            {{ comment.memberNickname || comment.memberId }}
          </span>
          <span class="bf-meta-divider">•</span>
          <span class="bf-comment-date">{{ formatDate(comment.createdAt) }}</span>
          <!-- 隐藏标记（仅管理员可见） -->
          <span
            v-if="isAdmin && comment.isHidden"
            class="bf-badge bf-badge--hidden"
            title="已隐藏 - 普通用户不可见"
          >
            ◉ 已隐藏
          </span>
        </div>

        <!-- 评论正文 -->
        <div class="bf-comment-text" :class="{ 'bf-comment-text--hidden': comment.isHidden && !isAdmin }">
          <MarkdownRenderer :content="comment.text" />
        </div>

        <!-- 操作栏 -->
        <div class="bf-comment-footer">
          <!-- 回复状态指示器（正在回复此评论时显示） -->
          <Transition name="reply-indicator">
            <div v-if="isReplyingTo" class="bf-reply-indicator">
              <span class="bf-reply-indicator__dot"></span>
              <span class="bf-reply-indicator__text">回复中</span>
            </div>
          </Transition>

          <!-- 回复按钮（最多3层嵌套，depth >= 3 禁止回复） -->
          <button v-if="canReply" class="bf-reply-btn" :class="{ 'bf-reply-btn--active': isReplyingTo }" @click="handleReply">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
              <path d="M3 10h10a5 5 0 015 5v6M3 10l6 6m-6-6l6-6" />
            </svg>
            {{ isReplyingTo ? '取消回复' : '回复' }}
          </button>

          <!-- 管理员操作按钮 -->
          <div v-if="isAdmin" class="bf-comment-actions">
            <button
              class="bf-comment-action-btn"
              :class="{
                'bf-comment-action-btn--active': comment.isHidden,
                'bf-comment-action-btn--loading': operatingCommentId === comment.commentId
              }"
              :disabled="operatingCommentId === comment.commentId"
              :title="comment.isHidden ? translate('forum', 'admin.show') : translate('forum', 'admin.hide')"
              @click="toggleComment"
            >
              <span class="bf-toggle-symbol">{{ comment.isHidden ? '○' : '◉' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 递归渲染子评论 -->
    <div v-if="canShowChildren && !isAutoCollapsed" class="bf-comment-children">
      <CommentItem
        v-for="child in childComments"
        :key="child.commentId"
        :comment="child"
        :is-admin="isAdmin"
        :depth="effectiveDepth + 1"
        @reply="(id) => emit('reply', id)"
        @comment-updated="(id) => emit('commentUpdated', id)"
      />
    </div>

    <!-- 展开/折叠按钮（超过5条子评论时显示） -->
    <button
      v-if="hasManyChildren"
      class="bf-expand-btn"
      @click="toggleCollapse"
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        width="14"
        height="14"
        :class="{ 'bf-expand-btn__icon--collapsed': isAutoCollapsed }"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
      {{ isAutoCollapsed ? `展开 ${childComments.length} 条回复` : '收起' }}
    </button>
  </div>
</template>

<style scoped>
.bf-comment-item {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* 嵌套层级样式 */
.bf-comment-item--nested {
  margin-left: 24px;
  padding-left: 16px;
  border-left: 3px solid var(--depth-color, var(--bf-card-border));
  position: relative;
}

/* 层级连接线效果 */
.bf-comment-item--nested::before {
  content: '';
  position: absolute;
  left: -3px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(
    180deg,
    var(--depth-color) 0%,
    rgba(255, 255, 255, 0.1) 100%
  );
  opacity: 0.6;
}

/* 管理员视角：隐藏评论的特殊样式 */
.bf-comment-item--hidden > .bf-comment-card {
  background: rgba(239, 68, 68, 0.08);
  border-color: rgba(239, 68, 68, 0.3);
  border-left: 3px solid #ef4444;
}

/* 回复动画：正在回复的评论高亮效果 */
.bf-comment-item--replying > .bf-comment-card {
  background: linear-gradient(
    135deg,
    rgba(255, 107, 53, 0.12) 0%,
    rgba(255, 159, 28, 0.08) 100%
  );
  border-color: rgba(255, 107, 53, 0.4);
  border-left: 3px solid var(--bf-primary, #ff6b35);
  box-shadow: 0 0 20px rgba(255, 107, 53, 0.15);
  animation: replyPulse 2s ease-in-out infinite;
}

/* 回复动画关键帧 - 柔和的呼吸光效 */
@keyframes replyPulse {
  0%, 100% {
    box-shadow: 0 0 15px rgba(255, 107, 53, 0.12);
  }
  50% {
    box-shadow: 0 0 25px rgba(255, 107, 53, 0.25);
  }
}

/* 回复目标指示器 - 左侧发射光效 */
.bf-comment-item--replying::after {
  content: '';
  position: absolute;
  left: -3px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(
    180deg,
    transparent 0%,
    var(--bf-primary, #ff6b35) 50%,
    transparent 100%
  );
  animation: replyGlow 1.5s ease-in-out infinite;
}

/* 光效动画 */
@keyframes replyGlow {
  0%, 100% {
    opacity: 0.5;
    height: 40%;
  }
  50% {
    opacity: 1;
    height: 70%;
  }
}

.bf-comment-card {
  display: flex;
  gap: var(--bf-space-md, 16px);
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius-sm, 12px);
  padding: var(--bf-space-md, 16px);
}

/* 投票区域 */
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

/* 评论内容区 */
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

/* 隐藏标记徽章 */
.bf-badge--hidden {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 4px;
  color: #ef4444;
  font-size: 0.6875rem;
  font-weight: 500;
  margin-left: 4px;
}

.bf-comment-text {
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  line-height: 1.5;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.bf-comment-text--hidden {
  opacity: 0.4;
  filter: blur(0.5px);
}

/* 底部操作栏 */
.bf-comment-footer {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  margin-top: var(--bf-space-sm, 8px);
}

.bf-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: none;
  color: var(--bf-text-muted, #666666);
  font-size: 0.75rem;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-reply-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--bf-text-secondary, #b3b3b3);
}

.bf-reply-btn svg {
  flex-shrink: 0;
}

/* 回复按钮激活状态（正在回复此评论时） */
.bf-reply-btn--active {
  background: rgba(255, 107, 53, 0.15);
  color: var(--bf-primary, #ff6b35);
  border: 1px solid rgba(255, 107, 53, 0.3);
}

/* 回复状态指示器 */
.bf-reply-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.2) 0%, rgba(255, 159, 28, 0.15) 100%);
  border: 1px solid rgba(255, 107, 53, 0.35);
  border-radius: 12px;
  font-size: 0.75rem;
  color: var(--bf-primary, #ff6b35);
  font-weight: 500;
}

.bf-reply-indicator__dot {
  width: 6px;
  height: 6px;
  background: var(--bf-primary, #ff6b35);
  border-radius: 50%;
  animation: replyDotPulse 1s ease-in-out infinite;
}

.bf-reply-indicator__text {
  letter-spacing: 0.02em;
}

/* 回复指示器动画 */
@keyframes replyDotPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

/* 回复指示器过渡动画 */
.reply-indicator-enter-active,
.reply-indicator-leave-active {
  transition: all 0.25s ease;
}

.reply-indicator-enter-from,
.reply-indicator-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

/* 管理员操作按钮 */
.bf-comment-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
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

.bf-comment-action-btn--active {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.bf-comment-action-btn--active:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.15);
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

.bf-toggle-symbol {
  font-size: 14px;
  line-height: 1;
}

/* 子评论容器 */
.bf-comment-children {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

/* 展开/折叠按钮 */
.bf-expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: var(--bf-space-sm, 8px);
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-btn-radius, 8px);
  color: var(--bf-primary, #ff6b35);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-expand-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.3);
}

.bf-expand-btn__icon--collapsed {
  transform: rotate(-90deg);
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-comment-card {
    padding: var(--bf-space-sm, 12px);
    overflow-x: hidden;
  }

  .bf-comment-item--nested {
    margin-left: 8px;
    padding-left: 10px;
  }

  .bf-comment-text {
    word-wrap: break-word;
    overflow-wrap: break-word;
  }

  /* 子评论容器手机端自适应 */
  .bf-comment-children {
    gap: var(--bf-space-sm, 8px);
  }

  /* 手机端回复指示器优化 */
  .bf-reply-indicator {
    padding: 6px 12px;
    font-size: 0.8125rem;
  }

  .bf-reply-indicator__dot {
    width: 8px;
    height: 8px;
  }

  /* 手机端回复动画优化（减少复杂度） */
  .bf-comment-item--replying > .bf-comment-card {
    animation: replyPulseMobile 2.5s ease-in-out infinite;
    box-shadow: 0 0 16px rgba(255, 107, 53, 0.18);
  }

  @keyframes replyPulseMobile {
    0%, 100% {
      box-shadow: 0 0 12px rgba(255, 107, 53, 0.12);
    }
    50% {
      box-shadow: 0 0 20px rgba(255, 107, 53, 0.22);
    }
  }
}
</style>
