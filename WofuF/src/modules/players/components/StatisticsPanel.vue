<script setup lang="ts">
import { computed } from 'vue'
import type { PlayerStatisticsData } from '@/modules/players/types/player.ts'

const props = defineProps<{ data: PlayerStatisticsData }>()

const statsList = computed(() => Object.values(props.data.statistics))
</script>

<template>
  <div class="mt-4 bg-white dark:bg-zinc-800 p-4 rounded-xl">
    <h3 class="font-bold mb-2">玩家统计</h3>

    <div v-for="item in statsList" :key="item.key" class="flex justify-between text-sm">
      <span>{{ item.key }}</span>
      <span>{{ item.value }}</span>
    </div>
  </div>
</template>
