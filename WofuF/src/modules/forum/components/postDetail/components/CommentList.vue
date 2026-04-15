<script lang="ts" setup>
/**
 * CommentList.vue - 评论列表组件
 *
 * 功能：
 * - 管理评论列表渲染（Bilibili 风格：后端返回扁平结构）
 * - 提供管理员权限检查
 * - 处理回复和隐藏/显示评论事件
 *
 * 注意：Bilibili 风格下，后端已处理好：
 * - 主评论列表（parentCommentId IS NULL）作为根节点
 * - 每个主评论的 childComments 包含其所有子评论（扁平结构）
 * - 前端只需要直接渲染后端返回的结构
 */
import { ref, computed, onMounted, watch } from 'vue'
import { authService } from '@M/auth/services/AuthService.ts'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import CommentItem from './CommentItem.vue'

const props = defineProps<{
  comments: CommentDto[]
  replyingToCommentId?: string | null  // 当前正在回复的评论ID
  targetScrollCommentId?: string | null  // 目标滚动评论ID
}>()

const emit = defineEmits<{
  (e: 'reply', commentId: string): void
  (e: 'replyCancelled'): void
  (e: 'commentUpdated', commentId: string): void
  (e: 'scrollToComment', commentId: string): void
}>()

// 管理员状态
const isAdmin = ref(false)

// 标准化评论数据（兼容 isHidden/hidden 字段）
// 用于处理旧数据中可能使用 hidden 而不是 isHidden 的情况
interface LegacyCommentDto extends CommentDto {
  hidden?: boolean
}

function normalizeComments(comments: CommentDto[]): CommentDto[] {
  return comments.map(comment => {
    const legacyComment = comment as LegacyCommentDto
    return {
      ...comment,
      isHidden: comment.isHidden ?? legacyComment.hidden ?? false,
      childComments: comment.childComments?.map(child => {
        const legacyChild = child as LegacyCommentDto
        return {
          ...child,
          isHidden: child.isHidden ?? legacyChild.hidden ?? false
        }
      }) ?? []
    }
  })
}

// 计算属性：标准化后的评论列表（后端已处理好树结构）
const normalizedComments = computed(() => normalizeComments(props.comments))

// 检查管理员权限
async function checkAdminPermission() {
  if (!authService.isAuthenticated()) {
    isAdmin.value = false
    return
  }

  try {
    const hasPermission = await adminService.hasAnyPermission(
      ['ADMIN_ACCESS', 'COMMENT_DELETE_ANY', 'COMMENT_VIEW_HIDDEN'],
      true
    )
    isAdmin.value = hasPermission
  } catch (e) {
    console.warn('[CommentList] Failed to check admin permissions:', e)
    isAdmin.value = false
  }
}

// 组件挂载时检查权限
onMounted(() => {
  checkAdminPermission()
})

// 监听 comments 变化，重新检查权限
watch(
  () => props.comments.length,
  () => {
    void checkAdminPermission()
  }
)

// 处理回复事件
function handleReply(commentId: string) {
  emit('reply', commentId)
}

// 处理取消回复事件
function handleReplyCancelled() {
  emit('replyCancelled')
}

// 处理评论更新事件
async function handleCommentUpdated(commentId: string) {
  await checkAdminPermission()
  emit('commentUpdated', commentId)
}

// 处理滚动到评论事件（子评论跨页跳转）
function handleScrollToComment(commentId: string) {
  emit('scrollToComment', commentId)
}
</script>

<template>
  <div class="bf-comments-list">
    <CommentItem
      v-for="comment in normalizedComments"
      :key="comment.commentId"
      :comment="comment"
      :is-admin="isAdmin"
      :depth="0"
      :is-replying-to="replyingToCommentId === comment.commentId"
      :replying-to-comment-id="replyingToCommentId"
      :target-scroll-comment-id="targetScrollCommentId"
      @reply="handleReply"
      @reply-cancelled="handleReplyCancelled"
      @comment-updated="handleCommentUpdated"
      @scroll-to-comment="handleScrollToComment"
    />
  </div>
</template>

<style scoped>
.bf-comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}
</style>
