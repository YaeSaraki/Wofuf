<script setup lang="ts">
import { ref, onMounted } from 'vue'
// 引入封装的组合式函数
import { useAsyncLoader } from '@/shared/composables/useAsyncLoader'
// 业务 API 和组件、类型
import {
  getPlayerStatistics,
  getPlayerAdvancements,
  getPlayerYesterdayOnline,
} from '@/modules/players/api/players.ts'
import PlayerProfileCard from '@/modules/players/components/PlayerProfileCard.vue'
import StatisticsPanel from '@/modules/players/components/StatisticsPanel.vue'
import AdvancementsPanel from '@/modules/players/components/AdvancementsPanel.vue'
import type {
  PlayerProfile,
  PlayerStatisticsData,
  PlayerAdvancementsData,
} from '@/modules/players/types/player.ts'

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 业务状态 ---------------- */
const player = ref<PlayerProfile | null>(null)
const statistics = ref<PlayerStatisticsData | null>(null)
const advancements = ref<PlayerAdvancementsData | null>(null)

/* ---------------- 业务行为 ---------------- */
async function loadRandomPlayer() {
  // 使用封装的 executeAsync 执行异步逻辑
  await executeAsync(async (signal) => {

    const [statRes, advRes] = await Promise.all([
      getPlayerStatistics(profile.name, { signal }),
      getPlayerAdvancements(profile.name, { signal }),
    ])

    statistics.value = statRes.data.data
    advancements.value = advRes.data.data
  }, '加载玩家数据失败，请稍后重试') // 自定义错误提示
}

// 组件挂载时加载数据
onMounted(loadRandomPlayer)

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
      <button
        class="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600"
        @click="loadRandomPlayer"
      >
        重试
      </button>
    </div>

    <!-- 📦 正常态 -->
    <div v-else>
      <PlayerProfileCard v-if="player" :player="player" />
      <StatisticsPanel v-if="statistics" :data="statistics" />
      <AdvancementsPanel v-if="advancements" :data="advancements" />
      <!-- 🪹 空数据兜底 -->
      <p v-if="!player" class="text-center text-zinc-400 mt-12">暂无玩家数据</p>
    </div>
  </div>
</template>
