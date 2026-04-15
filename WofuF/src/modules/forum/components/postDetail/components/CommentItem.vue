<script lang="ts" setup>
/**
 * CommentItem.vue - 单个评论组件（Bilibili 风格）
 *
 * 功能：
 * - 展示评论内容、作者信息、投票按钮
 * - 显示短 ID（可点击复制）
 * - 管理员视角下显示隐藏/显示控制
 * - 渲染一层子评论（childComments）
 * - 提供回复按钮
 * - 显示"回复 @xxx"标识
 * - 超过10条子评论自动折叠
 */
import { ref, computed, watch, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { PlayerService } from '@M/players/services/PlayerService'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { authService } from '@M/auth/services/AuthService'
import { forumService } from '@M/forum/services/ForumService'
import MarkdownRenderer from '@M/forum/components/shared/MarkdownRenderer.vue'
import { translate } from '@S/services/i18n'

const props = withDefaults(defineProps<{
  comment: CommentDto
  isAdmin: boolean
  depth?: number
  isReplyingTo?: boolean
  replyingToCommentId?: string | null  // 当前正在回复的评论ID（用于子评论）
  targetScrollCommentId?: string | null  // 目标滚动评论ID（用于跨页跳转）
}>(), {
  depth: 0,
  isReplyingTo: false,
  replyingToCommentId: null,
  targetScrollCommentId: null
})

const emit = defineEmits<{
  (e: 'reply', commentId: string): void
  (e: 'replyCancelled'): void
  (e: 'commentUpdated', commentId: string): void
  (e: 'scrollToComment', commentId: string): void
}>()

const toast = useToast()
const playerService = new PlayerService()

const operatingCommentId = ref<string | null>(null)
const commentAvatars = reactive(new Map<string, string>())
const comment = reactive<CommentDto>({ ...props.comment })
const isVoteLoading = ref(false)
const router = useRouter()

// 本地投票状态（类似 VoteButtons.vue 的实现）
const localPoints = ref(props.comment.points ?? 0)
const localWasUpvotedByMe = ref(props.comment.wasUpvotedByMe ?? false)
const localWasDownvotedByMe = ref(props.comment.wasDownvotedByMe ?? false)

// 监听 props 变化，同步本地状态
watch(() => props.comment, (newComment) => {
  localPoints.value = newComment.points ?? 0
  localWasUpvotedByMe.value = newComment.wasUpvotedByMe ?? false
  localWasDownvotedByMe.value = newComment.wasDownvotedByMe ?? false
  // 更新 reactive comment 对象
  Object.assign(comment, newComment)
}, { deep: true, immediate: true })

const effectiveDepth = computed(() => props.depth || 0)
const childComments = computed(() => comment.childComments || [])
const hasChildComments = computed(() => childComments.value.length > 0)
const isSubComment = computed(() => effectiveDepth.value > 0)

// 折叠阈值
const autoCollapseThreshold = 10
const hasManyChildren = computed(() => childComments.value.length > autoCollapseThreshold)

// 子评论分页
const childCommentsPerPage = 10
const childCommentsCurrentPage = ref(1)
const totalChildPages = computed(() => Math.ceil(childComments.value.length / childCommentsPerPage))

// 当前页的子评论
const paginatedChildComments = computed(() => {
  const start = (childCommentsCurrentPage.value - 1) * childCommentsPerPage
  const end = start + childCommentsPerPage
  return childComments.value.slice(start, end)
})

// 跳转到指定页
function goToChildPage(page: number) {
  childCommentsCurrentPage.value = page
  if (isAutoCollapsed.value) {
    isAutoCollapsed.value = false
  }
}

// 上一页
function prevChildPage() {
  if (childCommentsCurrentPage.value > 1) {
    childCommentsCurrentPage.value--
  }
}

// 下一页
function nextChildPage() {
  if (childCommentsCurrentPage.value < totalChildPages.value) {
    childCommentsCurrentPage.value++
  }
}

// 是否正在回复此评论（结合本地状态和父组件传入的 replyingToCommentId）
const isThisReplying = computed(() => {
  // 直接使用本地 isReplyingTo 状态（主评论）
  if (props.isReplyingTo) return true
  // 检查 replyingToCommentId 是否指向此评论（子评论）
  if (props.replyingToCommentId === props.comment.commentId) return true
  return false
})

// 折叠状态 - 使用 ref 但需要响应式更新
const isAutoCollapsed = ref(false)

// 监听子评论数量变化，更新折叠状态
watch(
  () => childComments.value.length,
  (newLength) => {
    if (newLength > autoCollapseThreshold) {
      isAutoCollapsed.value = true
    } else if (newLength === 0) {
      // 没有子评论时不折叠
      isAutoCollapsed.value = false
    }
  },
  { immediate: true }
)

// 监听目标滚动评论ID变化，处理跨页跳转
watch(
  () => props.targetScrollCommentId,
  (targetId) => {
    if (!targetId) return

    // 检查目标是否是当前评论本身（主评论的滚动）
    if (targetId === props.comment.commentId) {
      // 滚动到当前评论（主评论自身）
      isAutoCollapsed.value = false
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          const element = document.getElementById(`comment-${targetId}`)
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'center' })
            element.classList.add('bf-comment-highlight')
            setTimeout(() => element.classList.remove('bf-comment-highlight'), 2000)
          }
        })
      })
      return
    }

    // 检查目标是否是我的子评论
    const childIndex = childComments.value.findIndex(c => c.commentId === targetId)
    if (childIndex === -1) {
      // 目标不是我的子评论，不处理
      return
    }

    // 目标是我的子评论，需要跳转到正确页面
    isAutoCollapsed.value = false
    const targetPage = Math.floor(childIndex / childCommentsPerPage) + 1
    if (targetPage !== childCommentsCurrentPage.value) {
      childCommentsCurrentPage.value = targetPage
    }

    // 等待 Vue 更新 DOM 后再滚动
    nextTick(() => {
      const element = document.getElementById(`comment-${targetId}`)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' })
        element.classList.add('bf-comment-highlight')
        setTimeout(() => element.classList.remove('bf-comment-highlight'), 2000)
      }
    })
  }
)

