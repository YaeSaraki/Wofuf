<script setup lang="ts">
import type { PlayerAdvancementsData } from '@/modules/players/types/player.ts'

defineProps<{ data: PlayerAdvancementsData }>()
</script>

<template>
  <div class="mt-4 bg-white dark:bg-zinc-800 p-4 rounded-xl">
    <h3 class="font-bold mb-2">进度</h3>

    <div v-for="adv in data.advancements" :key="adv.key" class="text-sm">
      {{ adv.key }}
      <span v-if="adv.done">✅</span>
      <span v-else>❌</span>
    </div>
  </div>
</template>
