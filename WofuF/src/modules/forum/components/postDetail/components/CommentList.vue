<script lang="ts" setup>
/**
 * CommentList.vue - 评论列表组件
 *
 * 功能：
 * - 管理评论列表渲染
 * - 提供管理员权限检查
 * - 处理回复和隐藏/显示评论事件
 */
import { ref, computed, onMounted, watch } from 'vue'
import { authService } from '@M/auth/services/AuthService.ts'
import type { CommentDto } from '@M/forum/dtos/Post.ts'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import CommentItem from './CommentItem.vue'

const props = defineProps<{
  comments: CommentDto[]
  replyingToCommentId?: string | null  // 当前正在回复的评论ID
}>()

const emit = defineEmits<{
  (e: 'reply', commentId: string): void
  (e: 'replyCancelled'): void
  (e: 'commentUpdated', commentId: string): void
}>()

// 管理员状态
const isAdmin = ref(false)

// 将扁平评论列表构建为树结构
interface CommentNode extends CommentDto {
  children: CommentNode[]
}

function buildCommentTree(comments: CommentDto[]): CommentNode[] {
  const commentMap = new Map<string, CommentNode>()
  const rootComments: CommentNode[] = []

  // 先把所有评论转换为节点
  comments.forEach(comment => {
    commentMap.set(comment.commentId, {
      ...comment,
      children: []
    })
  })

  // 再遍历一次，构建树结构
  comments.forEach(comment => {
    const node = commentMap.get(comment.commentId)!
    if (comment.parentCommentId && commentMap.has(comment.parentCommentId)) {
      // 有父评论，加入父评论的 children
      const parent = commentMap.get(comment.parentCommentId)!
      parent.children.push(node)
    } else {
      // 顶级评论
      rootComments.push(node)
    }
  })

  return rootComments
}

// 计算属性：构建好的评论树
const commentTree = computed(() => buildCommentTree(props.comments))

// 检查管理员权限
async function checkAdminPermission() {
  if (!authService.isAuthenticated()) {
    isAdmin.value = false
    return
  }

  try {
    // 使用 forceRefresh: true 强制刷新成员数据，确保权限最新
    const hasPermission = await adminService.hasAnyPermission(
      ['ADMIN_ACCESS', 'COMMENT_DELETE_ANY', 'COMMENT_VIEW_HIDDEN'],
      true // forceRefresh
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

// 监听 comments 变化，重新检查权限（确保评论更新后权限状态正确）
watch(
  () => props.comments.length,
  () => {
    checkAdminPermission()
  }
)

// 处理回复事件（透传给父组件）
function handleReply(commentId: string) {
  emit('reply', commentId)
}

// 处理取消回复事件（透传给父组件）
function handleReplyCancelled() {
  emit('replyCancelled')
}

// 处理评论更新事件（透传给父组件）
async function handleCommentUpdated(commentId: string) {
  await checkAdminPermission()
  emit('commentUpdated', commentId)
}
</script>

<template>
  <div class="bf-comments-list">
    <CommentItem
      v-for="comment in commentTree"
      :key="comment.commentId"
      :comment="comment"
      :is-admin="isAdmin"
      :depth="0"
      :is-replying-to="replyingToCommentId === comment.commentId"
      @reply="handleReply"
      @reply-cancelled="handleReplyCancelled"
      @comment-updated="handleCommentUpdated"
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