function toggleCollapse() {
  isAutoCollapsed.value = !isAutoCollapsed.value
}

// 短 ID 点击处理：滚动到评论位置
function scrollToComment(e: MouseEvent) {
  e.preventDefault()
  if (!props.comment.shortId) return

  const targetId = `comment-${props.comment.commentId}`

  // 子评论需要处理跨页跳转
  if (isSubComment.value) {
    // 子评论：由父组件处理跨页跳转，发送事件
    emit('scrollToComment', props.comment.commentId)
    return
  }

  // 主评论：直接在当前组件内处理
  let element = document.getElementById(targetId)

  if (element) {
    // 确保子评论列表已展开
    if (isAutoCollapsed.value) {
      isAutoCollapsed.value = false
    }
    // 如果有分页，查找目标评论所在的页并跳转
    if (hasManyChildren.value) {
      const index = childComments.value.findIndex(c => c.commentId === props.comment.commentId)
      if (index !== -1) {
        const targetPage = Math.floor(index / childCommentsPerPage) + 1
        if (targetPage !== childCommentsCurrentPage.value) {
          childCommentsCurrentPage.value = targetPage
        }
      }
    }
    // 等待分页更新后滚动
    setTimeout(() => {
      const el = document.getElementById(targetId)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        el.classList.add('bf-comment-highlight')
        setTimeout(() => el.classList.remove('bf-comment-highlight'), 2000)
      }
    }, 50)
  } else {
    // 如果找不到元素，复制到剪贴板作为备选
    copyShortId()
  }
}

// 复制短 ID
async function copyShortId() {
  if (!props.comment.shortId) return
  try {
    await navigator.clipboard.writeText(`#${props.comment.shortId}`)
    toast.add({
      severity: 'info',
      summary: '已复制',
      detail: `#${props.comment.shortId}`,
      life: 1500,
    })
  } catch {
    toast.add({
      severity: 'warn',
      summary: '复制失败',
      detail: '请手动复制',
      life: 2000,
    })
  }
}

