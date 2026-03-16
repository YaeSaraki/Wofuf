<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ForumService } from '@M/forum/services/ForumService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'
import type { CreatePostRequest } from '@M/forum/dtos/Post.ts'

const router = useRouter()
const forumService = new ForumService()

/* ---------------- 表单数据 ---------------- */
const formData = ref<CreatePostRequest>({
  userId: '', // 需要从认证获取
  title: '',
  type: 'TEXT',
  text: '',
  link: '',
})

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, executeAsync } = useAsyncLoader()

// 提交创建帖子
async function createPost() {
  if (!formData.value.title.trim()) {
    alert(translate('forum', 'titleRequired'))
    return
  }

  // TODO: 从认证获取userId
  formData.value.userId = 'current-user-id' // 临时

  const result = await executeAsync(async () => {
    const apiResult = await forumService.createPost(formData.value)

    if (apiResult.isSuccess) {
      return apiResult.getValue()
    }

    throw new Error('创建帖子失败')
  }, translate('forum', 'createPostFailed'))

  if (result) {
    // 跳转到新创建的帖子
    router.push(`/forum/posts/${result.slug}`)
  }
}

// 重置表单
function resetForm() {
  formData.value = {
    userId: '',
    title: '',
    type: 'TEXT',
    text: '',
    link: '',
  }
}
</script>

<template>
  <div class="create-post">
    <div class="max-w-2xl mx-auto p-6">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-6">
        {{ translate('forum', 'create_post') }}
      </h1>

      <form @submit.prevent="createPost" class="space-y-6">
        <!-- 标题 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ translate('forum', 'title') }}
          </label>
          <input
            v-model="formData.title"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            :placeholder="translate('forum', 'enterTitle')"
          />
        </div>

        <!-- 类型选择 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ translate('forum', 'postType') }}
          </label>
          <select
            v-model="formData.type"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
          >
            <option value="TEXT">{{ translate('forum', 'textPost') }}</option>
            <option value="LINK">{{ translate('forum', 'linkPost') }}</option>
          </select>
        </div>

        <!-- 文本内容 -->
        <div v-if="formData.type === 'TEXT'">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ translate('forum', 'content') }}
          </label>
          <textarea
            v-model="formData.text"
            rows="6"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            :placeholder="translate('forum', 'enterContent')"
          ></textarea>
        </div>

        <!-- 链接 -->
        <div v-if="formData.type === 'LINK'">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ translate('forum', 'link') }}
          </label>
          <input
            v-model="formData.link"
            type="url"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            :placeholder="translate('forum', 'enterLink')"
          />
        </div>

        <!-- 用户ID（临时） -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            User ID (临时)
          </label>
          <input
            v-model="formData.userId"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            placeholder="输入用户ID"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="flex space-x-4">
          <button
            type="submit"
            :disabled="isLoading"
            class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="isLoading">{{ translate('forum', 'creating') }}</span>
            <span v-else>{{ translate('forum', 'create') }}</span>
          </button>
          <button
            type="button"
            @click="resetForm"
            class="px-6 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-400 dark:hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          >
            {{ translate('forum', 'reset') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
