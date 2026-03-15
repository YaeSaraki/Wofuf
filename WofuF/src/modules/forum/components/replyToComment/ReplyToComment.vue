<script lang="ts" setup>
import { ref } from 'vue'
import { ForumService } from '@M/forum/services/ForumService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { translate } from '@S/services/i18n'

interface Props {
  postId: string
  parentCommentId?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  replyAdded: []
}>()

/* ---------------- 表单数据 ---------------- */
const replyText = ref('')
const isReplying = ref(false)

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, executeAsync } = useAsyncLoader()

// 提交回复
async function submitReply() {
  if (!replyText.value.trim()) {
    alert(translate('forum', 'replyRequired'))
    return
  }

  // TODO: 从认证获取userId
  const userId = 'current-user-id' // 临时

  const result = await executeAsync(async () => {
    // 这里需要调用后端API创建评论
    // 暂时模拟成功
    await new Promise(resolve => setTimeout(resolve, 1000))
    return { success: true }
  }, translate('forum', 'replyFailed'))

  if (result) {
    replyText.value = ''
    isReplying.value = false
    emit('replyAdded')
  }
}

// 取消回复
function cancelReply() {
  replyText.value = ''
  isReplying.value = false
}
</script>

<template>
  <div class="reply-to-comment">
    <div v-if="!isReplying">
      <button
        @click="isReplying = true"
        class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 text-sm"
      >
        {{ translate('forum', 'reply') }}
      </button>
    </div>

    <div v-else class="mt-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
      <textarea
        v-model="replyText"
        rows="3"
        class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-600 dark:text-white"
        :placeholder="translate('forum', 'enterReply')"
      ></textarea>

      <div class="flex justify-end space-x-2 mt-2">
        <button
          @click="cancelReply"
          class="px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
        >
          {{ translate('forum', 'cancel') }}
        </button>
        <button
          @click="submitReply"
          :disabled="isLoading || !replyText.trim()"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="isLoading">{{ translate('forum', 'replying') }}</span>
          <span v-else>{{ translate('forum', 'submitReply') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