// 滚动到被回复的评论（父评论）
function scrollToParentComment(e: MouseEvent) {
  e.preventDefault()
  if (!props.comment.replyToParentCommentId) return

  const targetId = `comment-${props.comment.replyToParentCommentId}`
  const element = document.getElementById(targetId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    // 高亮效果
    element.classList.add('bf-comment-highlight')
    setTimeout(() => element.classList.remove('bf-comment-highlight'), 2000)
    return
  }

  // 目标评论可能在其它子评论分页中，向上层委托跨页跳转
  emit('scrollToComment', props.comment.replyToParentCommentId)
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
    console.warn('[CommentItem] Failed to load avatar:', e)
  }
}

function getCommentAvatar(playerId: string | null): string | undefined {
  if (!playerId) return undefined
  return commentAvatars.get(playerId)
}

watch(
  () => props.comment.playerId,
  (playerId) => {
    if (playerId) {
      loadCommentAvatar(playerId)
    }
  },
  { immediate: true }
)

watch(
  () => props.comment,
  (newComment) => {
    Object.assign(comment, newComment)
  },
  { deep: true, immediate: true }
)

const isLoggedIn = computed(() => authService.isAuthenticated())
const isUpvoted = computed(() => localWasUpvotedByMe.value === true)
const isDownvoted = computed(() => localWasDownvotedByMe.value === true)

async function handleUpvoteComment(event: Event) {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  if (isVoteLoading.value) return
  isVoteLoading.value = true

  try {
    const result = await forumService.upvoteComment(comment.commentId)
    if (result.isSuccess) {
      const response = result.getValue()
      const points = response.newPoints
      const alreadyUpvoted = localWasUpvotedByMe.value
      localPoints.value = points
      localWasUpvotedByMe.value = !alreadyUpvoted
      if (!alreadyUpvoted) {
        localWasDownvotedByMe.value = false
      }
      // 更新 reactive comment 对象用于其他逻辑
      comment.points = points
      comment.wasUpvotedByMe = !alreadyUpvoted
      if (!alreadyUpvoted) {
        comment.wasDownvotedByMe = false
      }
      // 投票不需要刷新评论列表（乐观更新已足够）
      // emit('commentUpdated', comment.commentId)
    } else {
      toast.add({
        severity: 'warn',
        summary: translate('forum', 'error'),
        detail: String(result.error || '投票失败'),
        life: 3000,
      })
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'error'),
      detail: String(e),
      life: 3000,
    })
  } finally {
    isVoteLoading.value = false
  }
}

async function handleDownvoteComment(event: Event) {
  event.stopPropagation()
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  if (isVoteLoading.value) return
  isVoteLoading.value = true

  try {
    const result = await forumService.downvoteComment(comment.commentId)
    if (result.isSuccess) {
      const response = result.getValue()
      const points = response.newPoints
      const alreadyDownvoted = localWasDownvotedByMe.value
      localPoints.value = points
      localWasDownvotedByMe.value = !alreadyDownvoted
      if (!alreadyDownvoted) {
        localWasUpvotedByMe.value = false
      }
      // 更新 reactive comment 对象用于其他逻辑
      comment.points = points
      comment.wasDownvotedByMe = !alreadyDownvoted
      if (!alreadyDownvoted) {
        comment.wasUpvotedByMe = false
      }
      // 投票不需要刷新评论列表（乐观更新已足够）
      // emit('commentUpdated', comment.commentId)
    } else {
      toast.add({
        severity: 'warn',
        summary: translate('forum', 'error'),
        detail: String(result.error || '投票失败'),
        life: 3000,
      })
    }
  } catch (e) {
    toast.add({
      severity: 'error',
      summary: translate('forum', 'error'),
      detail: String(e),
      life: 3000,
    })
  } finally {
    isVoteLoading.value = false
  }
}

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

