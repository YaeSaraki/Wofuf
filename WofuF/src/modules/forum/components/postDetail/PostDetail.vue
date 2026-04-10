<script lang="ts" setup>
import { ref, onMounted, watch, reactive, computed, onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { PostDto, CommentDto } from '@M/forum/dtos/Post.ts'
import { forumService } from '@M/forum/services/ForumService.ts'
import { imageService } from '@M/forum/services/ImageService.ts'
import { cacheService } from '@S/infra/cache'
import { useAuth } from '@M/auth/composables/useAuth.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import { PlayerService } from '@M/players/services/PlayerService'

// 子组件
import PostHeader from './components/PostHeader.vue'
import PostContent from './components/PostContent.vue'
import PostLink from './components/PostLink.vue'
import PostVote from './components/PostVote.vue'
import PostActions from './components/PostActions.vue'
import PostToc from './components/PostToc.vue'
import CommentForm from './components/CommentForm.vue'
import CommentList from './components/CommentList.vue'
import ImageLightbox from '@M/forum/components/imageLightbox/ImageLightbox.vue'

const playerService = new PlayerService()

const route = useRoute()
const router = useRouter()
const post = ref<PostDto | null>(null)
const comments = ref<CommentDto[]>([])
const { isAuthenticated } = useAuth()

// 来源页面（用于返回导航）
const fromRoute = ref<string | null>(null)

// 在组件挂载前检查来源
onBeforeMount(() => {
  // 从 sessionStorage 获取来源
  const sessionFrom = sessionStorage.getItem('forum_post_from')
  if (sessionFrom) {
    fromRoute.value = sessionFrom
    // 清除，只使用一次
    sessionStorage.removeItem('forum_post_from')
  }
})

// 评论区域显示状态
const showComments = ref(true)

// 回复状态
const replyingToCommentId = ref<string | null>(null)

// 获取正在回复的评论数据
const replyingToComment = computed(() => {
  if (!replyingToCommentId.value) return null
  return comments.value.find(c => c.commentId === replyingToCommentId.value) || null
})

// 导航返回
function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back() // 原路返回，天然会保留所有 URL 参数（Query）
  } else {
    // 3. 兜底：如果是直接通过别人分享的链接点进来的（没有历史栈），则跳回主页
    router.push('/forum')
  }
}

// 处理回复按钮点击
function handleReply(commentId: string) {
  replyingToCommentId.value = commentId
  // 自动展开评论区域
  if (!showComments.value) {
    showComments.value = true
  }
}

// 处理回复表单取消
function handleReplyCancelled() {
  replyingToCommentId.value = null
}

/* ---------------- 使用独立的加载状态 ---------------- */
const {
  isLoading: isPostLoading,
  errorMsg: postError,
  executeAsync: executePostAsync,
} = useAsyncLoader()
const {
  isLoading: isCommentsLoading,
  errorMsg: commentsError,
  executeAsync: executeCommentsAsync,
} = useAsyncLoader()

// 合并的加载状态
const isLoading = isPostLoading
const errorMsg = postError

// 投票状态
const isVoting = ref(false)
const voteError = ref<string | null>(null)

/* ---------------- 图片灯箱 ---------------- */
const lightboxVisible = ref(false)
const lightboxImageSrc = ref('')
const lightboxImageAlt = ref('')
const lightboxImages = ref<string[]>([])
const lightboxCurrentIndex = ref(0)

// 帖子内容引用（用于目录导航）
const postContentComponent = ref<InstanceType<typeof PostContent> | null>(null)
const postContentRef = computed(() => postContentComponent.value?.postContentRef || null)

// 从帖子内容中提取所有图片
const postImages = computed(() => {
  if (!post.value?.text) return []
  return imageService.extractImageUrls(post.value.text)
})

// 打开图片灯箱
function openLightbox(src: string, alt: string) {
  const images = postImages.value
  const index = images.indexOf(src)

  lightboxImages.value = images
  lightboxCurrentIndex.value = index >= 0 ? index : 0
  lightboxImageSrc.value = src
  lightboxImageAlt.value = alt
  lightboxVisible.value = true
}

// 关闭图片灯箱
function closeLightbox() {
  lightboxVisible.value = false
}

// 更新灯箱当前索引
function updateLightboxIndex(index: number) {
  lightboxCurrentIndex.value = index
  if (lightboxImages.value[index]) {
    lightboxImageSrc.value = lightboxImages.value[index]
  }
}

