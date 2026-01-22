<script setup lang="ts">
defineProps<{
  player: {
    name: string
    totalPlaytimeSeconds: number
    lastLogin: number
  }
}>()
</script>

<template>
  <div class="flex gap-4 bg-white dark:bg-zinc-800 p-4 rounded-xl shadow">
    <img class="w-24 h-24" :src="`https://visage.surgeplay.com/bust/128/${player.name}`" />

    <div>
      <h2 class="text-xl font-bold">{{ player.name }}</h2>
      <p>游玩时长：{{ (player.totalPlaytimeSeconds / 360000000).toFixed(1) }} h</p>
      <p>最近上线：{{ new Date(player.lastLogin).toLocaleString() }}</p>
    </div>
  </div>
</template>
