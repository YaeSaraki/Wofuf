<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { PostDto, CommentDto } from '@M/forum/dtos/Post.ts'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import { renderAvatar } from '@SU/renderUTil.ts'
import ReplyToComment from '@M/forum/components/replyToComment/ReplyToComment.vue'

const route = useRoute()
const router = useRouter()
const post = ref<PostDto | null>(null)
const comments = ref<CommentDto[]>([])
const { isAuthenticated } = useAuth()

/* ---------------- 使用独立的加载状态，避免互相取消 ---------------- */
const { isLoading: isPostLoading, errorMsg: postError, executeAsync: executePostAsync } = useAsyncLoader()
const { isLoading: isCommentsLoading, errorMsg: commentsError, executeAsync: executeCommentsAsync } = useAsyncLoader()

// 合并的加载状态（用于模板显示）
const isLoading = isPostLoading

// 合并的错误信息
const errorMsg = postError

// 投票状态
const isVoting = ref(false)
const voteError = ref<string | null>(null)

// 获取帖子详情
async function fetchPost() {
  const slug = route.params.slug as string
  console.debug('[PostDetail] 开始获取帖子, slug:', slug, 'route.params:', route.params)

  if (!slug) {
    console.error('[PostDetail] slug 参数为空')
    return
  }

  const result = await executePostAsync(async (signal) => {
    console.debug('[PostDetail] 调用 forumService.getPostBySlug...')
    const apiResult = await forumService.getPostBySlug(slug, { signal })

    console.debug('[PostDetail] API 结果:', apiResult)

    if (apiResult.isSuccess) {
      const value = apiResult.getValue()
      console.debug('[PostDetail] 获取成功:', value)
      return value
    }

    // 输出实际错误信息便于调试
    console.error('[PostDetail] 获取帖子失败:', apiResult.error)
    throw new Error(`获取帖子失败: ${apiResult.error || '未知错误'}`)
  }, translate('forum', 'error'))

  if (result) {
    post.value = result.post
    console.debug('[PostDetail] 设置 post.value:', post.value)
    // 加载帖子作者头像
    loadPostAuthorAvatar()
  } else {
    console.error('[PostDetail] result 为空')
  }
}

// 获取评论
async function fetchComments() {
  const slug = route.params.slug as string
  if (!slug) return

  const result = await executeCommentsAsync(async (signal) => {
    const apiResult = await forumService.getCommentsByPostSlug(slug, { signal })

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error('获取评论失败')
  }, translate('forum', 'error'))

  if (result) {
    comments.value = result.comments
    // 渲染评论头像
    renderAllAvatars()
  }
}

// 投票
async function vote(direction: 'up' | 'down') {
  if (!post.value) return
  
  // 检查是否已登录
  if (!isAuthenticated()) {
    router.push('/login')
    return
  }
  
  isVoting.value = true
  voteError.value = null
  
  const postId = post.value.postId || post.value.slug
  const result = direction === 'up' 
    ? await forumService.upvotePost(postId)
    : await forumService.downvotePost(postId)
  
  if (result.isSuccess) {
    // 更新本地投票数
    const voteData = result.getValue()
    post.value = {
      ...post.value,
      points: voteData.newPoints || voteData.points
    }
  } else {
    voteError.value = String(result.error)
  }
  
  isVoting.value = false
}

// 评论投票
async function voteComment(commentId: string) {
  if (!isAuthenticated()) {
    router.push('/login')
    return
  }
  
  const result = await forumService.upvoteComment(commentId)
  
  if (result.isSuccess) {
    // 刷新评论列表
    await fetchComments()
  }
}

// 获取作者首字母
function getAuthorInitial(post: PostDto | null): string {
  if (!post) return 'U'
  const nickname = post.memberPostBy?.nickname
  if (!nickname) return 'U'
  return nickname.charAt(0).toUpperCase()
}

// 帖子作者头像
const postAuthorAvatar = ref<string | null>(null)

async function loadPostAuthorAvatar() {
  if (!post.value?.memberPostBy?.playerSkin) return
  try {
    const skinDataUrl = `data:image/png;base64,${post.value.memberPostBy.playerSkin}`
    postAuthorAvatar.value = await renderAvatar(skinDataUrl, 24)
  } catch (e) {
    console.warn('Failed to render post author avatar:', e)
  }
}