// 获取帖子详情
async function fetchPost() {
  const slug = route.params.slug as string
  console.debug('[PostDetail] 开始获取帖子, slug:', slug, 'route.params:', route.params)

  if (!slug) {
    console.error('[PostDetail] slug 参数为空')
    return
  }

  const result = await executePostAsync(
    async (signal) => {
      console.debug('[PostDetail] 调用 forumService.getPostBySlug...')
      const apiResult = await forumService.getPostBySlug(slug, { signal })

      console.debug('[PostDetail] API 结果:', apiResult)

      if (apiResult.isSuccess) {
        const value = apiResult.getValue()
        console.debug('[PostDetail] 获取成功:', value)
        return value
      }

      console.error('[PostDetail] 获取帖子失败:', apiResult.error)
      throw new Error(`获取帖子失败: ${apiResult.error || '未知错误'}`)
    },
    translate('forum', 'error'),
  )

  if (result) {
    post.value = result.post
    console.debug('[PostDetail] 设置 post.value:', post.value)
    loadPostAuthorAvatar()
  } else {
    console.error('[PostDetail] result 为空')
  }
}

// 获取评论
async function fetchComments() {
  const slug = route.params.slug as string
  if (!slug) return

  // 清除评论缓存，确保获取最新数据
  cacheService.delete('forum_service', `comments_${slug}`)

  const result = await executeCommentsAsync(
    async (signal) => {
      const apiApi = await forumService.getCommentsByPostSlug(slug, { signal })

      if (apiApi.isSuccess) {
        return apiApi.getValue()
      }

      throw new Error('获取评论失败')
    },
    translate('forum', 'error'),
  )

  if (result) {
    comments.value = result.comments
  }
}

// 投票
async function vote(direction: 'up' | 'down') {
  if (!post.value) return

  if (!isAuthenticated()) {
    router.push('/forum/login')
    return
  }

  isVoting.value = true
  voteError.value = null

  const postId = post.value.postId || post.value.slug
  const result =
    direction === 'up'
      ? await forumService.upvotePost(postId)
      : await forumService.downvotePost(postId)

  if (result.isSuccess) {
    const voteData = result.getValue()
    const wasUpvoted = post.value.wasUpvotedByMe
    const wasDownvoted = post.value.wasDownvotedByMe

    let newUpvoted = false
    let newDownvoted = false

    if (direction === 'up') {
      if (wasUpvoted) {
        newUpvoted = false
      } else {
        newUpvoted = true
        newDownvoted = false
      }
    } else {
      if (wasDownvoted) {
        newDownvoted = false
      } else {
        newDownvoted = true
        newUpvoted = false
      }
    }

    post.value = {
      ...post.value,
      points: voteData.newPoints || voteData.points,
      wasUpvotedByMe: newUpvoted,
      wasDownvotedByMe: newDownvoted,
    }
  } else {
    voteError.value = String(result.error)
  }

  isVoting.value = false
}

// 帖子作者头像
const postAuthorAvatar = ref<string | null>(null)

async function loadPostAuthorAvatar() {
  const playerId = post.value?.memberPostBy?.playerId
  console.log('[PostDetail] loadPostAuthorAvatar called, playerId:', playerId, 'post:', post.value)
  if (!playerId) return

  try {
    const result = await playerService.getPlayerSkin(playerId)
    console.log('[PostDetail] getPlayerSkin result:', result, 'isSuccess:', result.isSuccess)
    if (result.isSuccess) {
      const skinData = result.getValue()
      console.log('[PostDetail] skinData:', skinData, 'hasSkin:', !!skinData?.skin)
      if (skinData?.skin) {
        postAuthorAvatar.value = await playerService.renderAvatar(skinData.skin, 24)
        console.log('[PostDetail] postAuthorAvatar set')
      }
    } else {
      console.log('[PostDetail] result is failure, error:', result.error)
    }
  } catch (e) {
    console.warn('[PostDetail] Failed to load post author avatar:', e)
  }
}

