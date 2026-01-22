<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPlayerYesterdayOnline } from '@/modules/players/api/players.ts'
import type { PlayerNameList } from '@/modules/players/types/player.ts'
import { useAsyncLoader } from '@/shared/composables/useAsyncLoader.ts'

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 业务状态 ---------------- */
const players = ref<PlayerNameList | null>(null)

/* ---------------- 业务行为 ---------------- */
async function loadYesterdayOnlinePlayers() {
  // 使用封装的 executeAsync 执行异步逻辑
  await executeAsync(async (signal) => {
    const statRes = await getPlayerYesterdayOnline({ signal })

    players.value = statRes.data.data
  }, '加载昨日玩家失败，请稍后重试') // 自定义错误提示
}

onMounted(loadYesterdayOnlinePlayers)
</script>

<template>
  <section class="yesterday-online">
    <h2 class="title text-center">昨日登录玩家</h2>

    <!-- loading -->
    <div v-if="isLoading" class="hint text-center">加载中喵…</div>

    <div v-else-if="errorMsg">
      <p>{{ errorMsg }}</p>
      <button class="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600"
              @click="loadYesterdayOnlinePlayers"
      >
        重试
      </button>
    </div>

    <!-- list -->
    <div v-else class="card-list ml-8 mr-8">
      <div v-for="playerName in players || []" :key="playerName.name" class="player-card">
        <img class="avatar" :src="`https://mc-heads.net/avatar/${playerName.name}/64`" />
        <div class="name">{{ playerName.name }}</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.yesterday-online {
  padding: 24px;
}

.title {
  font-size: 18px;
  margin-bottom: 16px;
}

.hint {
  color: #999;
  font-size: 14px;
}

.card-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
}

.player-card {
  border-radius: 14px;
  padding: 14px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.15s ease;
}

.player-card:hover {
  transform: translateY(-4px);
}

.avatar {
  border-radius: 10%;
  margin-bottom: 8px;
}

.name {
  font-size: 13px;
  text-align: center;
  word-break: break-all;
}
</style>