// 评论头像缓存
const commentAvatars = ref<Map<string, string>>(new Map())

// 渲染评论头像
async function renderCommentAvatar(comment: CommentDto) {
  if (!comment.memberPlayerSkin) return
  if (commentAvatars.value.has(comment.commentId)) return
  
  try {
    // 添加 data URL 前缀
    const skinDataUrl = `data:image/png;base64,${comment.memberPlayerSkin}`
    const avatarUrl = await renderAvatar(skinDataUrl, 24)
    commentAvatars.value.set(comment.commentId, avatarUrl)
  } catch (e) {
    console.warn('Failed to render avatar for comment:', comment.commentId, e)
  }
}

// 监听评论变化，渲染头像
async function renderAllAvatars() {
  for (const comment of comments.value) {
    await renderCommentAvatar(comment)
  }
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<template>
  <div class="bf-post-detail">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="bf-loading">
      <div class="bf-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="errorMsg" class="bf-error">
      <svg class="bf-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <span>{{ errorMsg }}</span>
    </div>

    <template v-else-if="post">
      <!-- 帖子内容卡片 -->
      <div class="bf-post-card">
        <!-- 投票区域 -->
        <div class="bf-vote-section">
          <button class="bf-vote-btn" @click="vote('up')">
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
              <path d="M12 4l-8 8h6v8h4v-8h6z"/>
            </svg>
          </button>
          <span class="bf-vote-count">{{ post.points }}</span>
          <button class="bf-vote-btn bf-vote-btn--down" @click="vote('down')">
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
              <path d="M12 20l8-8h-6v-8h-4v8h-6z"/>
            </svg>
          </button>
        </div>

        <!-- 帖子主体 -->
        <div class="bf-post-content">
          <!-- 标题 -->
          <h1 class="bf-post-title">{{ post.title }}</h1>

          <!-- 元信息 -->
          <div class="bf-post-meta">
            <div class="bf-author">
              <img
                v-if="postAuthorAvatar"
                :src="postAuthorAvatar"
                class="bf-author-avatar bf-author-avatar--img"
                alt=""
              />
              <div v-else class="bf-author-avatar">
                {{ getAuthorInitial(post) }}
              </div>
              <span class="bf-author-name">{{ post.memberPostBy?.nickname || 'Unknown' }}</span>
            </div>
            <span class="bf-meta-divider">•</span>
            <span class="bf-post-date">{{ new Date(post.createdAt).toLocaleDateString() }}</span>
            <span class="bf-meta-divider">•</span>
            <span class="bf-post-comments">{{ post.numComments }} 评论</span>
          </div>

          <!-- 帖子文本 -->
          <div v-if="post.text" class="bf-post-text">
            {{ post.text }}
          </div>

          <!-- 链接预览 -->
          <div v-if="post.link" class="bf-link-preview">
            <svg class="bf-link-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
            <a :href="post.link" target="_blank" class="bf-link-text">{{ post.link }}</a>
          </div>

          <!-- 操作栏 -->
          <div class="bf-post-actions">
            <button class="bf-action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <span>评论</span>
            </button>
            <button class="bf-action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>分享</span>
            </button>
            <button class="bf-action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
              <span>收藏</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="bf-comments-section">
        <h2 class="bf-section-title">
          <span class="bf-text-gradient">{{ translate('forum', 'comments') }}</span>
        </h2>

        <!-- 评论列表 -->
        <div class="bf-comments-list">
          <div
            v-for="comment in comments"
            :key="comment.commentId"
            class="bf-comment-card"
          >
            <!-- 投票 -->
            <div class="bf-comment-vote">
              <button class="bf-vote-btn bf-vote-btn--small">
                <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                  <path d="M12 4l-8 8h6v8h4v-8h6z"/>
                </svg>
              </button>
              <span class="bf-comment-points">{{ comment.points }}</span>
            </div>

            <!-- 评论内容 -->
            <div class="bf-comment-content">
              <div class="bf-comment-meta">
                <img
                  v-if="commentAvatars.get(comment.commentId)"
                  :src="commentAvatars.get(comment.commentId)"
                  class="bf-comment-avatar"
                  alt=""
                />
                <div v-else class="bf-comment-avatar-placeholder">
                  {{ (comment.memberNickname || comment.memberId).charAt(0).toUpperCase() }}
                </div>
                <span class="bf-comment-author">{{ comment.memberNickname || comment.memberId }}</span>
                <span class="bf-meta-divider">•</span>
                <span class="bf-comment-date">{{ new Date(comment.createdAt).toLocaleDateString() }}</span>
              </div>
              <div class="bf-comment-text">{{ comment.text }}</div>
            </div>
          </div>
        </div>

        <!-- 无评论提示 -->
        <div v-if="comments.length === 0" class="bf-empty-comments">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>{{ translate('forum', 'noComments') }}</span>
        </div>

        <!-- 回复评论组件 -->
        <ReplyToComment :post-slug="post.slug" @reply-added="fetchComments" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.bf-post-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--bf-space-lg, 24px);
}