function handleReply() {
  if (isThisReplying.value) {
    emit('replyCancelled')
  } else {
    emit('reply', props.comment.commentId)
  }
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}m`
  if (diffHours < 24) return `${diffHours}h`
  if (diffDays < 30) return `${diffDays}d`
  return date.toLocaleDateString('yyyy-MM-dd', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>

<template>
  <div
    :id="`comment-${comment.commentId}`"
    class="bf-comment-item"
    :class="{
      'bf-comment-item--hidden': isAdmin && comment.isHidden,
      'bf-comment-item--sub': isSubComment,
      'bf-comment-item--replying': isThisReplying
    }"
  >
    <!-- 评论主体 -->
    <div class="bf-comment-card">
      <!-- 投票（统一 PostCard 风格，小尺寸） -->
      <div class="bf-comment-vote">
        <button
          type="button"
          class="bf-vote-btn bf-vote-btn--up"
          :class="{ 'bf-vote-btn--active': isUpvoted }"
          :disabled="isVoteLoading"
          @click="handleUpvoteComment"
          :title="isUpvoted ? '取消点赞' : '点赞'"
        >
          <span class="bf-vote-icon bf-vote-icon--up">^</span>
        </button>

        <span class="bf-vote-count" :style="{ color: isUpvoted ? 'var(--bf-primary)' : isDownvoted ? '#8B5CF6' : 'var(--bf-text-secondary)' }">
          {{ localPoints }}
        </span>

        <button
          type="button"
          class="bf-vote-btn bf-vote-btn--down"
          :class="{ 'bf-vote-btn--active': isDownvoted }"
          :disabled="isVoteLoading"
          @click="handleDownvoteComment"
          :title="isDownvoted ? '取消点踩' : '点踩'"
        >
          <span class="bf-vote-icon bf-vote-icon--down">v</span>
        </button>
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

          <!-- 短 ID（点击跳转或复制） -->
          <button
            v-if="comment.shortId"
            class="bf-short-id"
            @click="scrollToComment"
            title="点击跳转至评论"
          >
            #{{ comment.shortId }}
          </button>

          <!-- 回复目标（仅子评论显示） -->
          <span v-if="isSubComment && comment.replyToMemberNickname" class="bf-reply-target">
            <span class="bf-arrow">»</span>
            <span class="bf-reply-nickname">@{{ comment.replyToMemberNickname }}</span>
            <button
              v-if="comment.replyToShortId"
              class="bf-reply-short-id"
              @click="scrollToParentComment"
              title="点击跳转至被回复评论"
            >
              #{{ comment.replyToShortId }}
            </button>
          </span>

          <span class="bf-meta-sep">|</span>
          <span class="bf-comment-date">{{ formatDate(comment.createdAt) }}</span>

          <!-- 隐藏标记（仅管理员可见） -->
          <span
            v-if="isAdmin && comment.isHidden"
            class="bf-badge bf-badge--hidden"
          >
            [已隐藏]
          </span>
        </div>

        <!-- 评论正文 -->
        <div class="bf-comment-text" :class="{ 'bf-comment-text--hidden': comment.isHidden && !isAdmin }">
          <MarkdownRenderer :content="comment.text" size="thumbnail" />
        </div>

        <!-- 操作栏 -->
        <div class="bf-comment-footer">
          <!-- 回复状态指示器 -->
          <Transition name="reply-indicator">
            <span v-if="isThisReplying" class="bf-reply-indicator">
              <span class="bf-reply-indicator__dot">*</span>
              回复中
            </span>
          </Transition>

          <!-- 回复按钮 -->
          <button class="bf-action-btn" :class="{ 'bf-action-btn--active': isThisReplying }" @click="handleReply">
            <span class="bf-action-symbol">{{ isThisReplying ? '×' : '»' }}</span>
            {{ isThisReplying ? '取消' : '回复' }}
          </button>

          <!-- 管理员操作按钮 -->
          <div v-if="isAdmin" class="bf-admin-actions">
            <button
              class="bf-action-btn bf-action-btn--admin"
              :class="{ 'bf-action-btn--active': comment.isHidden }"
              :disabled="operatingCommentId === comment.commentId"
              @click="toggleComment"
            >
              {{ comment.isHidden ? '○ 显示' : '◉ 隐藏' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 子评论列表（仅主评论渲染） -->
    <div v-if="!isSubComment && hasChildComments && !isAutoCollapsed" class="bf-comment-children">
      <CommentItem
        v-for="child in paginatedChildComments"
        :key="child.commentId"
        :comment="child"
        :is-admin="isAdmin"
        :depth="1"
        :replying-to-comment-id="replyingToCommentId"
        @reply="(id) => emit('reply', id)"
        @reply-cancelled="() => emit('replyCancelled')"
        @comment-updated="(id) => emit('commentUpdated', id)"
        @scroll-to-comment="(id) => emit('scrollToComment', id)"
      />
    </div>

    <!-- 分页导航 -->
    <div v-if="!isSubComment && hasManyChildren && !isAutoCollapsed" class="bf-child-pagination">
      <button
        class="bf-page-btn"
        :disabled="childCommentsCurrentPage === 1"
        @click="prevChildPage"
      >
        ‹
      </button>
      <span class="bf-page-info">
        {{ childCommentsCurrentPage }} / {{ totalChildPages }}
      </span>
      <button
        class="bf-page-btn"
        :disabled="childCommentsCurrentPage >= totalChildPages"
        @click="nextChildPage"
      >
        ›
      </button>
    </div>

    <!-- 展开/折叠按钮（字符符号风格） -->
    <button
      v-if="hasManyChildren"
      class="bf-expand-btn"
      @click="toggleCollapse"
    >
      <span class="bf-expand-symbol">{{ isAutoCollapsed ? '[+' : '[-' }}</span>
      {{ isAutoCollapsed ? `展开 ${childComments.length} 条回复` : '收起' }}
    </button>
  </div>
</template>

<style scoped>
.bf-comment-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 子评论样式 */
.bf-comment-item--sub {
  margin-left: 20px;
  padding-left: 12px;
  border-left: 2px solid var(--bf-card-border, rgba(255, 255, 255, 0.08));
}

/* 管理员隐藏样式 */
.bf-comment-item--hidden > .bf-comment-card {
  background: rgba(239, 68, 68, 0.06);
  border-color: rgba(239, 68, 68, 0.2);
}

/* 回复高亮效果 */
.bf-comment-item--replying > .bf-comment-card {
  background: rgba(255, 107, 53, 0.08);
  border-color: rgba(255, 107, 53, 0.3);
}

.bf-comment-card {
  display: flex;
  gap: 12px;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.6));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: 8px;
  padding: 12px;
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
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--bf-text-muted);
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-vote-btn:hover:not(:disabled) {
  background: var(--bf-btn-secondary-bg);
  color: var(--bf-text-secondary);
}

.bf-vote-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.bf-vote-btn svg {
  width: 16px;
  height: 16px;
}

.bf-vote-icon {
  font-size: 14px;
  font-weight: bold;
  line-height: 1;
  user-select: none;
}

.bf-vote-btn--up:hover:not(:disabled),
.bf-vote-btn--up.bf-vote-btn--active {
  color: var(--bf-primary);
  background: var(--bf-fire-gradient-subtle);
}

.bf-vote-btn--down:hover:not(:disabled),
.bf-vote-btn--down.bf-vote-btn--active {
  color: #8B5CF6;
  background: rgba(139, 92, 246, 0.1);
}

.bf-vote-count {
  font-weight: 700;
  font-size: 13px;
  line-height: 1;
  padding: 4px 0;
  min-width: 28px;
  text-align: center;
}

/* 评论内容区 */
.bf-comment-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bf-comment-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 12px;
}

.bf-comment-avatar {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  image-rendering: pixelated;
}

.bf-comment-avatar-placeholder {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff9f1c 100%);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: white;
}

.bf-comment-author {
  font-weight: 500;
  color: var(--bf-primary, #ff6b35);
}

/* 短 ID 样式 */
.bf-short-id {
  background: none;
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.1));
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: var(--bf-text-muted, #666);
  cursor: pointer;
  transition: all 0.15s;
}

.bf-short-id:hover {
  border-color: var(--bf-primary, #ff6b35);
  color: var(--bf-primary, #ff6b35);
}

/* 回复目标 */
.bf-reply-target {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--bf-text-muted, #666);
}

.bf-arrow {
  color: var(--bf-primary, #ff6b35);
  font-weight: bold;
}

.bf-reply-short-id {
  background: none;
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.1));
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: var(--bf-text-muted, #666);
  cursor: pointer;
  text-decoration: none;
  transition: all 0.15s;
  margin-left: 4px;
}

.bf-reply-short-id:hover {
  border-color: var(--bf-primary, #ff6b35);
  color: var(--bf-primary, #ff6b35);
  text-decoration: none;
}

.bf-reply-nickname {
  color: var(--bf-primary, #ff6b35);
}

.bf-meta-sep {
  color: var(--bf-card-border, rgba(255, 255, 255, 0.1));
}

.bf-comment-date {
  color: var(--bf-text-muted, #666);
}

.bf-badge--hidden {
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 4px;
  padding: 1px 4px;
  color: #ef4444;
  font-size: 10px;
}

/* 评论正文 */
.bf-comment-text {
  color: var(--bf-text-secondary, #bbb);
  font-size: 13px;
  line-height: 1.6;
  word-wrap: break-word;
}

.bf-comment-text--hidden {
  opacity: 0.3;
}

/* 底部操作栏 */
.bf-comment-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 回复状态指示器 */
.bf-reply-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--bf-primary, #ff6b35);
}

.bf-reply-indicator__dot {
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 操作按钮 */
.bf-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--bf-text-muted, #666);
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.15s;
}

.bf-action-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--bf-text-secondary, #999);
}

.bf-action-btn--active {
  color: var(--bf-primary, #ff6b35);
}

.bf-action-btn--admin {
  font-size: 11px;
}

.bf-action-btn--admin.bf-action-btn--active {
  color: #ef4444;
}

.bf-action-symbol {
  font-size: 14px;
}

/* 子评论容器 */
.bf-comment-children {
  display: flex;
  flex-direction: column;
  gap: 8px;
}


/* 子评论分页导航 */
.bf-child-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0;
}

.bf-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  background: transparent;
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.1));
  border-radius: 6px;
  color: var(--bf-text-secondary, #999);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
}

.bf-page-btn:hover:not(:disabled) {
  border-color: var(--bf-primary, #ff6b35);
  color: var(--bf-primary, #ff6b35);
}

.bf-page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.bf-page-info {
  font-size: 12px;
  color: var(--bf-text-muted, #666);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

/* 展开/折叠按钮 */
.bf-expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--bf-primary, #ff6b35);
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.15s;
  align-self: flex-start;
}

.bf-expand-btn:hover {
  background: rgba(255, 107, 53, 0.1);
}

.bf-expand-symbol {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

/* 回复指示器过渡动画 */
.reply-indicator-enter-active,
.reply-indicator-leave-active {
  transition: all 0.2s ease;
}

.reply-indicator-enter-from,
.reply-indicator-leave-to {
  opacity: 0;
  transform: translateX(-4px);
}

/* 滚动高亮效果 */
.bf-comment-highlight {
  animation: highlight-pulse 2s ease-out;
}

@keyframes highlight-pulse {
  0% {
    background: rgba(255, 107, 53, 0.25);
    border-color: rgba(255, 107, 53, 0.5);
  }
  100% {
    background: var(--bf-card-bg, rgba(26, 26, 26, 0.6));
    border-color: var(--bf-card-border, rgba(255, 255, 255, 0.06));
  }
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-comment-card {
    padding: 10px;
  }

  .bf-comment-item--sub {
    margin-left: 12px;
    padding-left: 8px;
  }

  .bf-comment-meta {
    font-size: 11px;
  }

  .bf-short-id {
    font-size: 10px;
    padding: 1px 4px;
  }
}
</style>