// 监听帖子变化加载作者头像
watch(() => post.value?.memberPostBy?.playerId, loadPostAuthorAvatar)

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<template>
  <div class="bf-post-detail">
    <!-- 返回按钮 -->
    <div class="bf-back-nav">
      <button class="bf-back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        <span>{{ translate('forum', 'back') }}</span>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="bf-loading">
      <div class="bf-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="errorMsg" class="bf-error">
      <svg
        class="bf-error-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <span>{{ errorMsg }}</span>
    </div>

    <template v-else-if="post">
      <!-- 目录侧边栏 -->
      <PostToc :content="post.text" :content-ref="postContentRef" />

      <!-- 帖子内容卡片 -->
      <div class="bf-post-card">
        <!-- 帖子主体 -->
        <div class="bf-post-content-wrapper">
          <!-- 头部信息 -->
          <PostHeader :post="post" :author-avatar="postAuthorAvatar" />

          <!-- 帖子内容 -->
          <PostContent
            ref="postContentComponent"
            :content="post.text"
            @image-click="openLightbox"
          />

          <!-- 链接预览 -->
          <PostLink :link="post.link" />

          <!-- 投票区域 -->
          <PostVote
            :points="post.points"
            :was-upvoted="post.wasUpvotedByMe"
            :was-downvoted="post.wasDownvotedByMe"
            :is-voting="isVoting"
            @vote="vote"
          />

          <!-- 操作栏 -->
          <PostActions
            :num-comments="post.numComments"
            :show-comments="showComments"
            @toggle-comments="showComments = !showComments"
          />
        </div>

        <!-- 评论区域 -->
        <Transition name="bf-comments">
          <div v-show="showComments" class="bf-comments-section">
            <h2 class="bf-section-title">
              <span class="bf-text-gradient">{{ translate('forum', 'comments') }}</span>
            </h2>

            <!-- 评论输入 -->
            <CommentForm
              :post-slug="post.slug"
              :parent-comment-id="replyingToCommentId ?? undefined"
              :parent-comment="replyingToComment"
              @reply-added="fetchComments(); replyingToCommentId = null"
              @reply-cancelled="handleReplyCancelled"
            />

            <!-- 评论列表 -->
            <CommentList :comments="comments" :replying-to-comment-id="replyingToCommentId" @reply="handleReply" @reply-cancelled="handleReplyCancelled" @comment-updated="fetchComments" />

            <!-- 无评论提示 -->
            <div v-if="comments.length === 0" class="bf-empty-comments">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                width="48"
                height="48"
              >
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
              <span>{{ translate('forum', 'noComments') }}</span>
            </div>
          </div>
        </Transition>
      </div>
    </template>

    <!-- 图片灯箱 -->
    <ImageLightbox
      :visible="lightboxVisible"
      :src="lightboxImageSrc"
      :alt="lightboxImageAlt"
      :images="lightboxImages"
      :current-index="lightboxCurrentIndex"
      @close="closeLightbox"
      @update:current-index="updateLightboxIndex"
    />
  </div>
</template>

<style scoped>
.bf-post-detail {
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--bf-space-lg, 24px);
  padding-top: 5rem;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 返回按钮 */
.bf-back-nav {
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-radius-md, 8px);
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bf-back-btn:hover {
  background: var(--bf-card-bg-hover, rgba(38, 38, 38, 0.9));
  color: var(--bf-text-primary, #ffffff);
  border-color: var(--bf-primary, #ff6b35);
}

.bf-back-btn svg {
  width: 18px;
  height: 18px;
}

/* 加载状态 */
.bf-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-2xl, 48px);
  color: var(--bf-text-secondary, #b3b3b3);
}

.bf-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-top-color: var(--bf-primary, #ff6b35);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-lg, 24px);
  box-sizing: border-box;
  overflow-x: hidden;
}

.bf-post-content-wrapper {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
}

/* 评论区域 */
.bf-comments-section {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-lg, 24px);
  margin-top: var(--bf-space-xl, 32px);
  padding-top: var(--bf-space-xl, 32px);
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.bf-text-gradient {
  background: var(
    --bf-fire-gradient,
    linear-gradient(135deg, #ff6b35 0%, #ff9f1c 50%, #ffbe0b 100%)
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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

/* 评论区域过渡动画 */
.bf-comments-enter-active,
.bf-comments-leave-active {
  transition: all 0.3s ease;
}

.bf-comments-enter-from,
.bf-comments-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-post-detail {
    padding: var(--bf-space-md, 16px);
    overflow-x: hidden;
  }

  .bf-post-card {
    padding: var(--bf-space-sm, 12px);
    overflow-x: hidden;
  }
}
</style>