/* 加载状态 */
.bf-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-2xl, 48px);
  color: var(--bf-text-secondary, #B3B3B3);
}

.bf-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-top-color: var(--bf-primary, #FF6B35);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.bf-error {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-lg, 24px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--bf-card-radius, 16px);
  color: #ef4444;
}

.bf-error-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

/* 帖子卡片 */
.bf-post-card {
  display: flex;
  gap: var(--bf-space-md, 16px);
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-lg, 24px);
  margin-bottom: var(--bf-space-lg, 24px);
}

/* 投票区域 */
.bf-vote-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
}

.bf-vote-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--bf-text-muted, #666666);
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-vote-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  color: var(--bf-primary, #FF6B35);
}

.bf-vote-btn--small {
  width: 24px;
  height: 24px;
}

.bf-vote-count {
  font-size: 1rem;
  font-weight: 700;
  color: var(--bf-text-primary, #FFFFFF);
}

/* 帖子主体 */
.bf-post-content {
  flex: 1;
  min-width: 0;
}

.bf-post-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--bf-text-primary, #FFFFFF);
  margin: 0 0 var(--bf-space-md, 16px);
  line-height: 1.3;
}

.bf-post-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--bf-space-sm, 8px);
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-author {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
}

.bf-author-avatar {
  width: 24px;
  height: 24px;
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.bf-author-avatar--img {
  background: transparent;
  border-radius: 4px;
  image-rendering: pixelated;
}

.bf-author-name {
  color: var(--bf-primary, #FF6B35);
  font-weight: 500;
}

.bf-meta-divider {
  color: var(--bf-text-muted, #666666);
}

.bf-post-date,
.bf-post-comments {
  color: var(--bf-text-secondary, #B3B3B3);
  font-size: 0.875rem;
}

.bf-post-text {
  color: var(--bf-text-secondary, #B3B3B3);
  line-height: 1.7;
  margin-bottom: var(--bf-space-md, 16px);
  white-space: pre-wrap;
}

/* 链接预览 */
.bf-link-preview {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-input-radius, 10px);
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-link-icon {
  width: 18px;
  height: 18px;
  color: var(--bf-primary, #FF6B35);
  flex-shrink: 0;
}

.bf-link-text {
  color: var(--bf-primary, #FF6B35);
  text-decoration: none;
  word-break: break-all;
  font-size: 0.875rem;
}

.bf-link-text:hover {
  text-decoration: underline;
}

/* 操作栏 */
.bf-post-actions {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  padding-top: var(--bf-space-md, 16px);
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-action-btn {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: transparent;
  border: none;
  border-radius: var(--bf-btn-radius, 12px);
  color: var(--bf-text-secondary, #B3B3B3);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-action-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--bf-text-primary, #FFFFFF);
}

/* 评论区域 */
.bf-comments-section {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-lg, 24px);
}

.bf-section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.bf-text-gradient {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

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

.bf-comment-points {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--bf-text-secondary, #B3B3B3);
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
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
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
  color: var(--bf-primary, #FF6B35);
  font-size: 0.875rem;
}

.bf-comment-date {
  color: var(--bf-text-muted, #666666);
  font-size: 0.75rem;
}

.bf-comment-text {
  color: var(--bf-text-secondary, #B3B3B3);
  font-size: 0.875rem;
  line-height: 1.5;
}

/* 无评论 */
.bf-empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-xl, 32px);
  color: var(--bf-text-muted, #666666);
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-post-card {
    flex-direction: column;
  }

  .bf-vote-section {
    flex-direction: row;
    gap: var(--bf-space-md, 16px);
  }

  .bf-post-actions {
    flex-wrap: wrap;
  }
}
</style>
