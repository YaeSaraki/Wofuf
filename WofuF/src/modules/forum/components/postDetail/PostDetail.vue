<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import type { PostDto, CommentDto } from '@M/forum/dtos/Post.ts'
import { forumService } from '@M/forum/services/ForumService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import ReplyToComment from '@M/forum/components/replyToComment/ReplyToComment.vue'

const route = useRoute()
const post = ref<PostDto | null>(null)
const comments = ref<CommentDto[]>([])

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 获取帖子详情
async function fetchPost() {
  const slug = route.params.slug as string
  if (!slug) return

  const result = await executeAsync(async (signal) => {
    const apiResult = await forumService.getPostBySlug(slug, { signal })

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error('获取帖子失败')
  }, translate('forum', 'error'))

  if (result) {
    post.value = result.post
  }
}

// 获取评论
async function fetchComments() {
  const slug = route.params.slug as string
  if (!slug) return

  const result = await executeAsync(async (signal) => {
    const apiResult = await forumService.getCommentsByPostSlug(slug, { signal })

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error('获取评论失败')
  }, translate('forum', 'error'))

  if (result) {
    comments.value = result.comments
  }
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<template>
  <div class="post-detail">
    <div v-if="isLoading" class="flex justify-center items-center h-40">
      <div class="animate-spin h-6 w-6 rounded-full border-2 border-blue-500 border-t-transparent"></div>
    </div>

    <div v-else-if="errorMsg" class="text-center text-red-500">
      {{ errorMsg }}
    </div>

    <div v-else-if="post">
      <!-- 帖子内容 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 mb-6">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">
          {{ post.title }}
        </h1>
        <div class="flex items-center text-sm text-gray-600 dark:text-gray-400 mb-4">
          <span class="mr-4">{{ post.memberPostBy?.nickname || 'Unknown' }}</span>
          <span>{{ new Date(post.createdAt).toLocaleDateString() }}</span>
        </div>
        <div v-if="post.text" class="text-gray-700 dark:text-gray-300 mb-4">
          {{ post.text }}
        </div>
        <div v-if="post.link" class="text-blue-600 dark:text-blue-400">
          <a :href="post.link" target="_blank">{{ post.link }}</a>
        </div>
        <div class="flex items-center text-sm text-gray-600 dark:text-gray-400 mt-4">
          <span class="mr-4">{{ post.points }} {{ translate('forum', 'points') }}</span>
          <span>{{ post.numComments }} {{ translate('forum', 'comments') }}</span>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comments-section">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-4">
          {{ translate('forum', 'comments') }}
        </h2>
        <div class="space-y-4">
          <div
            v-for="comment in comments"
            :key="comment.commentId"
            class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-4"
          >
            <div class="flex items-center text-sm text-gray-600 dark:text-gray-400 mb-2">
              <span class="mr-4">{{ comment.memberId }}</span>
              <span>{{ new Date(comment.createdAt).toLocaleDateString() }}</span>
            </div>
            <div class="text-gray-700 dark:text-gray-300">
              {{ comment.text }}
            </div>
            <div class="flex items-center text-sm text-gray-600 dark:text-gray-400 mt-2">
              <span>{{ comment.points }} {{ translate('forum', 'points') }}</span>
            </div>
          </div>
        </div>

        <div v-if="comments.length === 0" class="text-center text-gray-500 dark:text-gray-400 mt-8">
          {{ translate('forum', 'noComments') }}
        </div>
      </div>

      <!-- 回复评论组件 -->
      <ReplyToComment :post-id="post.slug" @reply-added="fetchComments" />
    </div>
  </div>
</template>
