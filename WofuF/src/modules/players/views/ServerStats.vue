<script setup lang="ts">
/* ---------------- 复用通用加载逻辑 ---------------- */
import { useAsyncLoader } from '@/shared/composables/useAsyncLoader.ts'
import { ref } from 'vue'
import YesterdayOnlineList from '@/modules/players/components/YesterdayOnlineList.vue'

const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

const data = ref({
  world_time: '233',
  players_online: '233',
})
</script>
<template>
  <div class="min-h-screen p-4 bg-zinc-100 dark:bg-zinc-900">
    <!-- 🌀 加载态（复用封装的 isLoading） -->
    <div v-if="isLoading" class="flex justify-center items-center h-64">
      <div
        class="animate-spin h-10 w-10 rounded-full border-4 border-zinc-300 border-t-transparent"
      />
    </div>

    <!-- ❌ 错误态（复用封装的 errorMsg） -->
    <div
      v-else-if="errorMsg"
      class="flex flex-col items-center justify-center h-64 gap-4 text-zinc-600 dark:text-zinc-300"
    >
      <p>{{ errorMsg }}</p>
      <button class="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600" @click="">
        重试
      </button>
    </div>

    <!-- 📦 正常态 -->
    <div v-else>
      <div>
        <h1 class="text-center text-5xl font-bold text-zinc-800 dark:text-zinc-200">
          Minecraft Server
        </h1>
      </div>

      <div class="flex justify-center items-center gap-8 mt-4">
        <div class="align-center text-center">
          <span class="text-center text-lg text-zinc-600 dark:text-zinc-300"> 世界历时 </span>
          <br />
          <span class="text-center text-2xl font-bold text-zinc-800 dark:text-zinc-200">
            {{ data.world_time }}
          </span>
        </div>

        <div class="align-center text-center">
          <span class="text-center text-lg text-zinc-600 dark:text-zinc-300"> 在线玩家 </span>
          <br />
          <span class="text-center text-2xl font-bold text-zinc-800 dark:text-zinc-200">
            {{ data.players_online }}
          </span>
        </div>
      </div>

      <YesterdayOnlineList />

    </div>




  </div>
</template>
<style scoped></style>
