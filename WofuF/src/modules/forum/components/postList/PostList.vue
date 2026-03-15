<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Post } from '@M/forum/dtos/Post.ts'
import { ForumService } from '@M/forum/services/ForumService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'

const router = useRouter()
const forumService = new ForumService()
const posts = ref<Post[]>([])

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 获取帖子列表
async function fetchPosts() {
  const result = await executeAsync(async (signal) => {
    const apiResult = await forumService.getPosts({ page: 1, limit: 20 }, { signal })

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error('获取帖子列表失败')
  }, translate('forum', 'error'))

  if (result) {
    posts.value = result.posts
  }
}

// 查看帖子详情
function viewPost(post: Post) {
  router.push(`/forum/posts/${post.slug}`)
}

onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <div class="post-list">
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ translate('forum', 'posts') }}
        </h1>
        <router-link
          to="/forum/create"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          {{ translate('forum', 'create_post') }}
        </router-link>
      </div>
    </div>

    <div v-if="isLoading" class="flex justify-center items-center h-40">
      <div class="animate-spin h-6 w-6 rounded-full border-2 border-blue-500 border-t-transparent"></div>
    </div>

    <div v-else-if="errorMsg" class="text-center text-red-500">
      {{ errorMsg }}
    </div>

    <div v-else>
      <div class="space-y-4">
        <div
          v-for="post in posts"
          :key="post.id"
          class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow"
          @click="viewPost(post)"
        >
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">
            {{ post.title }}
          </h2>
          <div class="flex items-center text-sm text-gray-600 dark:text-gray-400 mb-2">
            <span class="mr-4">{{ post.member?.nickname || 'Unknown' }}</span>
            <span>{{ new Date(post.dateTimePosted).toLocaleDateString() }}</span>
          </div>
          <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
            <span class="mr-4">{{ post.points }} {{ translate('forum', 'points') }}</span>
            <span>{{ post.totalNumComments }} {{ translate('forum', 'comments') }}</span>
          </div>
        </div>
      </div>

      <div v-if="posts.length === 0" class="text-center text-gray-500 dark:text-gray-400 mt-8">
        {{ translate('forum', 'noPosts') }}
      </div>
    </div>
  </div>
</template>
